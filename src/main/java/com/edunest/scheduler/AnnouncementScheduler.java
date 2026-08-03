package com.edunest.scheduler;

import com.edunest.entity.Announcement;
import com.edunest.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AnnouncementScheduler {

    @Autowired
    AnnouncementRepository announcementRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void publishScheduledAnnouncements() {
        List<Announcement> due = announcementRepository
                .findByStatusAndPublishDateLessThanEqualAndIsActiveTrue("SCHEDULED", LocalDateTime.now());

        for (Announcement announcement : due) {
            announcement.setStatus("PUBLISHED");
            announcementRepository.save(announcement);
            // TODO: send push notification to mobile app users once notification service exists
        }
    }
}
