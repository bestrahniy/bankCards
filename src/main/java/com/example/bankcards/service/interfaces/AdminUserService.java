package com.example.bankcards.service.interfaces;

import java.util.UUID;
import com.example.bankcards.dto.response.UserActiveResponse;
import com.example.bankcards.dto.response.UserResponse;

public interface AdminUserService {

    UserResponse grantAdminRole(UUID userId);

    UserActiveResponse blockUser(UUID userId);

    UserActiveResponse unblockUser(UUID userId);

}
