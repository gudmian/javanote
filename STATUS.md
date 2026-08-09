# javanote — статус проекта

> Запускайте `/step`, чтобы продолжить работу с места, где остановились — команда сама
> найдёт текущий этап/пункт по чек-листу ниже и обновит его после подтверждения. Полное
> описание этапов — в `PLAN.md`, поведение ассистента в этом репозитории — в `CLAUDE.md`.

Обновлено: 2026-08-10

## Текущая точка

Этапы 0-4 пройдены и проверены (частично — см. ниже про форму `/login`). В работе Этап 5 —
Web MVC (Thymeleaf). Начинаем с 5.1 (`NoteViewController`).

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
- [x] **2. SQL: пользователи (Postgres + JPA)**
    - [x] 2.1 `spring-boot-starter-data-jpa`, `postgresql`
    - [x] 2.2 Flyway `V1__init.sql`
    - [x] 2.3 `UserEntity`, `UserRepository`
    - [x] 2.4 `POST /api/users`
    - [x] 2.5 Тесты с Testcontainers (Postgres)
- [x] **3. NoSQL: заметки (MongoDB)**
    - [x] 3.1 Сервис `mongo` в docker-compose
    - [x] 3.2 `NoteDocument`, `spring-boot-starter-data-mongodb`
    - [x] 3.3 `NoteRepository extends MongoRepository`
    - [x] 3.4 Поле `ownerId`
    - [x] 3.5 Тесты с Testcontainers (Mongo)
- [x] **4. Авторизация**
    - [x] 4.1 `UserDetailsService`, BCrypt
    - [x] 4.2 Form login + сессии для web
    - [x] 4.3 JWT: `POST /api/auth/login`, фильтр валидации
    - [x] 4.4 `SecurityFilterChain` (раздельно REST/web)
    - [x] 4.5 Проверка владения заметкой (403)
    - [x] 4.6 Секреты через переменные окружения
- [~] **5. Web MVC (Thymeleaf)**
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
- `users`: `id UUID` (генерируется в Java через `@UuidGenerator`, без DB-side `DEFAULT`),
  `username`/`password` (пароль пока в открытом виде — хэширование только в Этапе 4.1),
  `created_at`
- `UserEntity` — первое реальное применение Lombok в проекте (`@Getter`/`@Setter`/
  `@NoArgsConstructor`) — JPA-сущности обязаны быть изменяемыми, `record` не подходит
- `UserRepository extends JpaRepository<UserEntity, UUID>` — без единой строчки
  реализации, в отличие от рукописного `InMemoryNoteRepository`
- Testcontainers `2.0.5` (версия из `spring-boot-dependencies`) — артефакты под конкретные
  БД переименованы с префиксом `testcontainers-` (`testcontainers-postgresql`,
  `testcontainers-junit-jupiter`); новый `org.testcontainers.postgresql.PostgreSQLContainer`
  — уже не generic-класс (в отличие от легаси `org.testcontainers.containers.PostgreSQLContainer<SELF>`)
- `@ServiceConnection` (`spring-boot-testcontainers`) — автоматически прокидывает
  datasource контейнера в контекст, без ручного `@DynamicPropertySource`
- Mongo: `mongo:7`, порт `27017`, root-аутентификация включена
  (`MONGO_INITDB_ROOT_USERNAME/PASSWORD`), volume на реальный путь данных `/data/db`
- Подключение — `spring.mongodb.uri` с `?authSource=admin` (root живёт в `admin`, не в
  целевой базе); представление UUID — `spring.mongodb.representation.uuid: STANDARD`
  (дефолт `UNSPECIFIED` не годится)
- `NoteDocument` — `record` (Spring Data MongoDB, в отличие от JPA, маппит immutable-типы
  нативно), `@Document(collection = "notes")`, `id`/`ownerId` — `UUID`
- `ownerId` в `NoteDocument` — ссылка на `UserEntity.id` без FK (Postgres и Mongo — разные
  движки, целостность связи только на уровне приложения); в `update` берётся из
  существующей записи, а не из тела запроса — владельца нельзя переписать через `PUT`
- `NoteRepository extends MongoRepository<NoteDocument, UUID>` — весь in-memory стек
  Этапа 1 (`Note`, старый `NoteRepository`, `InMemoryNoteRepository`) удалён
- Testcontainers для Mongo — `org.testcontainers:testcontainers-mongodb`,
  `org.testcontainers.mongodb.MongoDBContainer` (не-generic, тот же паттерн, что и
  `PostgreSQLContainer`)
- Проверка владения заметкой (4.5) — `NoteController.assertOwner`: `Authentication` берётся
  как параметр метода контроллера (Spring MVC резолвит `Authentication`/`Principal` из
  `request.getUserPrincipal()`), `authentication.getName()` → `UserRepository.findByUsername`
  → сравнение `UserEntity.id` с `NoteDocument.ownerId`; порядок проверок — сначала 404
  (`NoteNotFoundException`), потом 403 (`NoteAccessDeniedException`), чтобы код ответа не
  палил чужой заметке факт её существования
- `NoteAccessDeniedException` + обработчик в `NoteExceptionHandler` (`ProblemDetail`,
  `HttpStatus.FORBIDDEN`) — по образцу уже существующего `NoteNotFoundException`
- `GET /api/notes` (`readAll`) теперь отдаёт только заметки текущего пользователя —
  `NoteRepository.findAllByOwnerId(UUID)` (derived query), а не все заметки всех
  пользователей, как было в Этапе 1 (иначе список оставался бы дырой в обход 403 на
  конкретном `id`)
- JWT-секрет (4.6) уже читался из переменной окружения с дефолтом для разработки —
  `${JWT_SECRET:dev-only-secret-change-me-please-32-bytes-min}` в `application.yml` — отдельной
  работы не потребовалось, подпункт закрыт по факту существующей конфигурации
- Критерий «Проверка» этапа 4 подтверждён руками через curl (401 без токена, 403 на чужую
  заметку — двумя реальными пользователями и их JWT) — третья часть критерия («форма
  `/login` создаёт сессию и пускает на `/notes`») сознательно отложена: страницы `/notes`
  физически не существует до Этапа 5 (`NoteViewController`), полноценно проверить редирект
  можно будет только вместе с проверкой самого Этапа 5

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
- `FlywayException: Unsupported Database: PostgreSQL 16.13` — `flyway-database-postgresql`
  объявлен `optional` внутри `spring-boot-starter-flyway`, транзитивно не тянется, нужно
  подключать явно
- `JpaRepository<UUID, UserEntity>` — перепутаны местами параметры (entity, id) в
  `UserRepository`, компилируется, но полностью ломает семантику методов репозитория
- `@ServiceConnection` не резолвился — при переименовании `org.testcontainers:*`
  артефактов (см. выше) заодно случайно потерялась зависимость `spring-boot-testcontainers`
  из `pom.xml`
- Mockito `inline-mock-maker` self-attach warning в логах тестов — известный шум из-за
  будущего запрета динамической загрузки java-агентов в JDK, не ошибка; фикс через
  `-javaagent` в surefire отложен до Этапа 9
- `MONGO_DB` — такой env-переменной не существует (нужна `MONGO_INITDB_DATABASE`, но и она
  не создаёт базу заранее — Mongo бессхемна, база/коллекции появляются лениво при первой
  записи, в отличие от `POSTGRES_DB`)
- Volume `docker-compose.yml` для Mongo указывал на `/var/lib/mongo/data` (скопировано с
  Postgres-блока) вместо реального пути данных Mongo — `/data/db`; персистентность молча
  не работала
- `spring.data.mongodb.uri` — deprecated на уровне **error** начиная с Boot 4.0.0, заменён
  на `spring.mongodb.uri`; при этом `spring.data.mongodb.*` не исчез целиком — под ним
  остались Spring-Data-специфичные настройки (`representation.big-decimal`, `gridfs`,
  `auto-index-creation`), не connection-параметры
- `spring.mongodb.representation.uuid` — верный путь оказался не под `spring.data.mongodb`
  (там только `representation.big-decimal`), а под `spring.mongodb` — рядом с `uri`
- `@Document(collation = "notes")` — опечатка `collation`/`collection`: `collation` реально
  существующий, но не относящийся к имени коллекции атрибут (правила сравнения строк),
  компилируется без ошибок, но имя коллекции тихо не задаётся
- В `@WebMvcTest` с замоканным репозиторием точное совпадение объекта в
  `given(repository.save(конкретный объект))` не работает, если контроллер сам генерирует
  `id`/`Instant.now()` внутри себя — нужен `any(NoteDocument.class)` +
  `willAnswer(invocation -> invocation.getArgument(0))` вместо точного значения
- `BDDMockito.given(...)` нельзя вызывать на `void`-методах (`deleteById`) — на моках
  `void`-методы по умолчанию no-op, стабить нужно только то, что реально влияет на ветвление
  (`existsById`)
- После добавления `assertOwner` в `NoteController` тесты `NoteControllerTests` падали все
  разом — конструктор потребовал `UserRepository`, а `@WebMvcTest` не мокал новую
  зависимость (`UnsatisfiedDependencyException`)
- После добавления мока `UserRepository` часть тестов всё равно падала с
  `NullPointerException: Cannot invoke "Authentication.getName()" because "auth" is null` —
  `SecurityMockMvcRequestPostProcessors.user(...)` из `spring-security-test` кладёт
  аутентификацию в `SecurityContextHolder`/`TestSecurityContextHolder`, но `@WebMvcTest`
  не поднимает `SecurityConfig` (это `@Configuration`, слайс-тест его не сканирует), поэтому
  фильтр, синхронизирующий `SecurityContext` с `request.getUserPrincipal()`, в тестовом
  стеке отсутствует, и `Authentication`-параметр контроллера резолвится в `null`. Решение —
  подставлять принципал напрямую через `.principal(new UsernamePasswordAuthenticationToken(...))`
  на билдере `MockMvcTester` (пишет прямо в `MockHttpServletRequest.userPrincipal`, откуда
  Spring MVC и берёт `Authentication`-параметр) — зависимость `spring-security-test` в итоге
  не понадобилась

## Как продолжить

Запустите `/step`. Команда прочитает чек-лист выше, напомнит, на чём остановились
(или предложит следующий пункт), поможет разобраться и, после вашего подтверждения, что
пункт/этап сделан и проверен, обновит статус здесь и перейдёт дальше.
