# Bank Cards Management System
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

## enviroment
All configuration files and environment variables are included in the repository since this is a test assignment and I want to showcase them.

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

### Data Encryption
- **Card numbers** encrypted with AES-256-GCM before database storage ()
- **Passwords** hashed using bcrypt algorithm

## Test
for test code you can run tests or go to swagger

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

- **Developer**: Bobkov Ilya 
- **Email**: bobkovilya06@gmail.com
- **tg**: @Bestrahniy
