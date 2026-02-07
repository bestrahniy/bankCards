package com.example.bankcards.dto.request;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateRefreshTokenRequest {

    @NonNull
    private String hashToken;

    @NonNull
    private Date createdAt;

    @NonNull
    private Date expiredAt;

}
