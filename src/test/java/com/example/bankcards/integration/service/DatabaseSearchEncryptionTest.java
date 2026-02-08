package com.example.bankcards.integration.service;

import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.CardAccountRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.util.AesEncryption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DatabaseSearchEncryptionTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17")
    )
            .withDatabaseName("search_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private AesEncryption aesEncryption;

    @Autowired
    private BankCardsRepository bankCardsRepository;

    @Autowired
    private CardAccountRepository cardAccountRepository;

    @Autowired
    private UsersRepository usersRepository;

    private UsersEntity testUser;

    @BeforeEach
    void setUp() {
        bankCardsRepository.deleteAll();
        cardAccountRepository.deleteAll();
        usersRepository.deleteAll();

        testUser = UsersEntity.builder()
                .login("testUser")
                .email("test@example.com")
                .password("password")
                .createdAt(Instant.now())
                .isActive(true)
                .build();
        testUser = usersRepository.save(testUser);
    }

    @Test
    void testDirectDatabaseSearchFails_DueToRandomIV() {
        String cardNumber = "1234567812345678";
        BankCardsEntity savedCard = createCard(cardNumber, 1000.0);

        String searchEncrypted = aesEncryption.encrypt(cardNumber);
        Optional<BankCardsEntity> foundCard = bankCardsRepository.findByNumber(searchEncrypted);

        assertFalse(foundCard.isPresent(),
            "Карта не должна находиться прямым поиском из-за разных IV");

        assertEquals(1, bankCardsRepository.count());

        Optional<BankCardsEntity> foundByExactValue = bankCardsRepository.findByNumber(savedCard.getNumber());
        assertTrue(foundByExactValue.isPresent(), 
            "Карта должна находиться по точному значению из БД");
    }

    @Test
    void testEncryptionProducesDifferentResults() {
        String cardNumber = "1111222233334444";
        
        String encrypted1 = aesEncryption.encrypt(cardNumber);
        String encrypted2 = aesEncryption.encrypt(cardNumber);
        String encrypted3 = aesEncryption.encrypt(cardNumber);

        assertNotEquals(encrypted1, encrypted2);
        assertNotEquals(encrypted1, encrypted3);
        assertNotEquals(encrypted2, encrypted3);

        assertEquals(cardNumber, aesEncryption.decrypt(encrypted1));
        assertEquals(cardNumber, aesEncryption.decrypt(encrypted2));
        assertEquals(cardNumber, aesEncryption.decrypt(encrypted3));
    }

    @Test
    void testBruteForceSearchWorks() {
        createCard("1111111111111111", 100.0);
        createCard("2222222222222222", 200.0);
        String targetCardNumber = "3333333333333333";
        BankCardsEntity targetCard = createCard(targetCardNumber, 300.0);
        createCard("4444444444444444", 400.0);

        List<BankCardsEntity> allCards = bankCardsRepository.findAll();
        assertEquals(4, allCards.size());
        
        // Ищем целевую карту перебором
        Optional<BankCardsEntity> foundCard = allCards.stream()
            .filter(card -> targetCardNumber.equals(aesEncryption.decrypt(card.getNumber())))
            .findFirst();
        
        assertTrue(foundCard.isPresent(), "Карта должна быть найдена перебором");
        assertEquals(targetCard.getId(), foundCard.get().getId());
        assertEquals(300.0, foundCard.get().getCardAccountEntity().getCurrentBalance());
    }

    @Test
    void testMultipleSearchAttempts() {
        String cardNumber = "5555666677778888";
        BankCardsEntity savedCard = createCard(cardNumber, 500.0);
        
        int attempts = 50;
        int foundCount = 0;
        
        // Многократно пытаемся найти карту
        for (int i = 0; i < attempts; i++) {
            String searchEncrypted = aesEncryption.encrypt(cardNumber);
            Optional<BankCardsEntity> foundCard = bankCardsRepository.findByNumber(searchEncrypted);
            if (foundCard.isPresent()) {
                foundCount++;
            }
        }
        
        // Карта должна находиться очень редко (практически никогда)
        assertTrue(foundCount < 3, 
            "Карта должна находиться не более 2 раз из 50 попыток. Найдено: " + foundCount);
        
        // Но она всегда есть в базе
        assertEquals(1, bankCardsRepository.count());
    }

    @Test
    void testFindAllAndDecrypt() {
        // Создаем карты с известными номерами
        String[] cardNumbers = {"1111111111111111", "2222222222222222", "3333333333333333"};
        Double[] balances = {100.0, 200.0, 300.0};
        
        for (int i = 0; i < cardNumbers.length; i++) {
            createCard(cardNumbers[i], balances[i]);
        }
        
        // Получаем все карты
        List<BankCardsEntity> allCards = bankCardsRepository.findAll();
        assertEquals(3, allCards.size());
        
        // Проверяем, что все номера можно восстановить
        for (int i = 0; i < allCards.size(); i++) {
            BankCardsEntity card = allCards.get(i);
            String decrypted = aesEncryption.decrypt(card.getNumber());
            
            boolean numberExists = false;
            for (String expectedNumber : cardNumbers) {
                if (expectedNumber.equals(decrypted)) {
                    numberExists = true;
                    break;
                }
            }
            
            assertTrue(numberExists, "Декодированный номер должен быть в списке: " + decrypted);
        }
    }

    @Test
    void testSaveAndImmediateSearch() {
        String cardNumber = "1234123412341234";
        
        // Сохраняем карту
        BankCardsEntity savedCard = createCard(cardNumber, 1000.0);
        String savedEncrypted = savedCard.getNumber();
        
        // Немедленно пытаемся найти
        String searchEncrypted = aesEncryption.encrypt(cardNumber);
        Optional<BankCardsEntity> foundCard = bankCardsRepository.findByNumber(searchEncrypted);
        
        // Карта не должна находиться
        assertFalse(foundCard.isPresent());
        
        // Но если использовать значение из БД - найдет
        Optional<BankCardsEntity> foundBySavedValue = bankCardsRepository.findByNumber(savedEncrypted);
        assertTrue(foundBySavedValue.isPresent());
        
        // И дешифровка дает правильный номер
        assertEquals(cardNumber, aesEncryption.decrypt(savedEncrypted));
        assertEquals(cardNumber, aesEncryption.decrypt(searchEncrypted));
    }

    @Test
    void testTransactionScenario() {
        // Симуляция сценария перевода
        String fromCardNumber = "1111222233334444";
        String toCardNumber = "5555666677778888";
        
        BankCardsEntity fromCard = createCard(fromCardNumber, 1000.0);
        BankCardsEntity toCard = createCard(toCardNumber, 500.0);
        
        // Попытка найти карты для перевода (как в сервисе)
        String searchFromEncrypted = aesEncryption.encrypt(fromCardNumber);
        String searchToEncrypted = aesEncryption.encrypt(toCardNumber);
        
        Optional<BankCardsEntity> foundFrom = bankCardsRepository.findByNumber(searchFromEncrypted);
        Optional<BankCardsEntity> foundTo = bankCardsRepository.findByNumber(searchToEncrypted);
        
        // Обе карты не должны находиться
        assertFalse(foundFrom.isPresent(), "Карта отправителя не найдена");
        assertFalse(foundTo.isPresent(), "Карта получателя не найдена");
        
        // Но они есть в базе
        assertEquals(2, bankCardsRepository.count());
        
        // И их можно найти перебором
        List<BankCardsEntity> allCards = bankCardsRepository.findAll();
        Optional<BankCardsEntity> bruteForceFrom = findCardByNumber(allCards, fromCardNumber);
        Optional<BankCardsEntity> bruteForceTo = findCardByNumber(allCards, toCardNumber);
        
        assertTrue(bruteForceFrom.isPresent(), "Карта отправителя найдена перебором");
        assertTrue(bruteForceTo.isPresent(), "Карта получателя найдена перебором");
    }

    @Test
    void testEmptyDatabase() {
        // Поиск в пустой базе
        String searchEncrypted = aesEncryption.encrypt("1234567890123456");
        Optional<BankCardsEntity> foundCard = bankCardsRepository.findByNumber(searchEncrypted);
        
        assertFalse(foundCard.isPresent());
        assertEquals(0, bankCardsRepository.count());
    }

    @Test
    void testManyCardsSearch() {
        // Создаем 20 карт
        for (int i = 0; i < 20; i++) {
            String cardNumber = String.format("%016d", 1000000000000000L + i);
            createCard(cardNumber, 100.0 + i);
        }
        
        // Ищем конкретную карту
        String targetNumber = "1000000000000005";
        String searchEncrypted = aesEncryption.encrypt(targetNumber);
        
        Optional<BankCardsEntity> directSearch = bankCardsRepository.findByNumber(searchEncrypted);
        assertFalse(directSearch.isPresent(), "Прямой поиск не должен находить карту");
        
        // Но перебором находим
        List<BankCardsEntity> allCards = bankCardsRepository.findAll();
        Optional<BankCardsEntity> bruteForceSearch = findCardByNumber(allCards, targetNumber);
        
        assertTrue(bruteForceSearch.isPresent(), "Карта должна находиться перебором");
        assertEquals(105.0, bruteForceSearch.get().getCardAccountEntity().getCurrentBalance());
    }

    // Вспомогательные методы
    private BankCardsEntity createCard(String cardNumber, Double balance) {
        CardAccountEntity cardAccount = CardAccountEntity.builder()
                .currentBalance(balance)
                .updatedAt(Instant.now())
                .build();
        cardAccount = cardAccountRepository.save(cardAccount);
        
        String encryptedCardNumber = aesEncryption.encrypt(cardNumber);
        
        BankCardsEntity card = BankCardsEntity.builder()
                .number(encryptedCardNumber)
                .cvc2(123)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(365 * 5, ChronoUnit.DAYS))
                .isActive(true)
                .user(testUser)
                .cardAccountEntity(cardAccount)
                .build();
        
        cardAccount.setBankCardsEntity(card);
        return bankCardsRepository.save(card);
    }

    private Optional<BankCardsEntity> findCardByNumber(List<BankCardsEntity> cards, String cardNumber) {
        return cards.stream()
            .filter(card -> cardNumber.equals(aesEncryption.decrypt(card.getNumber())))
            .findFirst();
    }
}