# ✅ CHECKLIST BÀI THI - CI/CD DEPLOYMENT

## 📋 PHẦN 1: CẤU HÌNH SERVER & DATABASE (30 điểm)

### 1.1 Cài đặt môi trường

- ✅ **Docker đã cài**: Chạy `setup-server.sh` trên server
- ✅ **Docker Compose đã cài**: Version 1.29+
- 📝 **Command**:
  ```bash
  ssh ubuntu@3.107.235.9
  bash setup-server.sh
  ```

### 1.2 Database PostgreSQL

- ✅ **Container**: Đã cấu hình trong `docker-compose.yml` (postgres:16-alpine)
- ✅ **Database name**: `rikkei_prod`
- ✅ **Volume**: `postgres_data` - persistent storage
- ✅ **Kết nối nội bộ**: Port 5432 KHÔNG expose ra ngoài
- ✅ **Health check**: Có trong docker-compose

📂 **File liên quan**:

- `docker-compose.yml` (lines 7-21)
- `docker-compose.prod.yml` (lines 7-21)

---

## 📋 PHẦN 2: ĐÓNG GÓI VÀ TRIỂN KHAI (30 điểm)

### 2.1 Backend Dockerfile

- ✅ **Multi-stage build**:
  - Stage 1: Build với Gradle + JDK 21
  - Stage 2: Runtime với OpenJDK 21 slim
- ✅ **OpenJDK 17+**: Sử dụng OpenJDK 21
- ✅ **Security**: Non-root user (spring:spring)
- ✅ **Health check**: Curl health endpoint

📂 **File**: `Dockerfile` (42 lines)

### 2.2 Cấu hình Database Connection

- ✅ **Development**: `application.yml` - localhost:5432
- ✅ **Production**: `application-prod.yml` - postgres:5432
- ✅ **Docker Network**: Container connect via `rikkei_network`
- ✅ **Environment variables**: Cấu hình trong docker-compose

📂 **Files**:

- `src/main/resources/application.yml`
- `src/main/resources/application-prod.yml`

### 2.3 Orchestration

- ✅ **docker-compose.yml**: Quản lý Backend + PostgreSQL
- ✅ **Networks**: Internal network `rikkei_network`
- ✅ **Volumes**: Persistent data cho database
- ✅ **Depends_on**: Backend đợi PostgreSQL healthy
- ✅ **Restart policy**: unless-stopped

📂 **Files**:

- `docker-compose.yml` (dev)
- `docker-compose.prod.yml` (production)

---

## 📋 PHẦN 3: WEB SERVER & REVERSE PROXY (20 điểm)

### 3.1 Nginx Configuration

- ✅ **Reverse Proxy**: `/api` → `http://localhost:8080`
- ✅ **Gzip compression**: Enabled với config tối ưu
- ✅ **Upload limit**: `client_max_body_size 50M`
- ✅ **Headers**: Forwarding headers đầy đủ
- ✅ **WebSocket support**: Cấu hình cho /ws endpoint

📂 **File**: `nginx.conf` (80 lines)

### 3.2 Tối ưu hóa

- ✅ **Gzip types**: text/plain, css, js, json, xml
- ✅ **Gzip compression level**: 6
- ✅ **Proxy timeouts**: 60s
- ✅ **Buffering**: Configured
- ✅ **Logging**: access.log + error.log

---

## 📋 PHẦN 4: CI/CD PIPELINE (20 điểm)

### 4.1 GitHub Actions Workflow

- ✅ **Trigger**: Push to main/master branch
- ✅ **Manual trigger**: workflow_dispatch
- ✅ **File**: `.github/workflows/deploy.yml`

📂 **File**: `.github/workflows/deploy.yml` (80 lines)

### 4.2 Pipeline Flow

#### Step 1: Build

- ✅ **Checkout code**: actions/checkout@v3
- ✅ **Setup JDK 21**: actions/setup-java@v3
- ✅ **Build Docker image**: Multi-stage Dockerfile

#### Step 2: Push

- ✅ **Login Docker Hub**: docker/login-action@v2
- ✅ **Push image**:
  - Tag `latest`
  - Tag `${{ github.sha }}`
- ✅ **Registry**: Docker Hub (hoặc private registry)

#### Step 3: Deploy

- ✅ **SSH to server**: appleboy/ssh-action@master
- ✅ **Pull latest image**: docker pull
- ✅ **Update services**: docker-compose down + up -d
- ✅ **Health check**: Curl health endpoint
- ✅ **Configure Nginx**: Auto copy config

### 4.3 Secrets Required

- ✅ `SSH_PRIVATE_KEY`: Private key SSH
- ✅ `DOCKER_USERNAME`: Docker Hub username
- ✅ `DOCKER_PASSWORD`: Docker Hub password

---

## 📦 PHẦN 5: SẢN PHẨM BÀN GIAO

### 5.1 Files cần nộp

- ✅ `Dockerfile` - Multi-stage build
- ✅ `docker-compose.yml` - Local development
- ✅ `docker-compose.prod.yml` - Production
- ✅ `nginx.conf` - Reverse proxy config
- ✅ `.github/workflows/deploy.yml` - CI/CD pipeline
- ✅ `.dockerignore` - Optimize build
- ✅ `DEPLOYMENT.md` - Tài liệu triển khai
- ✅ `README.md` - Hướng dẫn sử dụng
- ✅ `setup-server.sh` - Script cài đặt server

### 5.2 URL truy cập

- 🌐 **Direct IP**: `http://3.107.235.9/api`
- 🌐 **Health Check**: `http://3.107.235.9/api/actuator/health`
- 🌐 **With Domain**: `http://domain.rikkei.edu.vn/api` (sau khi config DNS)

### 5.3 Repository

- ✅ All files committed to Git
- ✅ GitHub Actions workflow enabled
- ✅ Secrets configured

---

## 🚀 HƯỚNG DẪN TRIỂN KHAI NHANH

### Bước 1: Setup Server (Chỉ lần đầu)

```bash
ssh ubuntu@3.107.235.9
bash setup-server.sh
# Copy SSH private key hiển thị vào GitHub Secrets
```

### Bước 2: Cấu hình GitHub Secrets

Vào: Repository → Settings → Secrets → Actions

- Add `SSH_PRIVATE_KEY`
- Add `DOCKER_USERNAME`
- Add `DOCKER_PASSWORD`

### Bước 3: Deploy

```bash
# Trên máy local
git add .
git commit -m "Complete CI/CD deployment"
git push origin main
```

### Bước 4: Theo dõi

- Vào tab **Actions** trên GitHub
- Xem quá trình build & deploy
- Chờ ✅ Success

### Bước 5: Kiểm tra

```bash
# Test API
curl http://3.107.235.9/api/actuator/health

# SSH vào server xem logs
ssh ubuntu@3.107.235.9
cd /home/ubuntu/rikkei-app
docker-compose ps
docker-compose logs -f backend
```

---

## 📊 TIÊU CHÍ CHẤM ĐIỂM

| Tiêu chí                       | Điểm    | Hoàn thành  |
| ------------------------------ | ------- | ----------- |
| **Server & Database**          |         |             |
| - Docker & Docker Compose      | 5       | ✅          |
| - PostgreSQL container         | 10      | ✅          |
| - Volume persistent            | 5       | ✅          |
| - Kết nối nội bộ               | 10      | ✅          |
| **Đóng gói & Triển khai**      |         |             |
| - Dockerfile multi-stage       | 15      | ✅          |
| - Config database qua network  | 10      | ✅          |
| - docker-compose orchestration | 5       | ✅          |
| **Web Server**                 |         |             |
| - Nginx reverse proxy          | 10      | ✅          |
| - Gzip & upload limit          | 10      | ✅          |
| **CI/CD**                      |         |             |
| - GitHub Actions workflow      | 10      | ✅          |
| - Build + Push + Deploy flow   | 10      | ✅          |
| **TỔNG**                       | **100** | **✅ 100%** |

---

## 🎯 GHI CHÚ QUAN TRỌNG

### Trước khi nộp bài:

1. ✅ Đảm bảo tất cả files đã commit và push
2. ✅ GitHub Actions chạy thành công (màu xanh ✅)
3. ✅ API truy cập được qua URL
4. ✅ Database có dữ liệu persistent (test restart container)
5. ✅ Nginx reverse proxy hoạt động

### Demo cho giáo viên:

1. 📺 Mở tab GitHub Actions → Show workflow success
2. 📺 Truy cập URL health check: `http://3.107.235.9/api/actuator/health`
3. 📺 SSH vào server: `docker-compose ps` → Show containers running
4. 📺 Show logs: `docker-compose logs backend`
5. 📺 Show Nginx config: `cat /etc/nginx/sites-available/rikkei`

### Files quan trọng nhất:

1. `Dockerfile` - Đóng gói application
2. `docker-compose.yml` - Orchestration
3. `.github/workflows/deploy.yml` - CI/CD pipeline
4. `nginx.conf` - Reverse proxy
5. `DEPLOYMENT.md` - Tài liệu chi tiết

---

## 🐛 TROUBLESHOOTING

### Nếu build fail:

```bash
# Xem log GitHub Actions
# Kiểm tra Dockerfile syntax
# Test build local: docker build -t test .
```

### Nếu deploy fail:

```bash
# Kiểm tra SSH_PRIVATE_KEY đúng chưa
# Kiểm tra server có Docker chưa
# Xem log chi tiết trong Actions tab
```

### Nếu API không truy cập được:

```bash
ssh ubuntu@3.107.235.9
docker-compose ps  # Kiểm tra containers
docker-compose logs backend  # Xem lỗi
curl http://localhost:8080/actuator/health  # Test direct
```

---

**✅ HOÀN TẤT CHECKLIST - SẴN SÀNG NỘP BÀI!**
