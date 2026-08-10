package com.edunest.scheduler;

import com.edunest.entity.Announcement;
import com.edunest.repository.AnnouncementRepository;
import com.edunest.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class AnnouncementScheduler {

    @Autowired
    AnnouncementRepository announcementRepository;

    @Autowired
    AnnouncementService announcementService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void publishScheduledAnnouncements() {
        List<Announcement> due = announcementRepository
                .findByStatusAndPublishDateLessThanEqualAndIsActiveTrue("SCHEDULED", LocalDate.now());

        for (Announcement announcement : due) {
            announcement.setStatus("PUBLISHED");
            announcementRepository.save(announcement);
            announcementService.sendAnnouncementPush(announcement);
        }
    }
}
