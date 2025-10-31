#!/usr/bin/env bash
set -euo pipefail

# Render provides database credentials via several env vars. Normalise them
# into the standard Spring Boot variables expected by the application.

maybe_from_mysql_vars() {
  if [[ -n "${MYSQL_HOST:-}" && -n "${MYSQL_DATABASE:-}" ]]; then
    local host="${MYSQL_HOST}"
    local port="${MYSQL_PORT:-3306}"
    local user="${MYSQL_USER:-root}"
    local pass="${MYSQL_PASSWORD:-}" 
    export SPRING_DATASOURCE_URL="jdbc:mysql://${host}:${port}/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-$user}"
    export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-$pass}"
  fi
}

maybe_from_connection_string() {
  local raw="${1:-}"
  if [[ -z "$raw" ]]; then
    return
  fi
  if [[ "$raw" == mysql://* ]]; then
    # Strip scheme
    local without_scheme="${raw#mysql://}"
    local credentials="${without_scheme%%@*}"
    local host_and_db="${without_scheme#*@}"
    local user="${credentials%%:*}"
    local pass="${credentials#*:}"
    local host_port="${host_and_db%%/*}"
    local database="${host_and_db#*/}"
    local host="${host_port%%:*}"
    local port="${host_port##*:}"
    if [[ "$host_port" == "$host" ]]; then
      port=3306
    fi
    export SPRING_DATASOURCE_URL="jdbc:mysql://${host}:${port}/${database}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-$user}"
    export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-$pass}"
  fi
}

# Normalise env vars in priority order
maybe_from_mysql_vars
maybe_from_connection_string "${RENDER_MYSQL_CONNECTION_STRING:-}"
maybe_from_connection_string "${DATABASE_URL:-}"
maybe_from_connection_string "${JAWSDB_URL:-}"

exec java -jar /app/app.jar