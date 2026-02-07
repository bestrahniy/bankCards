package com.example.bankcards.service;
import com.example.bankcards.util.AesEncryption;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.UserNotActiveException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.BankCardMapper;
import com.example.bankcards.mapper.CardAccountMapper;
import com.example.bankcards.mapper.PageMapper;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.NotificationEntity;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.CardAccountRepository;
import com.example.bankcards.repository.NotificationRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AesEncryption aesEncryption;

    private final UserMapper userMapper;

    private final UsersRepository usersRepository;

    private final RoleRepository roleRepository;

    private final NotificationRepository notificationRepository;

    private final PageMapper pageMapper;

    private final CardAccountMapper cardAccountMapper;

    private final CardAccountRepository cardAccountRepository;

    private final BankCardMapper bankCardMapper;

    private final BankCardsRepository bankCardsRepository;

    @SuppressWarnings("null")
    @Transactional
    public UserResponse grantAdminRole(UUID userId) {
        UsersEntity user = usersRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Set<RoleEntity> roles = user.getRoles();

        List<RoleEntity> adminRoles = roleRepository.findByRole(RoleType.ADMIN);

        if (adminRoles.isEmpty()) {
            RoleEntity newAdminRole = RoleEntity.builder()
                    .role(RoleType.ADMIN)
                    .build();
            RoleEntity savedAdminRole = roleRepository.save(newAdminRole);
            roles.add(savedAdminRole);
        } else {
            roles.add(adminRoles.get(0));
        }

        user.setRoles(roles);
        usersRepository.save(user);

        return userMapper.toDto(user);
    }

    public PageResponse<NotificationResponse> getUserActiveNotifications(
            int page, int size) {
        
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<NotificationEntity> pageResult = notificationRepository.findAllByIsActiveTrue(pageable);
        
        return pageMapper.toDto(pageResult);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CreateCardResponse createCard(UUID userId) {
        UsersEntity user = usersRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        if (!checkUser(user)) {
            throw new UserNotActiveException(userId);
        }

        CardAccountEntity cardAccount = cardAccountMapper.toEntity();
        cardAccount = cardAccountRepository.save(cardAccount);

        BankCardsEntity bankCard = bankCardMapper.toEntity(cardAccount);
        bankCard.setUser(user);
        bankCard.setCardAccountEntity(cardAccount);

        cardAccount.setBankCardsEntity(bankCard);

        bankCard = bankCardsRepository.save(bankCard);

        if (user.getBankCardsEntities() == null) {
            user.setBankCardsEntities(new ArrayList<>());
        }

        user.getBankCardsEntities().add(bankCard);

        return bankCardMapper.toDto(bankCard);
    }

    private Boolean checkUser(UsersEntity usersEntity) {
        return usersEntity.isActive();
    }
}
