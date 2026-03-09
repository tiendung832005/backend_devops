# Rikkei Backend - Spring Boot Application

Backend API cho ứng dụng Rikkei, được triển khai với Docker và CI/CD.

## 🚀 Quick Start

### Development (Local)

```bash
# Chạy với database local
./gradlew bootRun
```

### Production (Docker)

```bash
# Build và chạy với Docker Compose
docker-compose up -d

# Xem logs
docker-compose logs -f backend

# Stop
docker-compose down
```

## 📚 Documentation

Xem chi tiết trong [DEPLOYMENT.md](DEPLOYMENT.md) - Hướng dẫn đầy đủ về:

- Cài đặt môi trường server
- Cấu hình GitHub Actions CI/CD
- Triển khai lên AWS EC2
- Troubleshooting

## 🛠️ Tech Stack

- **Java 21** - OpenJDK
- **Spring Boot** - Backend framework
- **PostgreSQL** - Database
- **Docker** - Containerization
- **GitHub Actions** - CI/CD
- **Nginx** - Reverse proxy

## 📦 Files Structure

```
.
├── Dockerfile                  # Multi-stage build
├── docker-compose.yml          # Local development
├── docker-compose.prod.yml     # Production deployment
├── nginx.conf                  # Nginx reverse proxy config
├── .github/workflows/
│   └── deploy.yml             # CI/CD pipeline
├── src/
│   └── main/resources/
│       ├── application.yml         # Dev config (localhost)
│       └── application-prod.yml    # Prod config (container)
└── DEPLOYMENT.md              # Full deployment guide
```

## 🔗 API Endpoints

- Health Check: `http://localhost:8080/actuator/health`
- API Base: `http://localhost:8080/api`

## 👨‍💻 Development

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Create JAR
./gradlew bootJar
```

## 📝 Environment Variables

| Variable                   | Description               | Default                                      |
| -------------------------- | ------------------------- | -------------------------------------------- |
| SPRING_PROFILES_ACTIVE     | Active profile (dev/prod) | dev                                          |
| SPRING_DATASOURCE_URL      | Database URL              | jdbc:postgresql://localhost:5432/rikkei_prod |
| SPRING_DATASOURCE_USERNAME | DB username               | postgres                                     |
| SPRING_DATASOURCE_PASSWORD | DB password               | 123456                                       |

## 🔐 Security Notes

- Database port (5432) không được expose ra ngoài trong production
- Sử dụng Docker network internal cho các service
- Mật khẩu nên được lưu trong environment variables hoặc secrets

---

**Server**: AWS EC2 (3.107.235.9)  
**Database**: PostgreSQL 16  
**CI/CD**: GitHub Actions
