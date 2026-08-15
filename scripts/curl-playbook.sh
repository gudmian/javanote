#!/usr/bin/env bash
#
# javanote — playbook для ручной проверки через curl.
#
# Использование:
#   source scripts/curl-playbook.sh
#   register alice
#   login alice
#   create_note "Заголовок" "Текст"
#   read_note "$NOTE_ID"
#   list_notes
#   update_note "$NOTE_ID" "Новый заголовок" "Новый текст"
#   delete_note "$NOTE_ID"
#
# Либо запусти файл напрямую (bash scripts/curl-playbook.sh) — прогонит полный
# демонстрационный сценарий сам, с одноразовым случайным пользователем.
#
# Переменные окружения (можно переопределить перед source):
#   BASE           — адрес core-api,        по умолчанию http://localhost:8080
#   PUSH_BASE      — адрес push-service,    по умолчанию http://localhost:8081
#
# Требуются установленные: curl, python3 (для разбора JSON — jq не обязателен).

BASE="${BASE:-http://localhost:8080}"
PUSH_BASE="${PUSH_BASE:-http://localhost:8081}"

_json_get() {
  # _json_get '<json>' key -> значение поля key
  python3 -c "import sys,json;print(json.loads(sys.argv[1])[sys.argv[2]])" "$1" "$2"
}

# --- health-check обоих сервисов -------------------------------------------

health() {
  echo "core-api:     $(curl -s -o /dev/null -w '%{http_code}' "$BASE/actuator/health")"
  echo "push-service: $(curl -s -o /dev/null -w '%{http_code}' "$PUSH_BASE/actuator/health" 2>/dev/null || echo "н/д")"
}

# --- аутентификация (REST, JWT) ---------------------------------------------
# После вызова register/login в переменных окружения остаются: USERNAME, USER_ID, TOKEN

register() {
  local username="$1"
  local password="${2:-${username}-pass-123}"
  local resp
  resp=$(curl -s -X POST "$BASE/api/users" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"password\":\"$password\"}")
  echo "$resp"
  USERNAME="$username"
  PASSWORD="$password"
  USER_ID=$(_json_get "$resp" id)
  export USERNAME PASSWORD USER_ID
}

login() {
  local username="${1:-$USERNAME}"
  local password="${2:-$PASSWORD}"
  local resp
  resp=$(curl -s -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"password\":\"$password\"}")
  TOKEN=$(_json_get "$resp" token)
  export TOKEN
  echo "token: ${TOKEN:0:20}..."
}

# --- заметки (REST) ---------------------------------------------------------
# После create_note в NOTE_ID остаётся id созданной заметки

create_note() {
  local title="${1:-Заголовок}"
  local content="${2:-Текст}"
  local tags="${3:-[]}"
  local resp
  resp=$(curl -s -X POST "$BASE/api/notes" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
    -d "{\"ownerId\":\"$USER_ID\",\"title\":\"$title\",\"content\":\"$content\",\"tags\":$tags}")
  echo "$resp"
  NOTE_ID=$(_json_get "$resp" id)
  export NOTE_ID
}

list_notes() {
  curl -s "$BASE/api/notes" -H "Authorization: Bearer $TOKEN" -w "\nSTATUS=%{http_code}\n"
}

read_note() {
  local id="${1:-$NOTE_ID}"
  curl -s "$BASE/api/notes/$id" -H "Authorization: Bearer $TOKEN" -w "\nSTATUS=%{http_code}\n"
}

update_note() {
  local id="${1:-$NOTE_ID}"
  local title="${2:-Обновлено}"
  local content="${3:-Обновлённый текст}"
  local tags="${4:-[]}"
  curl -s -X PUT "$BASE/api/notes/$id" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
    -d "{\"ownerId\":\"$USER_ID\",\"title\":\"$title\",\"content\":\"$content\",\"tags\":$tags}" \
    -w "\nSTATUS=%{http_code}\n"
}

delete_note() {
  local id="${1:-$NOTE_ID}"
  curl -s -X DELETE "$BASE/api/notes/$id" -H "Authorization: Bearer $TOKEN" -w "\nSTATUS=%{http_code}\n"
}

# --- веб-сессия (форма логина, куки, CSRF) ----------------------------------
# Использует отдельный cookie jar $WEB_JAR; после web_login можно ходить на /notes

web_register() {
  local username="$1"
  local password="${2:-${username}-pass-123}"
  WEB_JAR=$(mktemp)
  export WEB_JAR
  local page csrf
  page=$(curl -s -b "$WEB_JAR" -c "$WEB_JAR" "$BASE/register")
  csrf=$(echo "$page" | grep -oE 'name="_csrf"[^>]*value="[^"]*"' | head -1 | sed -E 's/.*value="([^"]*)"/\1/')
  curl -s -b "$WEB_JAR" -c "$WEB_JAR" -X POST "$BASE/register" \
    --data-urlencode "username=$username" \
    --data-urlencode "password=$password" \
    --data-urlencode "_csrf=$csrf" \
    -o /dev/null -w "register STATUS=%{http_code}\n"
  WEB_USERNAME="$username"
  WEB_PASSWORD="$password"
  export WEB_USERNAME WEB_PASSWORD
}

web_login() {
  local username="${1:-$WEB_USERNAME}"
  local password="${2:-$WEB_PASSWORD}"
  local page csrf
  page=$(curl -s -b "$WEB_JAR" -c "$WEB_JAR" "$BASE/login")
  csrf=$(echo "$page" | grep -oE 'name="_csrf"[^>]*value="[^"]*"' | head -1 | sed -E 's/.*value="([^"]*)"/\1/')
  curl -s -b "$WEB_JAR" -c "$WEB_JAR" -X POST "$BASE/login" \
    --data-urlencode "username=$username" \
    --data-urlencode "password=$password" \
    --data-urlencode "_csrf=$csrf" \
    -o /dev/null -w "login STATUS=%{http_code}\n"
}

web_get() {
  # web_get /notes  — GET произвольного веб-пути с уже залогиненной сессией
  curl -s -b "$WEB_JAR" "$BASE$1"
}

# --- наблюдаемость кэша (Этап 6) --------------------------------------------

cache_list() {
  curl -s "$BASE/actuator/caches" | python3 -m json.tool
}

cache_stats() {
  # cache_stats notes   — hit/miss по конкретному кэшу
  local cache="${1:-notes}"
  echo "hit:  $(curl -s "$BASE/actuator/metrics/cache.gets?tag=cache:$cache&tag=result:hit" | python3 -c "import sys,json;print(json.load(sys.stdin)['measurements'][0]['value'])")"
  echo "miss: $(curl -s "$BASE/actuator/metrics/cache.gets?tag=cache:$cache&tag=result:miss" | python3 -c "import sys,json;print(json.load(sys.stdin)['measurements'][0]['value'])")"
}

# --- Kafka (Этап 7) ----------------------------------------------------------

kafka_watch() {
  # Слушает топик note-events с самого начала, блокирует терминал (Ctrl+C для выхода)
  docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 --topic note-events --from-beginning --property print.key=true
}

kafka_topics() {
  docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092
}

# --- полный демонстрационный прогон, если файл запущен напрямую -------------

_demo() {
  local suffix=$RANDOM
  echo "=== health ==="
  health
  echo "=== register + login ==="
  register "demo$suffix"
  login
  echo "=== create + list + read + update + delete ==="
  create_note "Демо-заметка" "Текст заметки"
  list_notes
  read_note
  update_note "$NOTE_ID" "Демо-заметка (изменена)"
  read_note
  delete_note
  echo "=== готово ==="
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  _demo
fi
