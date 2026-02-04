package com.example.bankcards;

import org.springframework.boot.SpringApplication;

public class TestBankcardsApplication {

	public static void main(String[] args) {
		SpringApplication.from(BankcardsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
