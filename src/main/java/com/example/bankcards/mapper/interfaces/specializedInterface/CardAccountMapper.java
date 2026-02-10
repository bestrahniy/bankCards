package com.example.bankcards.mapper.interfaces.specializedInterface;

import com.example.bankcards.mapper.interfaces.mainInterface.EntityCreator;
import com.example.bankcards.model.entity.CardAccountEntity;

public interface CardAccountMapper extends EntityCreator<CardAccountEntity> {

    @Override
    CardAccountEntity toEntity();

}
