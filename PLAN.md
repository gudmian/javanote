# javanote — план реализации

Учебный проект по книге *Craig Walls — Spring in Action* (Manning, 2018). Приложение для
заметок: REST API с JWT для мобильных клиентов, Web MVC (Thymeleaf) с сессионной
авторизацией для веба, гибридная персистентность (PostgreSQL + MongoDB), кэш (Caffeine →
Redis), асинхронная рассылка пушей через отдельный сервис на Kafka + FCM, финальный деплой
в k3s на собственном VPS.

Код пишет пользователь самостоятельно. Роль ассистента — ментор: объясняет концепции,
помогает разобраться в конкретном пункте, подсказывает, где искать в книге и в актуальной
документации Spring, ревьюит написанный код по запросу. Ассистент не пишет код за
пользователя, если не попросят явно.

**Как этим пользоваться:** текущая точка прогресса фиксируется в `STATUS.md`. Чтобы
продолжить работу, сошлитесь на номер этапа/пункта оттуда — так ассистент восстановит
контекст без необходимости пересказывать всё заново.

## Архитектура

Монорепозиторий, Maven multi-module, groupId `io.gudmian`:

- `common` — общие DTO/контракты Kafka-событий между `core-api` и `push-service`
- `core-api` — REST (JWT) + Web MVC (Thymeleaf, сессии) + персистентность (Postgres/JPA для
  пользователей, MongoDB для заметок) + кэш + Kafka-продюсер
- `push-service` — Kafka-консьюмер → Firebase Cloud Messaging

Java 21 (LTS), Spring Boot 3.x. Код примеров из книги (Spring Boot 2 / Java 8) адаптируется
к актуальным версиям API там, где они разошлись.

Локальная инфраструктура — `docker-compose.yml` в корне, обрастает сервисами по мере
надобности (Postgres → Mongo → Redis → Kafka). Миграции схемы Postgres — Flyway.

Тесты — JUnit 5, MockMvc/WebTestClient, Testcontainers (Postgres/Mongo/Kafka). Пишутся
после освоения концепции каждого пункта, без строгого TDD.

Каждый этап заканчивается **проверяемым инкрементом** — п.4 каждого этапа ниже описывает,
как руками убедиться, что этап действительно работает, прежде чем переходить к следующему.

---

## Этап 0 — Окружение и скелет проекта

0.1. Инструменты: JDK 21, Maven, Docker, IDE (рекомендуется IntelliJ), клиент Postgres
(DBeaver/psql)
0.2. Git-репозиторий, `.gitignore` (Maven `target/`, IDE-файлы, `.env`), README
0.3. Maven multi-module: корневой `pom.xml` (packaging `pom`, модули `common`, `core-api`,
`push-service`), groupId `io.gudmian`
0.4. `core-api`: Spring Boot starter (web), `application.yml`, health/`ping`-эндпоинт
0.5. `docker-compose.yml`: сервис `postgres`

**Проверка:** `mvn -pl core-api spring-boot:run`, `curl localhost:8080/actuator/health` →
`{"status":"UP"}`; `docker compose up -d postgres` поднимается без ошибок.

## Этап 1 — REST CRUD (in-memory)

1.1. Модель `Note` (id, title, content, tags, createdAt, updatedAt), репозиторий на
`ConcurrentHashMap`/List (временно, без БД)
1.2. DTO для запроса/ответа — не отдавать доменную модель наружу напрямую
1.3. `NoteController`: `GET /api/notes`, `GET /api/notes/{id}`, `POST`, `PUT`, `DELETE`
1.4. Валидация (`jakarta.validation`), `@ControllerAdvice` + `ProblemDetail` (RFC 7807) для
единообразных ошибок
1.5. Тесты `@WebMvcTest` + `MockMvc` на контроллер

**Проверка:** полный CRUD-сценарий через Postman/curl; невалидный запрос → 400 с понятным
телом ошибки.

## Этап 2 — SQL: пользователи (Postgres + JPA)

2.1. `spring-boot-starter-data-jpa`, `postgresql`, подключение к контейнеру из этапа 0
2.2. Flyway: `V1__init.sql` — таблица `users`
2.3. `UserEntity`, `UserRepository` (Spring Data JPA)
2.4. `POST /api/users` — регистрация (хэширование пароля добавится в этапе 4 вместе с
security)
2.5. Тесты с Testcontainers (`@SpringBootTest` + контейнер Postgres)

**Проверка:** пользователь создаётся через эндпоинт, виден в БД (psql/DBeaver), Flyway
применяет миграцию при старте.

## Этап 3 — NoSQL: заметки (MongoDB)

3.1. Сервис `mongo` в `docker-compose.yml`
3.2. `spring-boot-starter-data-mongodb`, документ `NoteDocument` (заменяет in-memory модель
из этапа 1)
3.3. `NoteRepository extends MongoRepository`, перевод контроллера на Mongo-репозиторий
3.4. Поле `ownerId` — ссылка на `UserEntity.id` без FK (consistency на уровне приложения,
паттерн NoSQL)
3.5. Тесты с Testcontainers (Mongo)

**Проверка:** CRUD из этапа 1 продолжает работать через тот же REST-контракт; данные
переживают рестарт приложения; видно в `mongosh`.

## Этап 4 — Авторизация

4.1. `spring-boot-starter-security`, `UserDetailsService` на `UserRepository`, BCrypt для
паролей (обновить регистрацию из этапа 2)
4.2. Form login + сессии для web-путей (`/notes/**`)
4.3. JWT: выдача токена на `POST /api/auth/login`, фильтр (`OncePerRequestFilter`) валидации
токена на `/api/**`
4.4. `SecurityFilterChain`: раздельные конфигурации для `/api/**` (stateless, JWT) и
остального (session-based)
4.5. Проверка владения: доступ к чужой заметке → 403 (`@PreAuthorize` либо ручная проверка
в сервисе)
4.6. Секреты (JWT secret) — через переменные окружения, не в коде

**Проверка:** `/api/notes` без токена → 401; чужой токен на чужую заметку → 403; форма
`/login` создаёт сессию и пускает на `/notes`.

## Этап 5 — Web MVC (Thymeleaf)

5.1. `spring-boot-starter-thymeleaf`, контроллеры MVC (`NoteViewController`) отдельно от
REST-контроллера
5.2. Шаблоны: список заметок, форма создания/редактирования, страницы login/register
5.3. Layout/fragments (шапка, меню), серверная валидация форм с выводом ошибок
5.4. Флеш-сообщения (`RedirectAttributes`) при успешном создании/удалении

**Проверка:** весь цикл работы с заметками проходится в браузере без единого вызова
Postman.

## Этап 6 — Кэширование

6.1. `spring-boot-starter-cache` + Caffeine, `@Cacheable` на чтение заметки/списка
6.2. `@CacheEvict`/`@CachePut` на create/update/delete
6.3. Наблюдение cache hit/miss (лог или простая метрика в сервисе)
6.4. Redis-сервис в `docker-compose.yml`, `RedisCacheManager`, переключение конфигурации
через Spring profile (`cache=caffeine` / `cache=redis`)

**Проверка:** второй одинаковый GET быстрее первого / виден cache hit в логе; после
переключения на Redis поведение не меняется, а `redis-cli KEYS '*'` показывает ключи.

## Этап 7 — Kafka: продюсер

7.1. Kafka в `docker-compose.yml` (KRaft mode, без Zookeeper)
7.2. Топик `note-events`
7.3. `NoteCreatedEvent` в модуле `common` (общий контракт)
7.4. `spring-kafka` в `core-api`, публикация события после успешного создания заметки
7.5. Тест продюсера (`@EmbeddedKafka` или Testcontainers Kafka)

**Проверка:** `kafka-console-consumer --topic note-events` (или Kafka UI/Redpanda Console)
показывает событие сразу после `POST /api/notes`.

## Этап 8 — push-service + FCM

8.1. Новый модуль `push-service` (зависит от `common`), собственные `application.yml` и порт
8.2. Kafka-консьюмер на `note-events`
8.3. `POST /api/push/register` — регистрация device-токена (userId + token)
8.4. Firebase Admin SDK: отправка push всем токенам владельца при получении события
8.5. Обработка ошибок отправки — невалидный/просроченный токен удаляется из хранилища

**Проверка:** регистрируете тестовый токен (эмулятор с Firebase SDK или curl-заглушка),
создаёте заметку в `core-api` → push долетает / виден лог успешной отправки в Firebase.

## Этап 9 — Наблюдаемость и тестовое упрочнение

9.1. `spring-boot-starter-actuator`: health (индикаторы Postgres/Mongo/Kafka), info, metrics
9.2. Структурированные логи, MDC (requestId, userId) через Filter/Interceptor

**Проверка:** `/actuator/health`/`/actuator/info`/`/actuator/metrics` отдают ожидаемые данные;
лог содержит `requestId`/`userId` через MDC на каждом запросе.

> Подпункты 9.3 (интеграционные тесты на весь стек) и 9.4 (отчёт покрытия JaCoCo) исключены из
> плана по решению пользователя — сознательно вне охвата проекта.

## Этап 10 — CI/CD

10.1. Репозиторий на GitHub, README
10.2. GitHub Actions: build+test на push/PR
10.3. Multi-stage Dockerfile для `core-api` и `push-service`
10.4. Публикация образов в GHCR при пуше в main

**Проверка:** пуш в main → зелёный workflow в Actions → образы видны в GHCR.

## Этап 11 — Деплой в k3s (финал)

11.1. Установка k3s на VPS
11.2. Инфраструктура через Helm (Bitnami-чарты Postgres/Mongo/Kafka/Redis)
11.3. Kubernetes-манифесты для `core-api`/`push-service` (Deployment, Service,
ConfigMap/Secret)
11.4. Ingress (Traefik, встроен в k3s) + TLS (cert-manager + Let's Encrypt)
11.5. CD-шаг в GitHub Actions: `kubectl apply`/`helm upgrade` при пуше в main

**Проверка:** приложение открывается по публичному URL, REST/web/push работают в реальном
интернете — финальная точка всего проекта.
