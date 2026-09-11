# OmniSport 🥊
### About
Backend API for a comprehensive ERP system for managing a martial arts club. Designed to provide full control over attendance, payments, and the club's community.
### Tech Stack
- Java 17
- Spring Boot 3.3
- PostgreSQL 15
- Flyway 10.15
- Hibernate
- Maven
- Docker

### Features 
- JWT-based security (Spring Security)
- Result pagination
- Database migrations with Flyway
- DTO pattern implemented
- Relations between entities
- Authorization and login
- Global exception handler

### Getting started
1. Clone the repository
2. Build and start the application
```bash
docker compose up --build -d 
```
3. The application will be up running at `http://localhost:8080`
4. To stop the containers, run:
```bash
docker compose down 
```

### Database Structure
![OmniSport](docs/omnisport2.drawio.png)