package com.edunest.service;

import com.edunest.dto.announcement.AnnouncementRequest;
import com.edunest.dto.announcement.AnnouncementResponse;
import com.edunest.entity.Announcement;

import java.util.List;

public interface AnnouncementService {

    List<AnnouncementResponse> getAnnouncements(Integer tenantId);

    boolean saveAnnouncement(Integer tenantId, Integer loginTeacherId, AnnouncementRequest request);

    boolean deleteAnnouncement(Integer tenantId, Integer announcementId);

    /** Sends a push notification to the announcement's audience. Called on publish (immediate or scheduled). */
    void sendAnnouncementPush(Announcement announcement);
}
