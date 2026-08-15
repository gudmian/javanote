# javanote — статус проекта

> Запускайте `/step`, чтобы продолжить работу с места, где остановились — команда сама
> найдёт текущий этап/пункт по чек-листу ниже и обновит его после подтверждения. Полное
> описание этапов — в `PLAN.md`, поведение ассистента в этом репозитории — в `CLAUDE.md`.

Обновлено: 2026-08-14

## Текущая точка

Этапы 0-8 пройдены и проверены. Этап 8 (push-service + FCM) закрыт: Kafka-консьюмер читает
`note-events`, `POST /api/push/register` хранит токены в отдельной Postgres-базе push-service,
`NoteEventListener` реально шлёт push через Firebase Admin SDK (`sendEachForMulticast`,
проверено на настоящем Firebase-проекте) и подчищает токены с кодом `UNREGISTERED`. В работе
Этап 9 (наблюдаемость) закрыт и проверен: 9.1 (Actuator health/info/metrics) и 9.2
(структурированные логи, MDC через Filter) подтверждены curl'ом и живым логом на core-api;
9.3/9.4 исключены из плана по решению пользователя. В работе Этап 10 — CI/CD. Начинаем с 10.1
(репозиторий на GitHub, README).

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
- [x] **5. Web MVC (Thymeleaf)**
    - [x] 5.1 `NoteViewController`
    - [x] 5.2 Шаблоны: список/форма/login/register
    - [x] 5.3 Layout/fragments, серверная валидация форм
    - [x] 5.4 Флеш-сообщения
- [x] **6. Кэширование**
    - [x] 6.1 Caffeine, `@Cacheable`
    - [x] 6.2 `@CacheEvict`/`@CachePut`
    - [x] 6.3 Наблюдение cache hit/miss
    - [x] 6.4 Redis + переключение через profile
- [x] **7. Kafka: продюсер**
    - [x] 7.1 Kafka (KRaft) в docker-compose
    - [x] 7.2 Топик `note-events`
    - [x] 7.3 `NoteCreatedEvent` в `common`
    - [x] 7.4 Продюсер в `core-api`
    - [x] 7.5 Тест продюсера
- [x] **8. push-service + FCM**
    - [x] 8.1 Модуль `push-service`
    - [x] 8.2 Kafka-консьюмер
    - [x] 8.3 `POST /api/push/register`
    - [x] 8.4 Firebase Admin SDK, отправка push
    - [x] 8.5 Обработка ошибок отправки
- [x] **9. Наблюдаемость и тестовое упрочнение**
    - [x] 9.1 Actuator (health/info/metrics)
    - [x] 9.2 Структурированные логи, MDC
- [~] **10. CI/CD**
    - [x] 10.1 Репозиторий на GitHub, README
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
- Отложенная часть критерия Этапа 4 закрыта вместе с 5.1: curl-сценарий с cookie jar
  (заход на `/notes` без сессии → 302 на `/login` → csrf из формы → `POST /login` →
  302 на `/notes?continue` → `GET /notes` той же сессией → 200) подтвердил, что form login
  запоминает исходный запрошенный URL и после аутентификации возвращает именно на него
- `NoteViewController` (5.1, пакет `web`) — та же логика «текущий пользователь → его
  заметки», что в `NoteController.readAll`, но кладёт список в `Model` и возвращает имя
  вида `notes/list` вместо DTO; шаблон `templates/notes/list.html` — временная заглушка
  (`Заметок: N`), настоящая вёрстка списка — уже 5.2
- Web MVC контроллеры (`NoteViewController`, `AuthViewController`) — отдельный пакет `web`,
  не смешаны с REST-контроллерами в `rest`
- `NoteForm`/`RegisterForm` (5.2) — отдельные mutable-классы (Lombok `@Getter`/`@Setter`/
  `@NoArgsConstructor`) специально под `th:field`-биндинг форм, не переиспользуют
  `NoteRequest`/`UserRequest` (record'ы не подходят — Thymeleaf/Spring Data Binder ожидают
  геттеры/сеттеры); `NoteForm.tags` — строка через запятую, а не `List<String>`, парсится
  `.split(",")` в контроллере
- CSRF в формах (5.2) — не понадобилась доп. зависимость `thymeleaf-extras-springsecurity6`:
  раз `spring-boot-starter-security` на classpath, Thymeleaf через встроенный
  `RequestDataValueProcessor` сам подставляет скрытое поле `_csrf` в любой `<form>`, который
  вообще обрабатывает (есть хоть один `th:*`-атрибут, например `th:action`) — руками добавлять
  `_csrf`-инпут не нужно
- `notes/form.html` — общий шаблон для create/edit, `action` считается динамически по
  `noteForm.id` (`null` → `/notes`, иначе → `/notes/{id}`); `id` прокидывается через скрытое
  поле формы — иначе терялся бы при повторном рендере формы после ошибки валидации
- `.formLogin(form -> form.loginPage("/login"))` (5.2) — своя страница логина вместо
  дефолтной Spring Security; `POST /login` и `POST /logout` контроллерами не пишутся —
  Spring Security обрабатывает их сам
- `fragments/header.html` (5.3) — общая шапка (меню + кнопка «Выйти») через
  `th:fragment="header"`/`th:replace="~{fragments/header :: header}"`; подключена в
  `list.html`/`form.html`, не подключена в `login.html`/`register.html` (страницы для
  незалогиненных пользователей)
- Удаление заметки в вебе (5.4) — `POST /notes/{id}/delete` через `<form>`, не `<a href>`:
  `GET` не должен иметь побочных эффектов, и CSRF-защита всё равно не покрывает `GET`-переходы
- Валидация форм (5.3) — `@Valid` + `BindingResult` сразу следующим параметром метода,
  `#fields.hasErrors('поле')`/`th:errors="*{поле}"` в шаблоне; дефолтные сообщения Bean
  Validation (`@NotBlank` и т.п.) уже локализованы на русский самим Hibernate Validator,
  без доп. настройки
- Дубликат логина при регистрации (5.3) — проверяется вручную
  (`userRepository.findByUsername(...).isPresent()` + `bindingResult.rejectValue(...)`) до
  `save()`, не через перехват `DataIntegrityViolationException` от `UNIQUE`-constraint'а в БД
- Флеш-сообщения (5.4) — `RedirectAttributes.addFlashAttribute("message", ...)` в
  `create`/`update`/`delete` перед `redirect:/notes`; в `list()` ничего специально читать не
  нужно — Spring сам подмешивает flash-атрибуты в `Model` следующего запроса
- `NoteService` (6.1, пакет `service`) — заведён сервисный слой, чтобы убрать дублирование
  между `NoteController` (REST) и `NoteViewController` (веб): `assertOwner` был продублирован
  в двух местах, поиск `userId` по `username` — суммарно в шести. Оба контроллера теперь
  зависят только от `NoteService`, ни один не трогает `NoteRepository`/`UserRepository`
  напрямую
- `NoteService.create` принимает `UUID ownerId` явным параметром, а не резолвит его из
  `username` сам — REST (`ownerId` из тела `NoteRequest`) и веб (владелец = текущий
  пользователь) по-прежнему разными путями решают, кто становится владельцем новой заметки;
  это осознанно не унифицировано — вне рамок задачи про сервисный слой/кэш
- `@Cacheable` (6.1) поставлен на `NoteRepository.findById`/`findAllByOwnerId`
  (переобъявлены поверх унаследованных от `MongoRepository` — только чтобы навесить
  аннотацию, реализацию Spring Data это не меняет), а НЕ на `NoteService.findByIdForOwner` —
  сознательно, по двум причинам: (1) самовызов внутри одного бина не проходит через
  AOP-прокси, кэш бы тихо не сработал; (2) кэш метода с проверкой прав опасен — попадание в
  кэш пропускает тело метода целиком, включая `assertOwner`, то есть чужой пользователь мог
  бы получить закэшированный ответ в обход 403. Кэшируется только «сырой» доступ к данным на
  уровне репозитория, а `assertOwner` в сервисе выполняется всегда
- Подтверждено логом протокола MongoDB-драйвера (`org.mongodb.driver.protocol.command=DEBUG`)
  — три одинаковых `GET /api/notes/{id}` дают один `find` вместо трёх после включения кэша
- `@CacheEvict`/`@CachePut` (6.2) поставлены на `NoteService.create`/`update`/`delete`, а не
  на репозиторий (в отличие от чтения в 6.1) — обе ловушки чтения тут не действуют: это не
  самовызов (методы вызываются извне, из контроллеров), а `@CacheEvict`/`@CachePut` в отличие
  от `@Cacheable` никогда не пропускают тело метода, значит `assertOwner` внутри не обойти.
  `create` — `@CacheEvict("notesByOwner")`; `update` — `@CachePut("notes")` (уже есть свежее
  значение из `save`, незачем стирать и ждать перечитывания) + `@CacheEvict("notesByOwner")`
  (список — целая структура, точечно не обновить); `delete` — оба `@CacheEvict`.
  `delete()` стал возвращать `NoteDocument` вместо `void` — иначе не к чему обратиться через
  `#result.ownerId()` в SpEL ключа (локальные переменные метода SpEL не видит, только
  параметры и результат)
- Безопасность кэша при чтении чужой заметки по кэшированному `id` — проверено руками
  сценарием с двумя пользователями (A владеет, B — нет): A читает заметку (кэш заполняется),
  B читает тот же `id` → 403 без утечки контента в теле ошибки; лог MongoDB подтвердил, что
  запрос B ни разу не дошёл до базы (взят из кэша), при этом `assertOwner` всё равно
  отработал и запретил доступ. То же самое после `update()` от A (кэш обновлён через
  `@CachePut`) — B снова 403, не увидел ни старую, ни новую версию контента
- Наблюдение cache hit/miss (6.3) — свой `CacheManager` (`CacheConfiguration`) с
  `Caffeine.newBuilder().recordStats()`, кэши `"notes"`/`"notesByOwner"` заведены заранее
  через конструктор `CaffeineCacheManager("notes", "notesByOwner")` (не лениво при первом
  обращении) — иначе Actuator не успел бы их увидеть на старте и не построил бы метрики;
  `management.endpoints.web.exposure.include: metrics, caches` в `application.yml` открывает
  `/actuator/metrics/cache.gets` (теги `cache`, `result: hit|miss`) и `/actuator/caches`.
  Проверено руками: 1 промах + 2 попадания после трёх одинаковых `GET` — числа в метрике
  совпали точно
- Пакеты (после 6.3) переразложены с плоских (`rest`/`web`/`service`/`domain`/`data`/`dto`/
  `utils` под корнем) на двухуровневые — тот же верхний уровень по типу (rest/web/service/…),
  но внутри разбито по предметной области (`notes`/`user`/`auth`/`security`), например
  `rest.notes.NoteController`, `service.notes.NoteService`, `dto.auth.RegisterForm`. Решение
  пользователя, ради читаемости при разрастании проекта; после проверки (`mvn clean test` +
  ручной прогон) перенос не сломал функциональность — упавший `JavanoteApplicationTests` при
  первом прогоне без `clean` был вызван не самим переносом, а тем, что Maven не подчистил
  устаревшие `.class`-файлы по старым путям в `target/classes` инкрементально
- Redis (6.4) — два `CacheManager`-бина в `CacheConfiguration`, разнесённые `@Profile`:
  `@Profile("!redis")` на Caffeine (дефолт, чтобы локальный запуск без указания профиля
  продолжал работать как раньше) и `@Profile("redis")` на `RedisCacheManager`. Активация —
  `SPRING_PROFILES_ACTIVE=redis`. Сериализация значений — `GenericJacksonJsonRedisSerializer`
  (не «2»-версия, см. ниже) с `enableUnsafeDefaultTyping()`, чтобы в JSON попадало служебное
  поле `@class` и Redis-кэш мог восстановить конкретный тип (`NoteDocument`) при чтении, а не
  обобщённую `Map`
- Ключи в Redis — формат `<имяКэша>::<ключ>` (`notes::<id заметки>`,
  `notesByOwner::<id владельца>`), значения — читаемый JSON благодаря
  `GenericJacksonJsonRedisSerializer` вместо бинарной Java-сериализации по умолчанию
- Kafka (7.1) — single-node KRaft (без Zookeeper) в `docker-compose.yml`, образ
  `apache/kafka:latest`, конфигурация через `KAFKA_*`-переменные окружения по официальному
  примеру из репозитория Apache Kafka (сверено через Context7, не по памяти — конфигурация
  KRaft чувствительна к точным именам переменных). Два клиентских listener'а:
  `PLAINTEXT_HOST` (порт 9092, для хоста — пока `core-api` не в докере) и `PLAINTEXT` (порт
  19092, только внутри docker-сети, на будущее для Этапа 10); `CONTROLLER` — служебный,
  наружу не торчит
- Топик `note-events` (7.2) — создан вручную через `kafka-topics.sh --create`
  (`--partitions 1 --replication-factor 1`, реплика больше 1 физически невозможна на одном
  брокере); данные Kafka персистентны через volume `kafka-data:/tmp/kafka-logs`
- `NoteCreatedEvent` (7.3, модуль `common`, пакет `event.notes`) — record (`noteId`,
  `ownerId`, `title`, `createdAt`), только JDK-типы, никаких Spring/Jackson зависимостей —
  `common/pom.xml` осознанно остаётся пустым. `content` заметки в событие не включён —
  `push-service` (Этап 8) не будет иметь прямого доступа к MongoDB, событие для него
  единственный источник данных, а тащить полный текст заметки через Kafka ради push
  избыточно и по объёму, и по приватности; как сериализовать событие в байты для Kafka —
  решение уровня продюсера/консьюмера (7.4/Этап 8), не самого класса-контракта
- Продюсер (7.4) — `spring-boot-starter-kafka` (не голый `spring-kafka`, см. ниже),
  `value-serializer: JacksonJsonSerializer` (не `JsonSerializer` — та же история с Jackson
  2/3, что и в Redis, сверено через актуальную документацию `spring-kafka` 4.1, а не по
  памяти). Публикация — из `NoteService.create()`, после `noteRepository.save(...)`, не до
  (см. ниже); ключ сообщения — `ownerId.toString()`, чтобы события одного владельца
  сохраняли порядок при появлении нескольких партиций в будущем. Имя топика вынесено в
  константу `NoteEventsTopics.NOTE_CREATED` в `common` — понадобится и `push-service`
- Тест продюсера (7.5, `NoteEventProducerIT`) — Testcontainers Kafka
  (`org.testcontainers.kafka.KafkaContainer`, поддерживает `apache/kafka`, тот же образ,
  что в `docker-compose.yml`; старый generic `org.testcontainers.containers.KafkaContainer`
  — deprecated), не `@EmbeddedKafka` — ради единообразия с уже существующими
  `NotesRepositoryIT`/`UserRepositoryIT`. `@ServiceConnection` сам подставляет
  `spring.kafka.bootstrap-servers` контейнера. Тест вызывает реальный
  `noteService.create(...)` и читает результат обратно тем же `JacksonJsonDeserializer`,
  каким `push-service` будет читать это событие по-настоящему — проверяет не только факт
  публикации, но и что формат действительно десериализуется обратно
- Модуль `push-service` (8.1) — скелет уже существовал с Этапа 0 (`pom.xml`, зависимость на
  `common`, `spring-boot-maven-plugin`); добавлены `spring-boot-starter-webmvc` +
  `spring-boot-starter-actuator` (та же пара, что была первым шагом `core-api` в 0.4), свой
  `application.yml` с портом **8081** (8080 занят `core-api`), точка входа
  `PushServiceApplication` — пакет `io.gudmian.javanote`, совпадает с `core-api`, но не
  конфликтует: модули собираются в разные jar
- Kafka-консьюмер (8.2) — `spring-boot-starter-kafka` (та же зависимость, что и у продюсера
  в `core-api`, даёт и `KafkaTemplate`, и `@KafkaListener`), `NoteEventListener` пока просто
  логирует полученное событие (сама отправка push — 8.4). Настройки консьюмера в
  `application.yml`: `group-id: push-service`, `auto-offset-reset: earliest` (dev-удобство —
  видеть уже накопленные события при перезапуске; для прода обычно `latest`),
  `spring.json.use.type.headers: false` + явный `spring.json.value.default.type` — не
  доверять имени класса из заголовка Kafka-сообщения, всегда десериализовать в
  `NoteCreatedEvent` явно (та же защитная логика, что и `spring.json.trusted.packages`).
  Строки констант `JacksonJsonDeserializer` сверены по исходникам `spring-kafka` 4.1, не по
  памяти — класс новый (Jackson 3), есть риск, что namespace проперти изменился
- Playbook для ручной проверки — `scripts/curl-playbook.sh` (bash-функции для headless
  прогонов через терминал) и `http/javanote.http` + `http/http-client.env.json` (IntelliJ
  HTTP Client — запросы кликабельны прямо в IDE, `token`/`id` автоматически прокидываются
  между запросами через `client.global.set(...)` в response-хендлерах). Не часть
  `core-api`/`push-service`, чисто вспомогательные файлы разработчика
- `POST /api/push/register` (8.3) — по требованию пользователя хранение токенов настоящее
  (Postgres), а не in-memory, и **отдельный** Postgres-инстанс (`postgres-push`, свой
  volume `postgres-push-data`, порт 5434) — не общая база с `core-api`, ради настоящей
  изоляции между сервисами (иначе пришлось бы разводить общую таблицу
  `flyway_schema_history`, а тут просто разные физические БД, конфликтовать нечему).
  `push_tokens`: `id UUID` (`@UuidGenerator`, как `UserEntity`) — отдельный технический PK,
  `UNIQUE(owner_id, token)` — по требованию пользователя, чтобы у владельца при отправке не
  задваивались токены. Из-за generated PK (в отличие от варианта «token как PK») `save()`
  не делает merge сам по себе — идемпотентность реализована явно:
  `existsByOwnerIdAndToken` перед вставкой в `DeviceTokenStore.register()`. Подтверждено
  руками: повторная регистрация одного и того же `(owner_id, token)` не создаёt вторую
  строку; прямая SQL-вставка дубликата подтвердила, что `UNIQUE`-ограничение реально
  работает и на уровне БД (`duplicate key value violates unique constraint`)
- `PushRegisterRequest` изначально называл поле владельца просто `id` — переименовано в
  `userId` для ясности и консистентности с `PLAN.md`/остальным проектом (`NoteRequest.ownerId`
  и т.п. — поля называются по смыслу, не generic `id`)
- Actuator (9.1) — `management.endpoint.health.show-details: always` и `build-info` goal
  `spring-boot-maven-plugin` добавлены на обоих сервисах; подтверждено curl'ом: `db`/`mongo`/
  `redis` в `/actuator/health` реально проверяются (остановка контейнера `mongo` вживую даёт
  `DOWN`, не просто рапортует `UP`), `/actuator/info` отдаёт `build.version`/`build.time`,
  `/actuator/metrics/http.server.requests` считает реальные запросы. **Kafka health indicator
  сознательно не реализован** — сверено по документации Spring Boot 4.1 (Context7): список
  авто-конфигурируемых `HealthIndicator` включает Cassandra/Couchbase/Mongo/Neo4j/JMS/
  RabbitMQ/Elasticsearch/Hazelcast/Redis/LDAP/Mail/disk space, Kafka в нём нет — Spring Boot
  не поставляет такой индикатор из коробки, а свой писать (через `KafkaAdmin`/
  `AdminClient.describeCluster()`) решено не делать; критерий PLAN.md «health индикаторы
  Postgres/Mongo/Kafka» закрыт частично осознанно, без Kafka
- Kafka-метрики (обсуждали отдельно от health) при этом работают полностью автоматически, без
  единой строчки конфигурации: `kafka.producer.*`/`kafka.consumer.*` (сырые метрики клиента,
  через авто-регистрируемые `MicrometerProducerListener`/`MicrometerConsumerListener`) и
  `spring.kafka.template`/`spring.kafka.listener` (таймеры через Micrometer Observation).
  Нюанс, найденный экспериментально: `kafka.producer.*` на core-api появляются не сразу при
  старте, а только после первой реальной отправки — `KafkaProducer` создаётся лениво, в
  отличие от consumer'а на push-service, который подписывается на топик сразу при старте
  (`@KafkaListener`) и его `kafka.consumer.*` видны сразу
- Найдена (пока не исправлена) мелкая проблема в `scripts/curl-playbook.sh`: функция `login()`
  без явного аргумента берёт `${1:-$USERNAME}`, а `$USERNAME` в этом shell — read-only
  системная переменная (та же ловушка, что уже описана ниже про регистрацию), из-за чего
  `login` тихо подставляет логин macOS вместо тестового пользователя и падает с 401 —
  воспроизведено при проверке Kafka-метрик, обойдено прямыми curl-вызовами с явными
  переменными; исправление (`login`/`register` переименовать переменную окружения) отложено
- MDC (9.2) — `MdcLoggingFilter extends OncePerRequestFilter`, `@Component` (не часть
  Spring Security цепочки, как `JwtAuthenticationFilter` — обычный сервлетный фильтр верхнего
  уровня, без явного `@Order` получает `LOWEST_PRECEDENCE` и потому выполняется уже после всей
  цепочки Spring Security, `SecurityContextHolder` на этот момент заполнен). `requestId` —
  `UUID.randomUUID()`, дублируется в заголовок ответа `X-Request-Id`; `userId` кладётся только
  если `Authentication` есть и не `AnonymousAuthenticationToken`. `MDC.clear()` в `finally`.
  Паттерн лога (`logging.pattern.console` в `application.yml`) — полный дефолтный паттерн
  Spring Boot с добавленным `%X{requestId}/%X{userId}` внутрь, не замена паттерна целиком
  (замена целиком — первая версия — роняла всю остальную информацию строки лога). Подтверждено
  живым логом (временно поднятым до DEBUG на `org.springframework.web`, т.к. в прикладном коде
  пока нет ни одного `log.info`): разные запросы на разных worker-потоках несут каждый свой
  `requestId`, аутентифицированный запрос — ещё и `userId`, значения не текут между запросами
  (`MDC.clear()` в `finally` отрабатывает). **push-service сознательно не получил MDC** —
  решение пользователя, концепция понята на core-api; при этом стоит помнить (если вернутся к
  этому позже) — `NoteEventListener` вызывается из потока Kafka-консьюмера, а не через
  сервлетный фильтр, поэтому обычный `Filter`-подход там в принципе не сработал бы, MDC
  пришлось бы класть вручную прямо в `@KafkaListener`-методе
- Этап 9 сокращён по решению пользователя — подпункты 9.3 (интеграционные тесты на весь стек)
  и 9.4 (отчёт покрытия JaCoCo) убраны из `PLAN.md`/`STATUS.md` целиком, не отложены; этап
  закрыт по факту 9.1+9.2

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
- `notes/form.html` с захардкоженным `th:action="@{/notes}"` (первая версия 5.2) — открытие
  `/notes/{id}/edit` и сабмит формы фактически создавали вторую, новую заметку вместо
  обновления существующей (action всегда указывал на create-эндпоинт, `update()` вообще не
  вызывался); подтверждено руками — в списке оказывались обе заметки, старая нетронутая и
  «отредактированная» с новым `id`. Решение — `action` стал условным по `noteForm.id`, `id`
  прокинут через скрытое поле формы
- `NoteViewController.update()` изначально не имел `@Valid`/`BindingResult` (валидацию
  добавили только в `create()`) — пустой `title` при редактировании молча сохранялся;
  подтверждено руками (`<td></td>` в списке после «редактирования»)
- Копипаст-опечатка — после добавления флеш-сообщений `update()` показывал «Заметка создана»
  вместо «Заметка обновлена» (текст скопирован из `create()` и не поправлен)
- Собственная ловушка при ручном тестировании через curl — переменная шелла `$USERNAME`
  защищена/read-only в этом окружении и тихо не даёт себя переприсвоить: скрипт с
  `USERNAME="ivan..."` реально отправлял `d.guba` (текущий логин macOS), из-за чего на
  секунду показалось, что регистрация не сохраняет пользователя в БД — на деле баг был в
  тестовом скрипте (имя переменной совпало с системной), не в коде проекта; после
  переименования переменной (`TESTUSER`) всё воспроизвелось чисто
- `@Cacheable` на `NoteRepository` (6.1) сначала не давал эффекта вообще — нигде в проекте не
  было `@EnableCaching`, без неё Spring не поднимает AOP-прокси, читающий кэш-аннотации, и они
  остаются мёртвыми метаданными без единой ошибки при старте. Подтверждено логом протокола
  MongoDB-драйвера — три одинаковых `GET /api/notes/{id}` давали три одинаковых `find` в базу;
  после добавления `@EnableCaching` на `JavanoteApplication` тот же тест дал один `find`
  вместо трёх
- Перенос классов по пакетам (после 6.3) — при первом `mvn test` без `clean`
  `JavanoteApplicationTests` падал с `ConflictingBeanDefinitionException` (два бина
  `authController` — старый и новый путь). Не баг переноса, а инкрементальная сборка Maven:
  `mvn compile` не удаляет устаревшие `.class`-файлы по старым путям в `target/classes`,
  только `mvn clean` их выметает. После `clean test` — зелёный
- `NoteControllerTests` не запускался с самого Этапа 6.1 (`NoteService`) — обнаружено при
  ревью переноса пакетов (6.3): все 12 тестов падали, тест мокал `NoteRepository`/
  `UserRepository`, хотя `NoteController` зависит только от `NoteService`. Переписан на мок
  `NoteService` напрямую (`given(noteService.findByIdForOwner(...))` и т.п. вместо мока
  репозиториев). Заодно всплыла вторая, самостоятельная причина падений — `@EnableCaching` на
  `JavanoteApplication` требует бин `CacheManager` при старте контекста (даже в `@WebMvcTest`
  слайсе, где реальный `NoteService`/`NoteRepository` не поднимаются вообще — они замоканы),
  а `CacheConfiguration` слайс-тестом не подхватывается (та же история, что раньше была с
  `SecurityConfig`) — добавлен `@MockitoBean CacheManager`. После правок — 12/12 зелёных
- `GenericJackson2JsonRedisSerializer` не резолвился в IDE (6.4) — этот класс целиком на
  классическом Jackson 2 (`com.fasterxml.jackson.databind`), а он в проекте есть только
  транзитивно через `jjwt-jackson` и только в scope `runtime`, не `compile` — компилятору/IDE
  не виден. `GenericJacksonJsonRedisSerializer` (без «2») — на Jackson 3 (`tools.jackson.*`),
  который у проекта честный `compile`-scope через `spring-boot-starter-jackson` — им и
  пользуемся, это не альтернатива, а единственный реально рабочий вариант здесь
- `RedisCacheManager.builder()` без аргумента (6.4) — есть такая перегрузка, но без
  `RedisConnectionFactory`/`RedisCacheWriter` `.build()` кидает
  `IllegalStateException: CacheWriter must not be null`; приложение падало на старте с
  профилем `redis`. Подтверждено запуском. Решение — просить Spring внедрить
  `RedisConnectionFactory` параметром метода и передавать в `RedisCacheManager.builder(connectionFactory)`
- `GenericJacksonJsonRedisSerializer.builder().build()` без `enableUnsafeDefaultTyping()`
  (6.4) — сериализация в Redis проходила молча, а вот при чтении (второй `GET`, попадание в
  кэш) — `ClassCastException: LinkedHashMap cannot be cast to NoteDocument`. Без default
  typing в JSON не попадает информация о классе, и Jackson при чтении восстанавливает
  обобщённую `Map` вместо `NoteDocument`. Подтверждено логом приложения и `redis-cli GET` —
  после фикса в значении появилось поле `"@class": "...NoteDocument"`
- Volume для Kafka (7.2) сначала примонтировали на неверный путь (`/var/lib/kafka/data`) —
  образ `apache/kafka` реально пишет данные в `/tmp/kafka-logs` (`log.dirs` в конфиге `null`,
  что означает классический дефолт Kafka); проверено руками через `find`/`docker exec`, не по
  документации. После исправления пути контейнер не смог стартовать —
  `AccessDeniedException: /tmp/kafka-logs/bootstrap.checkpoint.tmp`: свежий именованный volume
  Docker создаёт с владельцем `root:root`, а процесс в образе бежит от непривилегированного
  `appuser` (`uid=1000`, проверено `docker run ... id appuser`). Решение — разово
  `docker run --rm -v javanote_kafka-data:/data alpine chown -R 1000:1000 /data`, дальше
  Docker сам сохраняет владельца между пересозданиями контейнера. Топик `note-events`
  пришлось создать заново (старый жил в эпемерном слое удалённого контейнера) — после фикса
  подтверждено, что переживает полный `stop`/`rm`/`up`
- Голый `spring-kafka` (7.4) — `application.yml` не признавал `spring.kafka.*`-свойства.
  Причина та же, что и с Redis: в Spring Boot 4.x у каждой интеграции своя пара
  starter+autoconfigure (`spring-boot-starter-data-redis`/`spring-boot-data-redis` и т.п.);
  для Kafka нужен `spring-boot-starter-kafka`, а не голый `spring-kafka` — только через
  starter подтягивается `spring-boot-kafka` (autoconfigure-модуль с `KafkaProperties`,
  который и биндит `spring.kafka.*`). Подтверждено по составу `spring-boot-dependencies` BOM
- `NoteService.create()` (7.4) публиковал событие в Kafka **до** `noteRepository.save(...)`,
  а не после — при сбое записи (проверено вживую: останавливал MongoDB) событие всё равно
  уходило в Kafka для заметки, которой не существует в базе («заметки-призраки», в тесте —
  дважды). Решение — переставить публикацию после `save(...)` и строить `NoteCreatedEvent`
  из уже сохранённого результата; повторной остановкой MongoDB подтверждено — событие больше
  не публикуется при неудачном сохранении, а при успешном — по-прежнему публикуется
- Координаты зависимости Firebase (8.4) — `com.google.firebase:firebase-messaging` не
  существует в Maven Central под таким groupId/artifactId (это отдельная Android-библиотека
  на `maven.google.com`, не имеющая отношения к серверной интеграции); нужная зависимость —
  `com.google.firebase:firebase-admin:9.9.0`, подтверждено через `mvn dependency:get` и
  прямой распаковкой jar (`unzip -l`, класс `FirebaseMessaging` реально внутри)
- `application.yml` push-service (8.4) — `firebase:` был вложен на том же уровне, что
  `kafka:`/`datasource:`/`jpa:`, то есть **под** `spring:`; реальная проперти получалась
  `spring.firebase.credentials-path`, а код читает `firebase.credentials-path`
  (`@Value("${firebase.credentials-path}")`) — несовпадение. Баг маскировался тем, что
  переменные окружения в Spring имеют более высокий приоритет, чем `application.yml`, и
  участвуют в relaxed binding: OS-переменная `FIREBASE_CREDENTIALS_PATH` сама по себе
  матчится на проперти `firebase.credentials-path`, в обход (сломанного) значения из YAML.
  Подтверждено запуском без переменной окружения после переноса `firebase:` на верхний
  уровень (вне `spring:`) — ошибка `Could not resolve placeholder 'FIREBASE_CREDENTIALS_PATH'
  in value "${FIREBASE_CREDENTIALS_PATH}" <-- "${firebase.credentials-path}"` показывает, что
  `firebase.credentials-path` теперь резолвится из YAML, а не случайно из env
- Чистка невалидных токенов (8.5) — `NoteEventListener` удаляет токен из `push_tokens` только
  при `MessagingErrorCode.UNREGISTERED` (был нормальным, но устройство/приложение исчезло),
  сознательно НЕ трогает `INVALID_ARGUMENT` (синтаксически некорректный токен — это баг на
  стороне клиента при регистрации, а не естественное протухание; решили не маскировать такие
  случаи тихим удалением). Подтверждено живым вызовом реального Firebase API (не догадкой):
  синтаксически невалидный токен (в т.ч. специально собранный по формату настоящего FCM-токена
  случайный) всегда возвращает `INVALID_ARGUMENT` с сообщением "The registration token is not
  a valid FCM registration token", а не `UNREGISTERED` — подделать `UNREGISTERED` без реального
  ранее выданного Firebase SDK токена невозможно, т.к. Firebase хранит происхождение токена на
  своей стороне. Проверено: код срабатывает верно (лог `Push sent for owner ...: 0 success, 1
  failure`), запись в БД не удаляется на `INVALID_ARGUMENT` (негативный путь подтверждён), сам
  API (`SendResponse`, `BatchResponse`, `MessagingErrorCode`, `getMessagingErrorCode()`)
  проверен по декомпиляции реального jar `firebase-admin:9.9.0`, а не по памяти. Прямая
  проверка положительного пути (реальное удаление по `UNREGISTERED`) требует настоящего
  Firebase-клиента на устройстве — вне рамок бэкенд-проекта, отложена как принятый риск

## Как продолжить

Запустите `/step`. Команда прочитает чек-лист выше, напомнит, на чём остановились
(или предложит следующий пункт), поможет разобраться и, после вашего подтверждения, что
пункт/этап сделан и проверен, обновит статус здесь и перейдёт дальше.
