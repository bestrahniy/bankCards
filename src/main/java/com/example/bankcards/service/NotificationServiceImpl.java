package com.example.bankcards.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.mapper.PageMapperImpl;
import com.example.bankcards.model.entity.NotificationEntity;
import com.example.bankcards.repository.NotificationRepository;
import com.example.bankcards.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Service for managing notifications.
 * 
 * Provides functionality for retrieving and managing user notifications
 * with pagination support. Notifications are system messages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final PageMapperImpl pageMapper;
    private final NotificationRepository notificationRepository;

    /**
     * Retrieves paginated list of active notifications users
     * 
     * @param page zero-based page index
     * @param size number of items per page
     * @return paginated response with notification details
     */
    public PageResponse<NotificationResponse> getUserActiveNotifications(int page, int size) {
        log.debug("Fetching active notifications page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        log.trace("Created pageable with sort: createdAt DESC");

        Page<NotificationEntity> pageResult = notificationRepository.findAllByIsActiveTrue(pageable);
        log.debug("Found {} active notifications on page {} of {}", 
            pageResult.getNumberOfElements(), page, pageResult.getTotalPages());
        
        log.info("Active notifications retrieved successfully");
        return pageMapper.toDtoNotification(pageResult);
    }

}
