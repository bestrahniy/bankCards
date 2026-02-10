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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final PageMapperImpl pageMapper;

    private final NotificationRepository notificationRepository;

    public PageResponse<NotificationResponse> getUserActiveNotifications(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<NotificationEntity> pageResult = notificationRepository.findAllByIsActiveTrue(pageable);
        
        return pageMapper.toDtoNotification(pageResult);
    }
}
