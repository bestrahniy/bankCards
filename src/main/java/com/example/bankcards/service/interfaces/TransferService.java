package com.example.bankcards.service.interfaces;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CreateTransactionResponse;

public interface TransferService {

    CreateTransactionResponse transfer(TransferRequest transferRequest) throws IllegalAccessException;

}
