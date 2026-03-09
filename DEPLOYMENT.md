# Hướng dẫn Triển khai CI/CD Backend Spring Boot lên AWS

## 📋 Tổng quan

Dự án này triển khai Backend Spring Boot lên AWS EC2 sử dụng:
- **Docker & Docker Compose**: Container hóa ứng dụng
- **PostgreSQL**: Database chạy trong container
- **Nginx**: Reverse proxy
- **GitHub Actions**: CI/CD tự động

---

## 🛠️ Chuẩn bị trên Server AWS (3.107.235.9)

### 1. Cài đặt Docker & Docker Compose

```bash
# SSH vào server
ssh ubuntu@3.107.235.9

# Cài Docker
sudo apt update
sudo apt install -y docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker

# Thêm user vào group docker
sudo usermod -aG docker ubuntu
newgrp docker

# Kiểm tra
docker --version
docker-compose --version
```

### 2. Cài đặt Nginx

```bash
# Cài Nginx
sudo apt install -y nginx

# Start và enable Nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 3. Tạo thư mục triển khai

```bash
mkdir -p /home/ubuntu/rikkei-app
cd /home/ubuntu/rikkei-app
```

---

## 🔑 Cấu hình GitHub Secrets

Vào repository GitHub → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Thêm các secrets sau:

| Secret Name | Giá trị | Mô tả |
|------------|---------|-------|
| `SSH_PRIVATE_KEY` | `[Private key của bạn]` | SSH key để kết nối server |
| `DOCKER_USERNAME` | `[Username Docker Hub]` | Tài khoản Docker Hub |
| `DOCKER_PASSWORD` | `[Password Docker Hub]` | Mật khẩu Docker Hub |

### Cách tạo SSH Private Key:

```bash
# Trên server AWS
ssh-keygen -t rsa -b 4096 -C "github-actions"

# Copy private key
cat ~/.ssh/id_rsa
# Paste vào GitHub Secret: SSH_PRIVATE_KEY

# Copy public key vào authorized_keys
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

---

## 📦 Các file đã tạo

### 1. **Dockerfile** - Multi-stage build cho Spring Boot
- Stage 1: Build JAR với Gradle
- Stage 2: Runtime với OpenJDK 21

### 2. **docker-compose.yml** - Orchestration
- Service `postgres`: PostgreSQL 16 với volume persistent
- Service `backend`: Spring Boot application
- Network: `rikkei_network` (internal)
- Database port **KHÔNG** expose ra ngoài (bảo mật)

### 3. **nginx.conf** - Reverse Proxy
- `http://domain.rikkei.edu.vn/api` → Backend
- Gzip compression
- Upload limit: 50MB
- WebSocket support

### 4. **.github/workflows/deploy.yml** - CI/CD Pipeline
- Trigger: Push to main/master
- Build Docker image
- Push to Docker Hub
- Deploy to AWS server

### 5. **application-prod.yml** - Production config
- Database URL: `postgres:5432` (container name)

---

## 🚀 Triển khai

### Bước 1: Đẩy code lên GitHub

```bash
# Thêm các file mới
git add Dockerfile docker-compose.yml nginx.conf .dockerignore
git add .github/workflows/deploy.yml
git add src/main/resources/application-prod.yml

# Commit
git commit -m "Add Docker & CI/CD configuration"

# Push lên GitHub
git push origin main
```

### Bước 2: GitHub Actions tự động chạy

- Vào tab **Actions** trên GitHub để theo dõi quá trình deploy
- Pipeline sẽ:
  1. ✅ Build Docker image
  2. ✅ Push lên Docker Hub
  3. ✅ Copy docker-compose.yml lên server
  4. ✅ Pull image mới và restart containers
  5. ✅ Cấu hình Nginx

### Bước 3: Cấu hình Domain (trên server)

```bash
# SSH vào server
ssh ubuntu@3.107.235.9

# Edit file nginx.conf đã copy
cd /home/ubuntu/rikkei-app
sudo nano nginx.conf
# Thay "domain.rikkei.edu.vn" bằng domain thực tế

# Copy vào Nginx sites
sudo cp nginx.conf /etc/nginx/sites-available/rikkei
sudo ln -s /etc/nginx/sites-available/rikkei /etc/nginx/sites-enabled/

# Test và reload Nginx
sudo nginx -t
sudo systemctl reload nginx
```

---

## ✅ Kiểm tra triển khai

### 1. Kiểm tra containers

```bash
ssh ubuntu@3.107.235.9
cd /home/ubuntu/rikkei-app
docker-compose ps
```

Kết quả mong đợi:
```
NAME               STATUS         PORTS
rikkei_backend     Up (healthy)   0.0.0.0:8080->8080/tcp
rikkei_postgres    Up (healthy)
```

### 2. Kiểm tra logs

```bash
# Xem log backend
docker-compose logs -f backend

# Xem log database
docker-compose logs -f postgres
```

### 3. Test API

```bash
# Health check
curl http://localhost:8080/actuator/health

# Qua Nginx
curl http://3.107.235.9/api/actuator/health

# Hoặc từ browser
http://domain.rikkei.edu.vn/api/actuator/health
```

---

## 🔧 Các lệnh hữu ích

### Quản lý containers

```bash
cd /home/ubuntu/rikkei-app

# Start services
docker-compose up -d

# Stop services
docker-compose down

# Restart một service
docker-compose restart backend

# Xem logs real-time
docker-compose logs -f backend

# Rebuild và restart
docker-compose up -d --build
```

### Kiểm tra database

```bash
# Exec vào PostgreSQL container
docker exec -it rikkei_postgres psql -U postgres -d rikkei_prod

# Trong psql:
\dt              # List tables
\d table_name    # Describe table
SELECT * FROM users LIMIT 10;
\q               # Quit
```

### Nginx

```bash
# Test config
sudo nginx -t

# Reload config
sudo systemctl reload nginx

# Restart Nginx
sudo systemctl restart nginx

# View logs
sudo tail -f /var/log/nginx/rikkei_access.log
sudo tail -f /var/log/nginx/rikkei_error.log
```

---

## 📊 Kiến trúc triển khai

```
Internet
   ↓
[Nginx :80]
   ↓ /api/*
[Backend Container :8080] ←→ [PostgreSQL Container :5432]
   ↑
[Docker Network: rikkei_network]
   ↑
[Volume: postgres_data] (Persistent storage)
```

---

## 🎯 Checklist hoàn thành bài thi

### Phần 1: Server & Database (30đ) ✅
- ✅ Cài Docker & Docker Compose
- ✅ PostgreSQL chạy trong container
- ✅ Database: `rikkei_prod`
- ✅ Volume persistent: `postgres_data`
- ✅ Chỉ kết nối nội bộ (không expose port 5432)

### Phần 2: Đóng gói & Triển khai (30đ) ✅
- ✅ Dockerfile multi-stage (Gradle build + OpenJDK runtime)
- ✅ Cấu hình application.yml kết nối PostgreSQL qua Docker Network
- ✅ docker-compose.yml quản lý Backend + Database

### Phần 3: Web Server & Reverse Proxy (20đ) ✅
- ✅ Nginx reverse proxy `/api` → Backend
- ✅ Gzip compression
- ✅ Giới hạn upload: `client_max_body_size 50M`

### Phần 4: CI/CD Pipeline (20đ) ✅
- ✅ GitHub Actions workflow
- ✅ Trigger: Push to main/master
- ✅ Build Docker images
- ✅ Push to Docker Hub
- ✅ Auto deploy: SSH + docker-compose pull + up

---

## 🐛 Troubleshooting

### Lỗi: Backend không connect được database

```bash
# Kiểm tra PostgreSQL đã chạy chưa
docker-compose ps postgres

# Xem log PostgreSQL
docker-compose logs postgres

# Restart cả stack
docker-compose down
docker-compose up -d
```

### Lỗi: Port 8080 đã được sử dụng

```bash
# Tìm process đang dùng port 8080
sudo lsof -i :8080

# Kill process
sudo kill -9 <PID>

# Hoặc dùng docker-compose down trước
docker-compose down
```

### Lỗi: GitHub Actions failed

- Kiểm tra Secrets đã nhập đúng chưa
- Xem chi tiết log trong tab Actions
- SSH key phải có quyền truy cập server

---

## 📝 Ghi chú

1. **Mật khẩu database** nên được lưu trong GitHub Secrets, không hardcode
2. **Domain** thay `domain.rikkei.edu.vn` bằng domain thực tế
3. **SSL/HTTPS**: Sau này có thể thêm Certbot để cấu hình Let's Encrypt
4. **Monitoring**: Có thể thêm Prometheus + Grafana để monitor

---

## 🎉 Kết quả

Sau khi deploy thành công, bạn có thể truy cập:

- **Backend API**: `http://3.107.235.9/api/...`
- **Health Check**: `http://3.107.235.9/api/actuator/health`
- **Với domain**: `http://domain.rikkei.edu.vn/api/...`

---

**Người thực hiện**: [Tên của bạn]  
**Ngày**: March 9, 2026  
**Server**: AWS EC2 (3.107.235.9)
