#!/bin/bash

# Script thiết lập server AWS để triển khai ứng dụng
# Chạy trên server: bash setup-server.sh

set -e

echo "=== Cài đặt môi trường AWS Server ===" 

# Update system
echo "1. Update system..."
sudo apt update && sudo apt upgrade -y

# Install Docker
echo "2. Cài đặt Docker..."
sudo apt install -y docker.io
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Install Docker Compose
echo "3. Cài đặt Docker Compose..."
sudo apt install -y docker-compose

# Install Nginx
echo "4. Cài đặt Nginx..."
sudo apt install -y nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# Install curl (for health checks)
echo "5. Cài đặt curl..."
sudo apt install -y curl

# Tạo thư mục deploy
echo "6. Tạo thư mục triển khai..."
mkdir -p /home/ubuntu/rikkei-app
cd /home/ubuntu/rikkei-app

# Generate SSH key cho GitHub Actions
echo "7. Tạo SSH key cho GitHub Actions..."
if [ ! -f ~/.ssh/id_rsa ]; then
    ssh-keygen -t rsa -b 4096 -N "" -f ~/.ssh/id_rsa -C "github-actions"
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
    chmod 600 ~/.ssh/authorized_keys
    echo ""
    echo "✅ SSH key đã được tạo!"
    echo "📋 Copy private key này vào GitHub Secret 'SSH_PRIVATE_KEY':"
    echo "---"
    cat ~/.ssh/id_rsa
    echo "---"
else
    echo "SSH key đã tồn tại"
fi

echo ""
echo "=== ✅ Hoàn tất cài đặt server ===" 
echo ""
echo "📝 Các bước tiếp theo:"
echo "1. Copy private key ở trên vào GitHub Secrets"
echo "2. Thêm DOCKER_USERNAME và DOCKER_PASSWORD vào GitHub Secrets"
echo "3. Push code lên GitHub để trigger CI/CD"
echo "4. Sau khi deploy xong, cấu hình Nginx với domain của bạn"
echo ""
echo "🔍 Kiểm tra cài đặt:"
docker --version
docker-compose --version
nginx -v
echo ""
echo "🎉 Server đã sẵn sàng!"
