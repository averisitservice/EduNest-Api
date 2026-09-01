package com.edunest.service;

import com.edunest.common.PagedResponse;
import com.edunest.dto.student.*;
import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class StudentBulkUploadServiceImpl implements StudentBulkUploadService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern AADHAR_PATTERN = Pattern.compile("^[0-9]{12}$");

    @Autowired
    StudentBulkUploadRepository bulkUploadRepository;

    @Autowired
    StudentBulkUploadErrorRepository bulkUploadErrorRepository;

    @Autowired
    StudentService studentService;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    ClassSectionRepository classSectionRepository;

    @Autowired
    CommonHelper commonHelper;

    @Autowired
    FileStorageService fileStorageService;

    @Override
    @Transactional
    public StudentBulkUploadValidationResponse validateBulkUpload(Integer tenantId, Integer loginTeacherId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException("file", "Please select an Excel (.xlsx) file to upload");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".xlsx") && !originalFilename.toLowerCase().endsWith(".xls"))) {
            throw new CustomException("file", "Invalid file format. Only Excel files (.xlsx) are supported");
        }

        // Upload file to file storage for audit / insert re-parsing
        Map<String, Object> uploadResult = fileStorageService.uploadFile(file, "edunest/bulk_upload");
        String filePath = String.valueOf(uploadResult.get("secure_url"));

        // Create Bulk Upload Master Entry
        StudentBulkUpload uploadRecord = new StudentBulkUpload();
        uploadRecord.setTenantId(tenantId);
        uploadRecord.setFileName(originalFilename);
        uploadRecord.setFilePath(filePath);
        uploadRecord.setUploadedBy(loginTeacherId);
        uploadRecord.setStatus("PROCESSING");
        uploadRecord = bulkUploadRepository.save(uploadRecord);

        List<StudentUploadErrorDTO> errors = new ArrayList<>();

        // Cache Classes and Sections for Tenant
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        Map<String, ClassMaster> classMap = loadClassMap(tenantId);
        Map<String, ClassSection> sectionMap = loadSectionMap(tenantId);

        // Set to track duplicate roll numbers within the current sheet: key -> "classId-sectionId-rollNo"
        Set<String> sheetRollNoSet = new HashSet<>();

        int totalDataRows = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new CustomException("file", "Uploaded Excel file contains no data rows");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new CustomException("file", "Uploaded Excel file missing header row");
            }

            Map<String, Integer> colMap = buildHeaderMap(headerRow);
            validateRequiredHeaders(colMap);

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (isRowEmpty(row)) {
                    continue;
                }
                totalDataRows++;
                int excelRowNumber = r + 1; // 1-indexed row number in Excel

                StudentExcelRowDTO rowDto = parseRow(row, colMap, excelRowNumber);
                List<String> rowValidationErrors = new ArrayList<>();

                // Required Fields Validation
                if (!StringUtils.hasText(rowDto.getFirstName())) {
                    rowValidationErrors.add("First Name is required");
                }
                if (!StringUtils.hasText(rowDto.getLastName())) {
                    rowValidationErrors.add("Last Name is required");
                }
                if (rowDto.getGender() == null) {
                    rowValidationErrors.add("Gender is required (M, F, O)");
                }
                if (rowDto.getDateOfBirth() == null) {
                    rowValidationErrors.add("Valid Date of Birth (yyyy-MM-dd) is required");
                }
                if (!StringUtils.hasText(rowDto.getClassName())) {
                    rowValidationErrors.add("Class Name is required");
                }

                // Format Validations
                if (StringUtils.hasText(rowDto.getEmail()) && !EMAIL_PATTERN.matcher(rowDto.getEmail()).matches()) {
                    rowValidationErrors.add("Invalid student email format: " + rowDto.getEmail());
                }
                if (StringUtils.hasText(rowDto.getParentEmail()) && !EMAIL_PATTERN.matcher(rowDto.getParentEmail()).matches()) {
                    rowValidationErrors.add("Invalid parent email format: " + rowDto.getParentEmail());
                }
                if (StringUtils.hasText(rowDto.getMobileNo()) && !MOBILE_PATTERN.matcher(rowDto.getMobileNo()).matches()) {
                    rowValidationErrors.add("Invalid mobile number (must be 10 digits): " + rowDto.getMobileNo());
                }
                if (StringUtils.hasText(rowDto.getParentMobile()) && !MOBILE_PATTERN.matcher(rowDto.getParentMobile()).matches()) {
                    rowValidationErrors.add("Invalid parent mobile number (must be 10 digits): " + rowDto.getParentMobile());
                }
                if (StringUtils.hasText(rowDto.getAadharNo()) && !AADHAR_PATTERN.matcher(rowDto.getAadharNo()).matches()) {
                    rowValidationErrors.add("Invalid student Aadhar (must be 12 digits): " + rowDto.getAadharNo());
                }

                // Reference Resolution (Class & Section)
                ClassMaster classMaster = null;
                if (StringUtils.hasText(rowDto.getClassName())) {
                    classMaster = classMap.get(rowDto.getClassName().trim().toLowerCase());
                    if (classMaster == null) {
                        rowValidationErrors.add("Class '" + rowDto.getClassName() + "' does not exist in your school");
                    }
                }

                ClassSection classSection = null;
                if (classMaster != null && StringUtils.hasText(rowDto.getSectionName())) {
                    String sectionKey = classMaster.getClassId() + "-" + rowDto.getSectionName().trim().toLowerCase();
                    classSection = sectionMap.get(sectionKey);
                    if (classSection == null) {
                        rowValidationErrors.add("Section '" + rowDto.getSectionName() + "' does not exist for class '" + rowDto.getClassName() + "'");
                    }
                }

                // Roll Number Duplicate Validation
                if (classMaster != null && StringUtils.hasText(rowDto.getRollNo())) {
                    Integer classId = classMaster.getClassId();
                    Integer sectionId = classSection != null ? classSection.getSectionId() : null;
                    String rollKey = classId + "-" + (sectionId != null ? sectionId : "0") + "-" + rowDto.getRollNo().trim().toLowerCase();

                    if (sheetRollNoSet.contains(rollKey)) {
                        rowValidationErrors.add("Duplicate Roll Number '" + rowDto.getRollNo().trim() + "' found in this Excel sheet");
                    } else {
                        sheetRollNoSet.add(rollKey);
                    }

                    boolean rollExistsInDb = studentClassRepository.existsByRollNo(
                            tenantId, classId, sectionId, currentYear.getAcademicYearId(), rowDto.getRollNo().trim(), -1);
                    if (rollExistsInDb) {
                        rowValidationErrors.add("Roll Number '" + rowDto.getRollNo().trim() + "' already exists in database for this class/section");
                    }
                }

                String identifier = String.format("%s %s (%s)",
                        rowDto.getFirstName() != null ? rowDto.getFirstName() : "",
                        rowDto.getLastName() != null ? rowDto.getLastName() : "",
                        rowDto.getRollNo() != null ? "Roll: " + rowDto.getRollNo() : "Row " + excelRowNumber).trim();

                if (!rowValidationErrors.isEmpty()) {
                    for (String reason : rowValidationErrors) {
                        StudentBulkUploadError errorEntity = new StudentBulkUploadError();
                        errorEntity.setUploadId(uploadRecord.getUploadId());
                        errorEntity.setTenantId(tenantId);
                        errorEntity.setExcelRowNumber(excelRowNumber);
                        errorEntity.setStudentIdentifier(identifier);
                        errorEntity.setErrorType("VALIDATION_ERROR");
                        errorEntity.setErrorReason(reason);
                        errorEntity.setRowData(rowToJsonString(rowDto));
                        errorEntity.setStatus("VALIDATION_FAILED");
                        bulkUploadErrorRepository.save(errorEntity);

                        errors.add(StudentUploadErrorDTO.builder()
                                .excelRowNumber(excelRowNumber)
                                .studentIdentifier(identifier)
                                .errorType("VALIDATION_ERROR")
                                .errorReason(reason)
                                .rowData(rowToJsonString(rowDto))
                                .status("VALIDATION_FAILED")
                                .build());
                    }
                }
            }

        } catch (CustomException ce) {
            uploadRecord.setStatus("VALIDATION_FAILED");
            bulkUploadRepository.save(uploadRecord);
            throw ce;
        } catch (Exception e) {
            log.error("Failed to parse Excel file for uploadId={}", uploadRecord.getUploadId(), e);
            uploadRecord.setStatus("VALIDATION_FAILED");
            bulkUploadRepository.save(uploadRecord);
            throw new CustomException("file", "Failed to parse Excel file: " + e.getMessage());
        }

        int failedRowsCount = (int) errors.stream().map(StudentUploadErrorDTO::getExcelRowNumber).distinct().count();
        int validRowsCount = totalDataRows - failedRowsCount;
        String status = (failedRowsCount == 0) ? "VALIDATED" : (validRowsCount > 0 ? "PARTIALLY_VALIDATED" : "VALIDATION_FAILED");

        uploadRecord.setTotalRows(totalDataRows);
        uploadRecord.setSuccessRows(validRowsCount);
        uploadRecord.setFailedRows(failedRowsCount);
        uploadRecord.setStatus(status);
        bulkUploadRepository.save(uploadRecord);

        return StudentBulkUploadValidationResponse.builder()
                .uploadId(uploadRecord.getUploadId())
                .fileName(originalFilename)
                .totalRows(totalDataRows)
                .validRows(validRowsCount)
                .failedRows(failedRowsCount)
                .status(status)
                .createdDate(uploadRecord.getCreatedDate())
                .errors(errors)
                .build();
    }

    @Override
    @Transactional
    public StudentBulkUploadInsertResponse insertBulkUpload(Integer tenantId, Integer loginTeacherId, Integer uploadId) {
        StudentBulkUpload uploadRecord = bulkUploadRepository.findByUploadIdAndTenantId(uploadId, tenantId)
                .orElseThrow(() -> new CustomException("uploadId", "Upload history record not found"));

        if (!"VALIDATED".equals(uploadRecord.getStatus()) && !"PARTIALLY_VALIDATED".equals(uploadRecord.getStatus())) {
            throw new CustomException("status", "Cannot insert. Upload status is '" + uploadRecord.getStatus() + "'");
        }

        List<StudentBulkUploadError> existingErrors = bulkUploadErrorRepository
                .findByUploadIdAndTenantIdOrderByExcelRowNumberAsc(uploadId, tenantId);

        Set<Integer> failedRowNumbers = new HashSet<>();
        for (StudentBulkUploadError err : existingErrors) {
            failedRowNumbers.add(err.getExcelRowNumber());
        }

        Map<String, ClassMaster> classMap = loadClassMap(tenantId);
        Map<String, ClassSection> sectionMap = loadSectionMap(tenantId);

        int insertedCount = 0;
        int skippedCount = failedRowNumbers.size();
        int insertFailedCount = 0;

        try (InputStream is = URI.create(uploadRecord.getFilePath()).toURL().openStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> colMap = buildHeaderMap(headerRow);

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (isRowEmpty(row)) continue;

                int excelRowNumber = r + 1;
                if (failedRowNumbers.contains(excelRowNumber)) {
                    continue; // Skip invalid rows
                }

                StudentExcelRowDTO rowDto = parseRow(row, colMap, excelRowNumber);
                ClassMaster cm = classMap.get(rowDto.getClassName().trim().toLowerCase());
                ClassSection cs = null;
                if (cm != null && StringUtils.hasText(rowDto.getSectionName())) {
                    cs = sectionMap.get(cm.getClassId() + "-" + rowDto.getSectionName().trim().toLowerCase());
                }

                StudentDTO studentDto = new StudentDTO();
                studentDto.setFirstName(rowDto.getFirstName());
                studentDto.setMiddleName(rowDto.getMiddleName());
                studentDto.setLastName(rowDto.getLastName());
                studentDto.setGender(rowDto.getGender());
                studentDto.setDateOfBirth(rowDto.getDateOfBirth());
                studentDto.setAadharNo(rowDto.getAadharNo());
                studentDto.setEmail(rowDto.getEmail());
                studentDto.setMobileNo(rowDto.getMobileNo());
                studentDto.setAddressLine1(rowDto.getAddressLine1());
                studentDto.setCity(rowDto.getCity());
                studentDto.setState(rowDto.getState());
                studentDto.setPostalCode(rowDto.getPostalCode());
                studentDto.setFatherName(rowDto.getFatherName());
                studentDto.setMotherName(rowDto.getMotherName());
                studentDto.setParentMobile(rowDto.getParentMobile());
                studentDto.setParentEmail(rowDto.getParentEmail());
                studentDto.setParentAadhar(rowDto.getParentAadhar());
                studentDto.setClassId(cm != null ? cm.getClassId() : null);
                studentDto.setSectionId(cs != null ? cs.getSectionId() : null);
                studentDto.setRollNo(rowDto.getRollNo());
                studentDto.setIsHostel(rowDto.getIsHostel());
                studentDto.setPassword(rowDto.getPassword());

                try {
                    studentService.saveStudent(tenantId, loginTeacherId, studentDto);
                    insertedCount++;
                } catch (Exception ex) {
                    insertFailedCount++;
                    StudentBulkUploadError errorEntity = new StudentBulkUploadError();
                    errorEntity.setUploadId(uploadRecord.getUploadId());
                    errorEntity.setTenantId(tenantId);
                    errorEntity.setExcelRowNumber(excelRowNumber);
                    errorEntity.setStudentIdentifier(rowDto.getFirstName() + " " + rowDto.getLastName());
                    errorEntity.setErrorType("INSERTION_ERROR");
                    errorEntity.setErrorReason(ex.getMessage());
                    errorEntity.setRowData(rowToJsonString(rowDto));
                    errorEntity.setStatus("INSERTION_FAILED");
                    bulkUploadErrorRepository.save(errorEntity);
                }
            }
        } catch (Exception e) {
            log.error("Error reading file during insert for uploadId={}", uploadId, e);
            throw new CustomException("uploadId", "Failed to access uploaded file for insertion: " + e.getMessage());
        }

        String finalStatus = (insertFailedCount == 0 && skippedCount == 0) ? "INSERTED" : "PARTIALLY_INSERTED";
        uploadRecord.setInsertedRows(insertedCount);
        uploadRecord.setStatus(finalStatus);
        bulkUploadRepository.save(uploadRecord);

        return StudentBulkUploadInsertResponse.builder()
                .uploadId(uploadId)
                .totalRows(uploadRecord.getTotalRows())
                .insertedRows(insertedCount)
                .skippedRows(skippedCount)
                .failedRows(insertFailedCount)
                .status(finalStatus)
                .message(String.format("Bulk insertion completed. Inserted: %d, Skipped: %d, Failed: %d", insertedCount, skippedCount, insertFailedCount))
                .build();
    }

    @Override
    public PagedResponse<StudentBulkUploadHistoryResponse> getUploadHistory(Integer tenantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentBulkUpload> pageResult = bulkUploadRepository.findByTenantIdOrderByUploadIdDesc(tenantId, pageable);

        List<StudentBulkUploadHistoryResponse> content = new ArrayList<>();
        for (StudentBulkUpload u : pageResult.getContent()) {
            String uploaderName = commonHelper.teacherNameForId(u.getUploadedBy());
            content.add(StudentBulkUploadHistoryResponse.builder()
                    .uploadId(u.getUploadId())
                    .fileName(u.getFileName())
                    .totalRows(u.getTotalRows())
                    .successRows(u.getSuccessRows())
                    .failedRows(u.getFailedRows())
                    .insertedRows(u.getInsertedRows())
                    .status(u.getStatus())
                    .uploadedByName(uploaderName)
                    .createdDate(u.getCreatedDate())
                    .updatedDate(u.getUpdatedDate())
                    .build());
        }

        return new PagedResponse<>(content, pageResult.getTotalElements(), pageResult.getTotalPages(), pageResult.getNumber(), pageResult.getSize());
    }

    @Override
    public List<StudentUploadErrorDTO> getUploadErrors(Integer tenantId, Integer uploadId) {
        List<StudentBulkUploadError> errors = bulkUploadErrorRepository.findByUploadIdAndTenantIdOrderByExcelRowNumberAsc(uploadId, tenantId);
        List<StudentUploadErrorDTO> result = new ArrayList<>();

        for (StudentBulkUploadError e : errors) {
            result.add(StudentUploadErrorDTO.builder()
                    .errorId(e.getErrorId())
                    .excelRowNumber(e.getExcelRowNumber())
                    .studentIdentifier(e.getStudentIdentifier())
                    .errorType(e.getErrorType())
                    .errorReason(e.getErrorReason())
                    .rowData(e.getRowData())
                    .status(e.getStatus())
                    .build());
        }
        return result;
    }

    // Helper methods for Excel parsing
    private Map<String, ClassMaster> loadClassMap(Integer tenantId) {
        List<ClassMaster> classes = classMasterRepository.findByTenantIdAndIsActiveTrue(tenantId);
        Map<String, ClassMaster> map = new HashMap<>();
        for (ClassMaster cm : classes) {
            map.put(cm.getClassName().trim().toLowerCase(), cm);
        }
        return map;
    }

    private Map<String, ClassSection> loadSectionMap(Integer tenantId) {
        List<ClassSection> sections = classSectionRepository.findByTenantIdAndIsActiveTrue(tenantId);
        Map<String, ClassSection> map = new HashMap<>();
        for (ClassSection cs : sections) {
            map.put(cs.getClassId() + "-" + cs.getSectionName().trim().toLowerCase(), cs);
        }
        return map;
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> colMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String colName = getCellValueAsString(cell).trim().toLowerCase();
            if (StringUtils.hasText(colName)) {
                colMap.put(colName, cell.getColumnIndex());
            }
        }
        return colMap;
    }

    private void validateRequiredHeaders(Map<String, Integer> colMap) {
        String[] required = {"first name", "last name", "gender", "date of birth", "class name"};
        for (String req : required) {
            if (!colMap.containsKey(req)) {
                throw new CustomException("file", "Missing required header column in Excel: '" + req + "'");
            }
        }
    }

    private StudentExcelRowDTO parseRow(Row row, Map<String, Integer> colMap, int rowNum) {
        StudentExcelRowDTO dto = new StudentExcelRowDTO();
        dto.setRowNumber(rowNum);
        dto.setFirstName(getCellString(row, colMap.get("first name")));
        dto.setMiddleName(getCellString(row, colMap.get("middle name")));
        dto.setLastName(getCellString(row, colMap.get("last name")));

        String genderStr = getCellString(row, colMap.get("gender"));
        if (StringUtils.hasText(genderStr)) {
            char g = genderStr.trim().toUpperCase().charAt(0);
            dto.setGender(g == 'M' || g == 'F' || g == 'O' ? g : null);
        }

        dto.setDateOfBirth(getCellDate(row, colMap.get("date of birth")));
        dto.setAadharNo(getCellString(row, colMap.get("aadhar no")));
        dto.setEmail(getCellString(row, colMap.get("email")));
        dto.setMobileNo(getCellString(row, colMap.get("mobile no")));
        dto.setAddressLine1(getCellString(row, colMap.get("address")));
        dto.setCity(getCellString(row, colMap.get("city")));
        dto.setState(getCellString(row, colMap.get("state")));
        dto.setPostalCode(getCellString(row, colMap.get("postal code")));
        dto.setFatherName(getCellString(row, colMap.get("father name")));
        dto.setMotherName(getCellString(row, colMap.get("mother name")));
        dto.setParentMobile(getCellString(row, colMap.get("parent mobile")));
        dto.setParentEmail(getCellString(row, colMap.get("parent email")));
        dto.setParentAadhar(getCellString(row, colMap.get("parent aadhar")));
        dto.setClassName(getCellString(row, colMap.get("class name")));
        dto.setSectionName(getCellString(row, colMap.get("section name")));
        dto.setRollNo(getCellString(row, colMap.get("roll no")));

        String hostelStr = getCellString(row, colMap.get("is hostel"));
        if (StringUtils.hasText(hostelStr)) {
            dto.setIsHostel("true".equalsIgnoreCase(hostelStr) || "yes".equalsIgnoreCase(hostelStr) || "1".equals(hostelStr));
        }

        dto.setPassword(getCellString(row, colMap.get("password")));
        return dto;
    }

    private String getCellString(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        return cell != null ? getCellValueAsString(cell).trim() : null;
    }

    private LocalDate getCellDate(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
        }

        String str = getCellValueAsString(cell).trim();
        if (!StringUtils.hasText(str)) return null;

        String[] formats = {"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd"};
        for (String fmt : formats) {
            try {
                return LocalDate.parse(str, DateTimeFormatter.ofPattern(fmt));
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) 
                    ? cell.getDateCellValue().toString() 
                    : String.format("%.0f", cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && StringUtils.hasText(getCellValueAsString(cell))) {
                return false;
            }
        }
        return true;
    }

    private String rowToJsonString(StudentExcelRowDTO row) {
        return String.format("{\"row\":%d,\"name\":\"%s %s\",\"class\":\"%s\",\"section\":\"%s\",\"rollNo\":\"%s\"}",
                row.getRowNumber(), row.getFirstName(), row.getLastName(), row.getClassName(), row.getSectionName(), row.getRollNo());
    }
}
