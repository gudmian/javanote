# javanote — статус проекта

> Запускайте `/step`, чтобы продолжить работу с места, где остановились — команда сама
> найдёт текущий этап/пункт по чек-листу ниже и обновит его после подтверждения. Полное
> описание этапов — в `PLAN.md`, поведение ассистента в этом репозитории — в `CLAUDE.md`.

Обновлено: 2026-08-08

## Текущая точка

Этапы 0 и 1 пройдены и проверены. В работе Этап 2 — SQL: пользователи (Postgres + JPA).
Начинаем с 2.1 (`spring-boot-starter-data-jpa`, `postgresql`).

## Прогресс по этапам

Статус этапа: `[ ]` не начат · `[~]` в работе · `[x]` завершён и проверен (см. «Проверка» в
`PLAN.md`). Подпункты — просто `[ ]`/`[x]`.

- [x] **0. Окружение и скелет проекта**
    - [x] 0.1 Инструменты (JDK 21, Maven, Docker, IDE, клиент Postgres)
    - [x] 0.2 Git-репозиторий, `.gitignore`, README
    - [x] 0.3 Maven multi-module (`common`, `core-api`, `push-service`)
    - [x] 0.4 `core-api`: starter web, `application.yml`, health-эндпоинт
    - [x] 0.5 `docker-compose.yml`: `postgres`
- [x] **1. REST CRUD (in-memory)**
    - [x] 1.1 Модель `Note`, in-memory репозиторий
    - [x] 1.2 DTO запроса/ответа
    - [x] 1.3 `NoteController` (CRUD)
    - [x] 1.4 Валидация, `@ControllerAdvice`/`ProblemDetail`
    - [x] 1.5 Тесты `@WebMvcTest`
- [~] **2. SQL: пользователи (Postgres + JPA)**
    - [ ] 2.1 `spring-boot-starter-data-jpa`, `postgresql`
    - [ ] 2.2 Flyway `V1__init.sql`
    - [ ] 2.3 `UserEntity`, `UserRepository`
    - [ ] 2.4 `POST /api/users`
    - [ ] 2.5 Тесты с Testcontainers (Postgres)
- [ ] **3. NoSQL: заметки (MongoDB)**
    - [ ] 3.1 Сервис `mongo` в docker-compose
    - [ ] 3.2 `NoteDocument`, `spring-boot-starter-data-mongodb`
    - [ ] 3.3 `NoteRepository extends MongoRepository`
    - [ ] 3.4 Поле `ownerId`
    - [ ] 3.5 Тесты с Testcontainers (Mongo)
- [ ] **4. Авторизация**
    - [ ] 4.1 `UserDetailsService`, BCrypt
    - [ ] 4.2 Form login + сессии для web
    - [ ] 4.3 JWT: `POST /api/auth/login`, фильтр валидации
    - [ ] 4.4 `SecurityFilterChain` (раздельно REST/web)
    - [ ] 4.5 Проверка владения заметкой (403)
    - [ ] 4.6 Секреты через переменные окружения
- [ ] **5. Web MVC (Thymeleaf)**
    - [ ] 5.1 `NoteViewController`
    - [ ] 5.2 Шаблоны: список/форма/login/register
    - [ ] 5.3 Layout/fragments, серверная валидация форм
    - [ ] 5.4 Флеш-сообщения
- [ ] **6. Кэширование**
    - [ ] 6.1 Caffeine, `@Cacheable`
    - [ ] 6.2 `@CacheEvict`/`@CachePut`
    - [ ] 6.3 Наблюдение cache hit/miss
    - [ ] 6.4 Redis + переключение через profile
- [ ] **7. Kafka: продюсер**
    - [ ] 7.1 Kafka (KRaft) в docker-compose
    - [ ] 7.2 Топик `note-events`
    - [ ] 7.3 `NoteCreatedEvent` в `common`
    - [ ] 7.4 Продюсер в `core-api`
    - [ ] 7.5 Тест продюсера
- [ ] **8. push-service + FCM**
    - [ ] 8.1 Модуль `push-service`
    - [ ] 8.2 Kafka-консьюмер
    - [ ] 8.3 `POST /api/push/register`
    - [ ] 8.4 Firebase Admin SDK, отправка push
    - [ ] 8.5 Обработка ошибок отправки
- [ ] **9. Наблюдаемость и тестовое упрочнение**
    - [ ] 9.1 Actuator (health/info/metrics)
    - [ ] 9.2 Структурированные логи, MDC
    - [ ] 9.3 Интеграционные тесты на весь стек
    - [ ] 9.4 Отчёт покрытия (JaCoCo)
- [ ] **10. CI/CD**
    - [ ] 10.1 Репозиторий на GitHub, README
    - [ ] 10.2 GitHub Actions: build+test
    - [ ] 10.3 Multi-stage Dockerfile
    - [ ] 10.4 Публикация образов в GHCR
- [ ] **11. Деплой в k3s (финал)**
    - [ ] 11.1 Установка k3s на VPS
    - [ ] 11.2 Инфраструктура через Helm
    - [ ] 11.3 Манифесты для `core-api`/`push-service`
    - [ ] 11.4 Ingress + TLS
    - [ ] 11.5 CD-шаг в GitHub Actions

## Зафиксированные по ходу решения

- groupId проекта — `io.gudmian` (вместо изначально запланированного `ru.dguba.javanote`;
  `PLAN.md` обновлён)
- Spring Boot parent — `4.1.0` (актуальный релиз на момент старта; корневой `pom.xml` не
  наследуется от `spring-boot-starter-parent`, а импортирует BOM `spring-boot-dependencies`
  через `dependencyManagement`, дочерние модули наследуются от корня)
- Postgres — образ `postgres:16-alpine`, хостовый порт **5433** (5432 уже занят контейнером
  другого локального проекта, `jira_review-db-1`), `POSTGRES_DB/USER/PASSWORD=javanote`,
  именованный volume `postgres-data`
- Actuator (`spring-boot-starter-actuator`) — эндпоинт `/actuator/health` доступен по
  умолчанию без доп. настройки `management.endpoints.web.exposure.include`
- Первый REST-эндпоинт — `PingController` (`GET /ping` → `pong`), позже перенесён в
  `io.gudmian.javanote.rest`, разминка перед `NoteController` в Этапе 1
- Пакеты `domain`/`data`/`dto`/`rest`/`utils` — плоско под `io.gudmian.javanote`, без общего
  `web`-родителя (сознательный выбор: `dto`/`domain`/`data` не привязаны к конкретному
  REST/MVC входу и могут переиспользоваться)
- `Note.tags` — `List<String>` (не `String`), `record` с компактным конструктором
  (`tags = List.copyOf(tags)`) для защиты от мутации извне
- `NoteRepository.create/update` принимают отдельные поля (`title`, `content`, `tags`), а не
  целый `Note` — `id`/`createdAt`/`updatedAt` полностью под управлением репозитория
- Ошибки — `spring.mvc.problemdetails.enabled=true` (автоматический `ProblemDetail` для
  стандартных исключений Spring, включая ошибки `@Valid`) + свой `@ControllerAdvice`
  (`NoteExceptionHandler`) для доменных исключений (`NoteNotFoundException`)
- Тесты контроллера — `@WebMvcTest` + `MockMvcTester` (AssertJ-стиль поверх MockMvc,
  актуальный API Boot 4) + `@MockitoBean` (замена deprecated `@MockBean`)

## Проблемы и их решения

- `Non-resolvable parent POM` / `Could not find artifact ...:common:jar` — в `<parent>` и в
  `<dependency>` на `common` у дочерних модулей остался старый groupId `ru.dguba.javanote`
  из черновика, не совпадающий с фактическим `io.gudmian` в корневом `pom.xml`. Решение —
  привести groupId к одному значению везде (корень, `<parent>` во всех модулях, зависимости
  между модулями)
- `Maven model problem: 'modelVersion' is missing` — при переписывании корневого `pom.xml` в
  родительский (`packaging=pom`) потерялся обязательный тег `<modelVersion>4.0.0</modelVersion>`
  сразу после `<project>`
- Git remote на GitHub уже содержал независимый первый коммит (`.gitignore`/`LICENSE`/`README.md`
  от шаблона GitHub) — слито через `git merge --allow-unrelated-histories`; push по HTTPS
  потребовал бы токен, использован SSH-remote
- Docker daemon не был запущен при первой проверке (`docker ps` падал с `no such file or
  directory`) — решилось запуском Docker Desktop (`open -a Docker`)
- `@PathVariable UUID id` падал с `500`
  (`Name for argument of type [UUID] not specified... Ensure the compiler uses '-parameters'`)
  — та же причина, что и с `record`: без `spring-boot-starter-parent` компилятор не получает
  флаг `-parameters` автоматически. Решение — добавить в корневой `pom.xml` явный
  `pluginManagement` для `maven-compiler-plugin` с `<parameters>true</parameters>`
- AssertJ: импорт `assertThat` из `org.assertj.core.api.AssertionsForClassTypes` (узкий
  класс) не даёт доступа к fluent-методам `MockMvcTester` (`hasStatusOk()` и т.п.) — нужен
  импорт из полного `org.assertj.core.api.Assertions`
- После правок с изменением сигнатур методов/переносом пакетов `spring-boot-devtools`
  иногда не подхватывает изменения через hot-reload — помогает полный рестарт приложения

## Как продолжить

Запустите `/step`. Команда прочитает чек-лист выше, напомнит, на чём остановились
(или предложит следующий пункт), поможет разобраться и, после вашего подтверждения, что
пункт/этап сделан и проверен, обновит статус здесь и перейдёт дальше.
