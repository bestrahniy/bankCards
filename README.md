# Bank Cards Management System

## Project Overview

**Bank Cards Management System** is a comprehensive RESTful API for managing bank cards, users, and financial transactions. The system provides a secure and scalable solution for processing payment operations while maintaining all security requirements.

## Functional Requirements

### Administrator:
- **Creates, blocks, activates, deletes cards**
- **Manages users** (grant admin roles, block/unblock)
- **Views all cards** in the system
- **Reviews notifications** about card creation/block requests

### User:
- **Views their cards** (search + pagination)
- **Requests card blocking**
- **Makes transfers** between their own cards
- **Checks card balance**
- **Views transaction history**

### Notification System:
- Separate database table for notifications
- Admin receives notifications for:
  - New card creation requests
  - Card block requests
- Admin can view and manage active requests

## To Start

### Prerequisites

1. **Java 17** or higher
2. **Maven 3.8+** or **Gradle 7+**
3. **Docker** (recommended) or **PostgreSQL 14+**
4. **Git** for repository cloning

### Installation & Launch

```bash
# 1. Clone the repository
git clone https://github.com/bestrahniy/bankCards
cd bankcards

# 2. Build and launch with Docker Compose
mvn clean package -DskipTests && docker-compose build --no-cache && docker-compose up -d
```
### Application URLs

After successful launch:
- **Application:** http://localhost:8085
- **Swagger UI:** http://localhost:8085/swagger-ui.html

## 📊 Database Schema

### Complete Database Diagram
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────────┐
│     users       │      │   user_role      │      │      roles          │
├─────────────────┤      ├──────────────────┤      ├─────────────────────┤
│ id UUID(PK)     │◄─── ─┤ user_id (FK)     │      │ id BIGINT(PK)       │
│ login VARCHAR(64)│     │ role_id (FK)     ├─────►│ role VARCHAR        │
│ email VARCHAR(64)│     └──────────────────┘      │ is_active BOOLEAN   │
│ password VARCHAR(255)│ ───┐                      └─────────────────────┘
│ created_at TIMESTAMPTZ    ▼
│ is_active BOOLEAN│    ┌──────────────────┐       ┌─────────────────────┐
└─────────────────┘     │  bank_cards      │       │  card_account       │
       │                ├──────────────────┤       ├─────────────────────┤
       │                │ id UUID(PK)      │       │ id UUID(PK)         │
       ▼                │ number TEXT      │◄─── ──┤ current_balance DECIMAL│
┌─────────────────┐     │ cvc2 SMALLINT    │       │ updated_at TIMESTAMPTZ│
│ notifications   │     │ created_at TIMESTAMPTZ│  └─────────────────────┘
├─────────────────┤     │ expires_at TIMESTAMPTZ│       │
│ id BIGINT(PK)   │     │ is_active BOOLEAN│            │
│ event VARCHAR(20)│    │ user_id (FK)     ├────────────┘
│ created_at TIMESTAMPTZ││ card_id (FK)    │            │
│ is_active BOOLEAN│    └──────────────────┘            ▼
│ user_id (FK)    ├──┐                     ┌─────────────────────┐
│ card_id (FK)    │  │                     │payment_transactions │
└─────────────────┘  │                     ├─────────────────────┤
       │             │                     │ id UUID(PK)         │
       ▼             ▼                     │ amount DECIMAL(15,2)│
┌─────────────────┐  ┌──────────────────┐  │ comment TEXT        │
│ refresh_tokens  │  │transactions_type │  │ created_at TIMESTAMPTZ│
├─────────────────┤  ├──────────────────┤  │ sender_card_account_id│
│ id UUID(PK)     │  │ id BIGINT(PK)    │  │ recipient_account_id│
│ hash_token TEXT │  │ type VARCHAR     │  │ type_id (FK)        │
│ created_at DATE │  └──────────────────┘  │ status_id (FK)      │
│ expires_at DATE │          ▲             └─────────────────────┘
│ is_active BOOLEAN│         │                     ▲
│ user_id (FK)    │          └─────────────────────┘
└─────────────────┘                    │
                                       │
                             ┌──────────────────┐
                             │status_transactions│
                             ├──────────────────┤
                             │ id BIGINT(PK)    │
                             │ status VARCHAR   │
                             └──────────────────┘

### Data Encryption
- **Card numbers** encrypted with AES-256-GCM before database storage ()
- **Passwords** hashed using bcrypt algorithm
- **JWT tokens** signed with HMAC-SHA256
- **Refresh tokens** hashed with SHA-256 before storage

### PCI-DSS Compliance
- Card numbers masked for display (`**** **** **** 1234`)
- Full card numbers never logged
- Sensitive data encrypted both at rest and in transit

## Test
for test code you can run tests or go to swagger

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

- **Developer**: Bobkov Ilya 
- **Email**: bobkovilya06@gmail.com
- **tg**: @Bestrahniy

---

*Note: This is a demonstration project for a test assignment. Additional security configuration, monitoring, and backup solutions are required for production environments.*
