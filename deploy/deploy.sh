#!/usr/bin/env bash
set -euo pipefail

# Simple deployment helper for Ubuntu-like VPS
# Run as a user with sudo privileges. This script will:
# - install Docker & docker-compose (if missing)
# - clone the repo into /opt/ourvoice-backend (if not present)
# - ensure .env exists (copy from .env.example)
# - start services with docker compose

REPO_URL="$(git config --get remote.origin.url || echo '')"
TARGET_DIR="/opt/ourvoice-backend"

install_prereqs() {
  echo "Installing Docker and Docker Compose..."
  sudo apt-get update
  sudo apt-get install -y ca-certificates curl gnupg lsb-release
  if ! command -v docker >/dev/null 2>&1; then
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \$(lsb_release -cs) stable" \
      | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
  fi
  sudo usermod -aG docker "$USER" || true
}

clone_repo() {
  if [ -d "$TARGET_DIR/.git" ]; then
    echo "Repository already cloned at $TARGET_DIR"
    return
  fi
  if [ -z "$REPO_URL" ]; then
    echo "No origin URL available. Please run this script from within a checked-out repo or set REPO_URL in the script."
    exit 1
  fi
  sudo mkdir -p "$TARGET_DIR"
  sudo chown "$USER":"$USER" "$TARGET_DIR"
  git clone "$REPO_URL" "$TARGET_DIR"
}

ensure_env() {
  if [ ! -f "$TARGET_DIR/.env" ]; then
    if [ -f "$TARGET_DIR/.env.example" ]; then
      echo "Creating .env from .env.example (edit secrets afterwards)..."
      cp "$TARGET_DIR/.env.example" "$TARGET_DIR/.env"
      echo "Please edit $TARGET_DIR/.env to set DATA_GOV_API_KEY and other secrets, then re-run this script if needed."
    else
      echo "No .env or .env.example found in $TARGET_DIR. Create $TARGET_DIR/.env with required environment variables and re-run."
      exit 1
    fi
  fi
}

start_compose() {
  echo "Starting services with Docker Compose..."
  cd "$TARGET_DIR"
  sudo docker compose pull || true
  sudo docker compose up -d --build
}

main() {
  install_prereqs
  clone_repo
  ensure_env
  start_compose
  echo "Deployment complete. Check containers with: sudo docker ps"
}

main "$@"
