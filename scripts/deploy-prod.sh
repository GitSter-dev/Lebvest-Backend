#!/usr/bin/env bash
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/lebvest}"
COMPOSE_BIN="${COMPOSE_BIN:-/usr/local/bin/docker-compose}"
COMPOSE_FILE="${COMPOSE_FILE:-${APP_DIR}/compose.prod.yaml}"
INFRA_ENV_FILE="${INFRA_ENV_FILE:-${APP_DIR}/.env.infrastructure}"
HEALTHCHECK_URL="${HEALTHCHECK_URL:-http://localhost:8080/api/v1/actuator/health}"
HEALTHCHECK_ATTEMPTS="${HEALTHCHECK_ATTEMPTS:-20}"
HEALTHCHECK_INTERVAL_SECONDS="${HEALTHCHECK_INTERVAL_SECONDS:-15}"

: "${IMAGE_NAME:?IMAGE_NAME is required}"
: "${DOCKERHUB_USERNAME:?DOCKERHUB_USERNAME is required}"
: "${DOCKERHUB_TOKEN:?DOCKERHUB_TOKEN is required}"

if [ ! -x "${COMPOSE_BIN}" ]; then
  echo "Docker Compose binary not found at ${COMPOSE_BIN}"
  exit 1
fi

if [ ! -f "${COMPOSE_FILE}" ]; then
  echo "Compose file not found at ${COMPOSE_FILE}"
  exit 1
fi

if [ ! -f "${INFRA_ENV_FILE}" ]; then
  echo "Infrastructure env file not found at ${INFRA_ENV_FILE}"
  exit 1
fi

cleanup() {
  docker logout >/dev/null 2>&1 || true
}

trap cleanup EXIT

cd "${APP_DIR}"
printf '%s' "${DOCKERHUB_TOKEN}" | docker login --username "${DOCKERHUB_USERNAME}" --password-stdin

export IMAGE_NAME
"${COMPOSE_BIN}" -f "${COMPOSE_FILE}" pull
"${COMPOSE_BIN}" -f "${COMPOSE_FILE}" up -d --remove-orphans

for attempt in $(seq 1 "${HEALTHCHECK_ATTEMPTS}"); do
  if curl -sf "${HEALTHCHECK_URL}" >/dev/null; then
    echo "Health check passed on attempt ${attempt}"
    exit 0
  fi
  echo "Attempt ${attempt}: application is not healthy yet"
  sleep "${HEALTHCHECK_INTERVAL_SECONDS}"
done

docker logs lebvest --tail 50
exit 1
