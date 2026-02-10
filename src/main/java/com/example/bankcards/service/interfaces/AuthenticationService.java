package com.example.bankcards.service.interfaces;

import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.response.UserResponse;

public interface AuthenticationService {

    UserResponse authenticationUser(AuthorizationRequest authorizationRequest);

}
