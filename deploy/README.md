VPS deployment helper
======================

This folder contains a convenience script `deploy.sh` to bootstrap this project on a Linux (Ubuntu) VPS. The script automates installing Docker, cloning the repository, copying `.env.example` to `.env`, and starting the services with Docker Compose.

Important: you must edit the `.env` file on the VPS to set secrets (for example `DATA_GOV_API_KEY`) and any production DB credentials before starting the services.

Quick manual steps (recommended)
-------------------------------

1. SSH to your VPS (Ubuntu 22.04/24 recommended).

2. Install Docker & Docker Compose (the script will do this automatically). If you prefer manual:

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg lsb-release
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker $USER
```

3. Clone the repo to `/opt/ourvoice-backend` (or run the script from a local checkout):

```bash
git clone <your-repo-url> /opt/ourvoice-backend
cd /opt/ourvoice-backend
cp .env.example .env
# Edit .env and set DATA_GOV_API_KEY and any passwords
nano .env
```

4. Place the GeoLite2-City.mmdb file in `/opt/ourvoice-backend/geoip` (or update `GEOIP2_DB_PATH` in `.env` accordingly).

5. Start with Docker Compose:

```bash
sudo docker compose up -d --build
```

6. Make the service persistent across reboots (systemd unit example):

Create `/etc/systemd/system/ourvoice-backend.service` with contents:

```
[Unit]
Description=OurVoiceOurRights Backend (Docker Compose)
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/ourvoice-backend
ExecStart=/usr/bin/docker compose up -d --build
ExecStop=/usr/bin/docker compose down

[Install]
WantedBy=multi-user.target
```

Then enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now ourvoice-backend.service
```

7. Optional: set up Nginx as a reverse proxy and obtain TLS certs with Certbot. Run Nginx on the host, proxy `/:8080` to 127.0.0.1:8080, and use certbot to get Let's Encrypt certs.

Security & production notes
---------------------------
- Use strong DB credentials and do not run MySQL with default root password in production.
- Consider running MySQL and Redis as managed services or on separate hosts for production.
- Configure a firewall (ufw) to only allow necessary ports (22, 80, 443). Example:

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80,443/tcp
sudo ufw enable
```
