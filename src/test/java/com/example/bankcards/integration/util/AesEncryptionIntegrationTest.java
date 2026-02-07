package com.example.bankcards.integration.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.util.AesEncryption;
import com.example.bankcards.util.AesHelper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@Testcontainers
class AesEncryptionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17")
    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AesEncryption aesEncryption;

    @Mock
    private BankCardsRepository bankCardsRepository;

    private AesHelper aesHelper;
    
    private static final String TEST_CARD_NUMBER = "1234567890123456";

    @BeforeEach
    void setUp() {
        aesHelper = new AesHelper(aesEncryption, bankCardsRepository);
    }

    @Test
    void testEncryptDecrypt_Success() {
        String originalCardNumber = TEST_CARD_NUMBER;

        String encrypted = aesEncryption.encrypt(originalCardNumber);
        String decrypted = aesEncryption.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(originalCardNumber, encrypted);
        assertEquals(originalCardNumber, decrypted);

        assertTrue(encrypted.matches("^[A-Za-z0-9+/]+=*$"));
    }

    @Test
    void testEncryptDecrypt_DifferentIVsProduceDifferentOutput() {
        String cardNumber = TEST_CARD_NUMBER;

        String encrypted1 = aesEncryption.encrypt(cardNumber);
        String encrypted2 = aesEncryption.encrypt(cardNumber);

        assertNotEquals(encrypted1, encrypted2, "Different IVs should produce different encrypted output");

        assertEquals(cardNumber, aesEncryption.decrypt(encrypted1));
        assertEquals(cardNumber, aesEncryption.decrypt(encrypted2));
    }

    @Test
    void testEncrypt_EmptyString() {
        // Given
        String emptyCardNumber = "";

        // When
        String encrypted = aesEncryption.encrypt(emptyCardNumber);
        String decrypted = aesEncryption.decrypt(encrypted);

        // Then
        assertEquals(emptyCardNumber, decrypted);
    }


    @Test
    void testCheckActiveCard() {
        BankCardsEntity activeCard = mock(BankCardsEntity.class);
        when(activeCard.getIsActive()).thenReturn(true);
        
        BankCardsEntity inactiveCard = mock(BankCardsEntity.class);
        when(inactiveCard.getIsActive()).thenReturn(false);

        Boolean activeResult = aesHelper.checkActiveCard(activeCard);
        Boolean inactiveResult = aesHelper.checkActiveCard(inactiveCard);

        assertTrue(activeResult);
        assertFalse(inactiveResult);
    }

    @Test
    void testCheckExpiresCard() {
        BankCardsEntity notExpiredCard = mock(BankCardsEntity.class);
        when(notExpiredCard.getExpiresAt()).thenReturn(
            LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()
        );
        
        BankCardsEntity expiredCard = mock(BankCardsEntity.class);
        when(expiredCard.getExpiresAt()).thenReturn(
            LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()
        );

        Boolean notExpiredResult = aesHelper.checkExpiresCard(notExpiredCard);
        Boolean expiredResult = aesHelper.checkExpiresCard(expiredCard);

        assertFalse(notExpiredResult, "Card should not be expired");
        assertTrue(expiredResult, "Card should be expired");
    }

    @Test
    void testGetMaskedCardNumber_ValidCard() {
        String encryptedData = aesEncryption.encrypt(TEST_CARD_NUMBER);

        String masked = aesHelper.getMaskedCardNumber(encryptedData);

        assertEquals("**** **** **** 3456", masked);
    }

    @Test
    void testGetMaskedCardNumber_ShortCardNumber() {
        String shortCard = "123";
        String encryptedShortCard = aesEncryption.encrypt(shortCard);

        String masked = aesHelper.getMaskedCardNumber(encryptedShortCard);

        assertEquals("****", masked);
    }

    @Test
    void testGetMaskedCardNumber_16DigitCard() {
        String sixteenDigitCard = "4111111111111111";
        String encryptedCard = aesEncryption.encrypt(sixteenDigitCard);

        String masked = aesHelper.getMaskedCardNumber(encryptedCard);

        assertEquals("**** **** **** 1111", masked);
    }

    @Test
    void testGetMaskedCardNumber_19DigitCard() {
        String nineteenDigitCard = "1234567890123456789";
        String encryptedCard = aesEncryption.encrypt(nineteenDigitCard);

        String masked = aesHelper.getMaskedCardNumber(encryptedCard);

        assertEquals("**** **** **** 6789", masked);
    }

    private BankCardsEntity createMockBankCard(boolean isActive, boolean isExpired) {
        BankCardsEntity entity = mock(BankCardsEntity.class);
        when(entity.getIsActive()).thenReturn(isActive);
        
        LocalDateTime expiresAt = isExpired ?
            LocalDateTime.now().minusDays(1) :
            LocalDateTime.now().plusDays(1);
        
        when(entity.getExpiresAt()).thenReturn(
            expiresAt.atZone(ZoneId.systemDefault()).toInstant()
        );
        
        return entity;
    }

}