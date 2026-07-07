#!/usr/bin/env bash
# deploy.sh — roda LOCAL, conecta na EC2 e faz o deploy via Docker Compose.
#
# O que o script faz:
#   1. Instala Docker + Docker Compose se ainda não houver
#   2. Clona o repositório (ou atualiza com git pull)
#   3. Envia o .env local pro servidor
#   4. Sobe os containers com docker compose up --build -d
#
# Uso:
#   export EC2_HOST=1.2.3.4
#   export REPO_URL=https://github.com/seu-usuario/ziro.git
#   ./deploy.sh
#
# Pré-requisitos:
#   - Security Group com portas 22, 80 e 8080 abertas
#   - .env local configurado com os valores de produção

set -euo pipefail

# ── configuração (edite aqui ou exporte antes de rodar) ──────────────────────
EC2_HOST="${EC2_HOST:-}"
EC2_USER="${EC2_USER:-ec2-user}"
EC2_KEY="${EC2_KEY:-${HOME}/.ssh/ziro.pem}"
REPO_URL="${REPO_URL:-}"
REPO_BRANCH="${REPO_BRANCH:-main}"
APP_DIR="${APP_DIR:-/home/${EC2_USER}/ziro}"
ENV_FILE="${ENV_FILE:-.env}"
# ──────────────────────────────────────────────────────────────────────────────

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
log()  { echo -e "\n${CYAN}[deploy]${NC} $*"; }
ok()   { echo -e "${GREEN}[✓]${NC} $*"; }
die()  { echo -e "${RED}[✗]${NC} $*" >&2; exit 1; }

# ── validações locais ─────────────────────────────────────────────────────────
[[ -z "$EC2_HOST" ]]  && die "EC2_HOST não definido. Ex: export EC2_HOST=1.2.3.4"
[[ -z "$REPO_URL" ]]  && die "REPO_URL não definido. Ex: export REPO_URL=https://github.com/user/ziro.git"
[[ ! -f "$EC2_KEY" ]] && die "Chave SSH não encontrada: $EC2_KEY"
[[ ! -f "$ENV_FILE" ]] && die ".env não encontrado: $ENV_FILE  (copie .env.example e configure)"

SSH_OPTS="-i ${EC2_KEY} -o StrictHostKeyChecking=accept-new -o ConnectTimeout=15"
remote() { ssh $SSH_OPTS "${EC2_USER}@${EC2_HOST}" "$@"; }

echo ""
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN} Deploy Ziro${NC}  →  ${EC2_USER}@${EC2_HOST}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# ── testa conexão ─────────────────────────────────────────────────────────────
log "Testando conexão SSH..."
remote "echo ok" > /dev/null || die "Falhou ao conectar. Verifique EC2_HOST, EC2_USER e EC2_KEY."
ok "SSH OK"

# ── instala Docker ────────────────────────────────────────────────────────────
log "Verificando Docker..."
remote bash <<'EOF'
set -e
if command -v docker &>/dev/null; then echo "[já instalado: $(docker --version)]"; exit 0; fi
echo "Instalando Docker..."
. /etc/os-release
case "$ID" in
  amzn)
    if [[ "${VERSION_ID:-}" == "2" ]]; then
      sudo yum update -y -q
      sudo amazon-linux-extras install docker -y
    else
      sudo dnf update -y -q
      sudo dnf install -y docker
    fi ;;
  ubuntu|debian)
    export DEBIAN_FRONTEND=noninteractive
    sudo apt-get update -qq
    sudo apt-get install -y -qq ca-certificates curl gnupg
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL "https://download.docker.com/linux/${ID}/gpg" \
      | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
      https://download.docker.com/linux/${ID} $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
      | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update -qq
    sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin ;;
  *) echo "Distro '$ID' não suportada. Instale Docker manualmente." >&2; exit 1 ;;
esac
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
echo "Docker instalado."
EOF
ok "Docker OK"

# ── instala Docker Compose plugin ─────────────────────────────────────────────
log "Verificando Docker Compose..."
remote bash <<'EOF'
set -e
if docker compose version &>/dev/null 2>&1; then echo "[já disponível]"; exit 0; fi
COMPOSE_DIR="${HOME}/.docker/cli-plugins"
mkdir -p "$COMPOSE_DIR"
COMPOSE_VER=$(curl -fsSL https://api.github.com/repos/docker/compose/releases/latest \
              | grep '"tag_name"' | cut -d'"' -f4)
curl -fsSL "https://github.com/docker/compose/releases/download/${COMPOSE_VER}/docker-compose-$(uname -s)-$(uname -m)" \
  -o "${COMPOSE_DIR}/docker-compose"
chmod +x "${COMPOSE_DIR}/docker-compose"
echo "Docker Compose ${COMPOSE_VER} instalado."
EOF
ok "Docker Compose OK"

# ── clona ou atualiza repositório ─────────────────────────────────────────────
log "Sincronizando código (branch: ${REPO_BRANCH})..."
remote bash -s -- "$APP_DIR" "$REPO_URL" "$REPO_BRANCH" <<'EOF'
APP_DIR="$1"; REPO_URL="$2"; BRANCH="$3"
set -e
if [[ -d "${APP_DIR}/.git" ]]; then
  echo "Atualizando repositório..."
  cd "$APP_DIR"
  git fetch origin
  git checkout "$BRANCH"
  git reset --hard "origin/$BRANCH"
else
  echo "Clonando repositório..."
  git clone --branch "$BRANCH" "$REPO_URL" "$APP_DIR"
fi
echo "Commit: $(cd "$APP_DIR" && git log -1 --oneline)"
EOF
ok "Código atualizado"

# ── envia .env ────────────────────────────────────────────────────────────────
log "Enviando .env..."
scp $SSH_OPTS "$ENV_FILE" "${EC2_USER}@${EC2_HOST}:${APP_DIR}/.env"
ok ".env enviado"

# ── sobe os containers ────────────────────────────────────────────────────────
log "Subindo containers (pode demorar no primeiro deploy)..."
remote bash -s -- "$APP_DIR" <<'EOF'
APP_DIR="$1"
set -e
cd "$APP_DIR"

run_compose() {
  docker compose up --build -d --remove-orphans
}

# Se o Docker foi instalado nesta sessão o grupo ainda não está ativo na sessão SSH atual.
# sg docker reexecuta o comando já com o grupo aplicado.
if ! docker info &>/dev/null 2>&1; then
  sg docker -c "cd '$APP_DIR' && docker compose up --build -d --remove-orphans"
else
  run_compose
fi
EOF
ok "Containers no ar"

# ── status final ──────────────────────────────────────────────────────────────
log "Status dos serviços:"
remote bash -s -- "$APP_DIR" <<'EOF'
cd "$1" && docker compose ps
EOF

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN} Deploy concluído!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "  Frontend  →  http://${EC2_HOST}"
echo -e "  Backend   →  http://${EC2_HOST}:8080"
echo -e "  Adminer   →  http://${EC2_HOST}:8081  ${YELLOW}(feche essa porta no Security Group)${NC}"
echo ""
echo -e "${YELLOW}Lembre-se de ajustar no .env antes do deploy:${NC}"
echo -e "  VITE_API_URL=http://${EC2_HOST}:8080"
echo -e "  FRONTEND_URL=http://${EC2_HOST}"
echo ""
echo "Acompanhar logs:"
echo "  ssh -i ${EC2_KEY} ${EC2_USER}@${EC2_HOST} 'cd ${APP_DIR} && docker compose logs -f'"
echo ""
