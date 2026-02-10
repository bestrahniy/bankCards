package com.example.bankcards.service.interfaces;

import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;

public interface UserRegistrationService {

    UserResponse registerUser(RegistrationRequest registrationRequest);

}
