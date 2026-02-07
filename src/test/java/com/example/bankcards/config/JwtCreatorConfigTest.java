package com.example.bankcards.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.model.entity.UsersEntity;

@Component
public class JwtCreatorConfigTest {

    @Autowired
    JwtCreator jwtCreator;

    public String createToken(UsersEntity usersEntity) {
        return jwtCreator.createJwt(usersEntity);
    }

}
