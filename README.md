# Library Lending Service

REST API для учета книг, клиентов библиотеки и выдач книг.

Сервис позволяет:

- регистрировать и авторизовывать клиентов;
- получать JWT и работать с защищенными эндпоинтами;
- создавать, получать, искать и обновлять книги;
- получать, искать и обновлять клиентов;
- создавать выдачи книг, получать выдачу по ID и возвращать книгу;
- получать отчет по активным читателям;
- ускорять чтение данных через Redis cache-aside;
- ограничивать частоту запросов через Redis rate limiter.

## Что реализовано

- Java 8, Spring Boot 2.7.18, Maven.
- Spring Web REST API.
- Простой web UI: `http://localhost:8090/`.
- PostgreSQL 14, Hibernate, Spring Data JPA.
- Liquibase migrations и seed-данные.
- JWT security для клиентов.
- Роли `ADMIN` и `USER`.
- Redis cache-aside для книг, клиентов и выдач.
- Точечная инвалидация cache keys выдач по `clientId` и `bookId`.
- Redis rate limiter: `60` запросов за `1m` на IP.
- Swagger UI и OpenAPI.
- MapStruct и DTO validation.
- Глобальная обработка ошибок.
- Unit tests, controller tests, service/repository integration tests.
- Testcontainers для интеграционных тестов.
- `requests.http` с позитивными и негативными HTTP-сценариями.

## Технологии

- Java 8
- Spring Boot 2.7.18
- Spring Web
- Spring Validation
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- Redis
- Liquibase
- JWT 
- MapStruct
- SpringDoc OpenAPI / Swagger UI
- JUnit 5
- Mockito
- Testcontainers
- Maven
- Docker Compose

## Инициализация данных

При старте приложение создает клиентов по умолчанию, если их еще нет:

| Role | Login | Password |
| --- | --- | --- |
| `ADMIN` | `admin` | `admin_password` |
| `USER` | `pavlov` | `pavlov_password` |

Дополнительные demo-данные загружаются Liquibase seed-файлами:

- `004-seed-books.sql`
- `005-seed-clients.sql`
- `006-seed-lendings.sql`

## Роли и доступы

### Public

Не требуют JWT:

- `POST /api/clients`
- `POST /api/clients/auth`
- `GET /api/clients/debug/auth`
- Swagger/OpenAPI и статический web UI

### USER

Доступно с JWT пользователя `USER` или `ADMIN`:

- `GET /api/books/{id}`
- `GET /api/books/search`
- `POST /api/lendings`
- `GET /api/lendings/{id}`
- `PATCH /api/lendings/{id}/return`
- `GET /api/lendings/active-readers`

### ADMIN

Доступно только с JWT пользователя `ADMIN`:

- `GET /api/clients/{id}`
- `GET /api/clients/search`
- `PUT /api/clients/{id}`
- `POST /api/books`
- `PUT /api/books/{id}`

## API

Подробные параметры, обязательность полей и схемы ответов описаны в Swagger: `http://localhost:8090/swagger-ui.html`.

### Clients

- `POST /api/clients` - регистрация клиента. Public.
- `POST /api/clients/auth` - авторизация и получение JWT. Public.
- `GET /api/clients/debug/auth` - диагностика текущего пользователя. Public; опционально принимает `Authorization: Bearer <jwt>`.
- `GET /api/clients/{id}` - получить клиента по ID. Требуется `ADMIN`.
- `GET /api/clients/search` - поиск клиентов по `fullName` с пагинацией `pageNumber`, `pageSize`. Требуется `ADMIN`.
- `PUT /api/clients/{id}` - обновить клиента. Требуется `ADMIN`.

### Books

- `POST /api/books` - создать книгу. Требуется `ADMIN`.
- `GET /api/books/{id}` - получить книгу по ID. Требуется `USER` или `ADMIN`.
- `GET /api/books/search` - поиск книг по `title`, `author` с пагинацией `pageNumber`, `pageSize`. Требуется `USER` или `ADMIN`.
- `PUT /api/books/{id}` - обновить книгу. Требуется `ADMIN`.

### Lendings

- `POST /api/lendings` - выдать книгу клиенту. Требуется `USER` или `ADMIN`; `takenAt` можно не передавать, тогда используется текущее время.
- `GET /api/lendings/{id}` - получить выдачу по ID. Требуется `USER` или `ADMIN`.
- `PATCH /api/lendings/{id}/return` - вернуть книгу. Требуется `USER` или `ADMIN`.
- `GET /api/lendings/active-readers` - отчет по активным читателям с пагинацией `pageNumber`, `pageSize`. Требуется `USER` или `ADMIN`.

## Сценарий работы с API

1. Запустить PostgreSQL, Redis и приложение через Docker Compose.
2. Зарегистрировать клиента через `POST /api/clients` или использовать default user.
3. Авторизоваться через `POST /api/clients/auth`.
4. Скопировать JWT из поля `jwt`.
5. Передавать JWT в защищенные запросы:

```http
Authorization: Bearer <jwt>
```

6. Для административных операций использовать `admin / admin_password`.
7. Для пользовательских операций можно использовать `pavlov / pavlov_password`.
8. Для ручной проверки можно запускать запросы из `requests.http`.

## Swagger и OpenAPI

После запуска доступны:

- Web UI: `http://localhost:8090/`
- Swagger UI: `http://localhost:8090/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8090/v3/api-docs`
- OpenAPI YAML: `http://localhost:8090/openapi.yaml`

В Swagger нажмите `Authorize`, вставьте JWT в формате:

```text
Bearer <jwt>
```

## Конфигурация

Все runtime-настройки вынесены в `.env`. Пример есть в `.env.example`.

Основные переменные:

| Variable | Description |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Активный Spring profile |
| `LIBRARY_APPLICATION_NAME` | Имя приложения |
| `LIBRARY_SERVER_PORT` | Внешний HTTP-порт приложения |
| `POSTGRES_LIBRARY_DB_PORT` | Внешний порт PostgreSQL |
| `POSTGRES_LIBRARY_DB` | Имя БД |
| `POSTGRES_LIBRARY_USER` | Пользователь БД |
| `POSTGRES_LIBRARY_PASSWORD` | Пароль БД |
| `LIBRARY_DB_URL` | JDBC URL для локального запуска без compose |
| `LIBRARY_DB_DRIVER_CLASS_NAME` | JDBC driver |
| `LIBRARY_DB_SCHEMA` | DB schema |
| `LIBRARY_LIQUIBASE_ENABLED` | Включить/выключить Liquibase |
| `LIBRARY_REDIS_HOST` | Redis host для локального запуска |
| `LIBRARY_REDIS_PORT` | Порт Redis |
| `LIBRARY_REDIS_DB` | Номер базы Redis |
| `LIBRARY_REDIS_TIMEOUT` | Таймаут Redis |
| `LIBRARY_DEFAULT_PAGE_NUMBER` | Номер страницы по умолчанию |
| `LIBRARY_DEFAULT_PAGE_SIZE` | Размер страницы по умолчанию |
| `LIBRARY_MAX_PAGE_SIZE` | Максимальный размер страницы |
| `LIBRARY_CACHE_BOOKS_TTL` | Время жизни кеша книг |
| `LIBRARY_CACHE_CLIENTS_TTL` | Время жизни кеша клиентов |
| `LIBRARY_CACHE_LENDINGS_TTL` | Время жизни кеша выдач |
| `LIBRARY_CACHE_BOOK_KEY_PREFIX` | Префикс Redis-ключей для кеша книг |
| `LIBRARY_CACHE_CLIENT_KEY_PREFIX` | Префикс Redis-ключей для кеша клиентов |
| `LIBRARY_CACHE_LENDING_KEY_PREFIX` | Префикс Redis-ключей для кеша выдач |
| `LIBRARY_CACHE_LENDING_BY_CLIENT_KEY_PREFIX` | Префикс Redis-ключей для индекса выдач по `clientId` |
| `LIBRARY_CACHE_LENDING_BY_BOOK_KEY_PREFIX` | Префикс Redis-ключей для индекса выдач по `bookId` |
| `LIBRARY_SEARCH_EMPTY_FILTER_VALUE` | Значение фильтра, если поисковый параметр не передан |
| `LIBRARY_RATE_LIMIT_ENABLED` | Включить/выключить ограничение частоты запросов |
| `LIBRARY_RATE_LIMIT_CAPACITY` | Количество запросов в окно |
| `LIBRARY_RATE_LIMIT_WINDOW` | Окно ограничения частоты запросов |
| `LIBRARY_RATE_LIMIT_KEY_PREFIX` | Префикс Redis-ключей для ограничения частоты запросов |
| `LIBRARY_OPENAPI_SERVER_URL` | Адрес сервера в OpenAPI |
| `JWT_SECRET_KEY` | Секретный ключ для подписи JWT |
| `JWT_LIFETIME_MS` | Время жизни JWT в миллисекундах |
| `APP_TIMEZONE` | Часовой пояс приложения и контейнеров |
| `LOGGING_LEVEL_ORG_HIBERNATE_SQL` | Уровень логирования Hibernate SQL |

Пример `.env`:

```env
SPRING_PROFILES_ACTIVE=dev
LIBRARY_APPLICATION_NAME=library-lending-service
LIBRARY_SERVER_PORT=8090

POSTGRES_LIBRARY_DB_PORT=5545
POSTGRES_LIBRARY_DB=library_lending_db
POSTGRES_LIBRARY_USER=library_user
POSTGRES_LIBRARY_PASSWORD=library_pass_2026
LIBRARY_DB_URL=jdbc:postgresql://localhost:5545/library_lending_db
LIBRARY_DB_DRIVER_CLASS_NAME=org.postgresql.Driver
LIBRARY_DB_SCHEMA=public
LIBRARY_LIQUIBASE_ENABLED=true

LIBRARY_REDIS_HOST=localhost
LIBRARY_REDIS_PORT=6386
LIBRARY_REDIS_DB=0
LIBRARY_REDIS_TIMEOUT=2s

LIBRARY_DEFAULT_PAGE_NUMBER=0
LIBRARY_DEFAULT_PAGE_SIZE=10
LIBRARY_MAX_PAGE_SIZE=100

LIBRARY_CACHE_BOOKS_TTL=45s
LIBRARY_CACHE_CLIENTS_TTL=45s
LIBRARY_CACHE_LENDINGS_TTL=30s
LIBRARY_CACHE_BOOK_KEY_PREFIX=book:
LIBRARY_CACHE_CLIENT_KEY_PREFIX=client:
LIBRARY_CACHE_LENDING_KEY_PREFIX=lending:
LIBRARY_CACHE_LENDING_BY_CLIENT_KEY_PREFIX=lending:client:
LIBRARY_CACHE_LENDING_BY_BOOK_KEY_PREFIX=lending:book:

LIBRARY_SEARCH_EMPTY_FILTER_VALUE=

LIBRARY_RATE_LIMIT_ENABLED=true
LIBRARY_RATE_LIMIT_CAPACITY=60
LIBRARY_RATE_LIMIT_WINDOW=1m
LIBRARY_RATE_LIMIT_KEY_PREFIX=rate-limit:

LIBRARY_OPENAPI_SERVER_URL=http://localhost:8090

JWT_SECRET_KEY=GRO16WVD3nAoqU1dqzcAVblU1m4p0oWpiyU-MSQ0i5XQoOcFuOowoPTMyAq9KigqcdFWXrvCv-MVSc-E1rycjw
JWT_LIFETIME_MS=86400000

APP_TIMEZONE=Europe/Moscow
LOGGING_LEVEL_ORG_HIBERNATE_SQL=INFO
```

## Сборка и компиляция

Проверить компиляцию main и test sources без запуска тестов:

```bash
./mvnw -DskipTests test-compile
```

Windows PowerShell:

```powershell
.\mvnw.cmd -DskipTests test-compile
```

Собрать jar:

```bash
./mvnw clean package
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

Запустить собранный jar без Docker в профиле `local`:

```bash
java -jar target/library-lending-service-0.0.1-SNAPSHOT.jar
```

Windows PowerShell:

```powershell
java -jar target\library-lending-service-0.0.1-SNAPSHOT.jar
```

Запустить собранный jar с PostgreSQL и Redis:

```bash
SPRING_PROFILES_ACTIVE=dev java -jar target/library-lending-service-0.0.1-SNAPSHOT.jar
```

Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE='dev'; java -jar target\library-lending-service-0.0.1-SNAPSHOT.jar
```

## Локальный запуск без Docker

По умолчанию приложение запускается в профиле `local`. Этот режим рассчитан на проверку через Maven Wrapper без Docker: используется in-memory H2, Liquibase отключен, Redis cache-aside и rate limiter отключены. Дефолтные клиенты `admin / admin_password` и `pavlov / pavlov_password` создаются при старте, несколько книг загружаются из `db/local-data.sql`.

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

После старта доступны:

- Web UI: `http://localhost:8090/`
- Swagger UI: `http://localhost:8090/swagger-ui.html`
- H2 console: `http://localhost:8090/h2-console`

Настройки H2 console:

- JDBC URL: `jdbc:h2:mem:library_lending_service`
- User: `library_user`
- Password: `library_password`

Если нужно запустить приложение локально именно с PostgreSQL и Redis без Docker Compose, поднимите PostgreSQL и Redis отдельно, выставьте переменные из `.env` и запустите профиль `dev`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

## Docker Compose

В проекте есть `Dockerfile`, `.env`, `.env.example` и `docker-compose.yaml`.

### Запуск

```bash
docker compose --env-file .env up --build -d
```

### Остановка контейнеров без удаления

```bash
docker compose --env-file .env stop
```

### Повторный старт остановленных контейнеров

```bash
docker compose --env-file .env start
```

### Остановка и удаление контейнеров

```bash
docker compose --env-file .env down
```

### Полный сброс БД и Redis

```bash
docker compose --env-file .env down -v
docker compose --env-file .env up --build -d
```

### Удаление orphan-контейнеров

```bash
docker compose --env-file .env down --remove-orphans
```

Порты по умолчанию:

- app: `8090`
- PostgreSQL: `5545`
- Redis: `6386`

## Как проверить работоспособность

1. Запустить стек:

```bash
docker compose --env-file .env up --build -d
```

2. Проверить контейнеры:

```bash
docker ps
```

Ожидаемые контейнеры:

- `library-lending-service`
- `library-lending-db`
- `library-lending-redis`

3. Открыть web UI:

```text
http://localhost:8090/
```

4. Открыть Swagger:

```text
http://localhost:8090/swagger-ui.html
```

5. Получить JWT:

```http
POST http://localhost:8090/api/clients/auth
Content-Type: application/json

{
  "login": "admin",
  "password": "admin_password"
}
```

6. Проверить текущую аутентификацию:

```http
GET http://localhost:8090/api/clients/debug/auth
Authorization: Bearer <jwt>
```

Ожидаемый ответ:

```json
{
  "name": "admin",
  "authorities": ["ADMIN"]
}
```

7. Запустить позитивные и негативные сценарии из `requests.http`.

8. Проверить health по логам:

```bash
docker logs --tail 100 library-lending-service
```

## Миграции и индексы

Liquibase changelog:

- `001-create-table-books.sql`
- `002-create-table-clients.sql`
- `003-create-table-lendings.sql`
- `004-seed-books.sql`
- `005-seed-clients.sql`
- `006-seed-lendings.sql`
- `007-drop-redundant-client-indexes.sql`

В таблице `lendings` используется `id BIGSERIAL PRIMARY KEY`; партиционирование не применяется.

Основные индексы:

- `books`: GIN trigram-индексы по `lower(title)` и `lower(author)` ускоряют поиск книг без учета регистра и по части строки. Уникальность `isbn` контролируется отдельным ограничением.
- `clients`: GIN trigram-индекс по `lower(full_name)` ускоряет поиск клиентов по ФИО без учета регистра и по части строки. Уникальность `login` контролируется отдельным ограничением.
- `lendings`: первичный ключ по `id`, обычные индексы по `client_id` и `book_id`, а также частичные индексы для активных выдач, где `returned_at is null`.


## Cache Aside

Чтение книг, клиентов и выдач по ID сначала проверяет Redis. При cache miss данные читаются из PostgreSQL и сохраняются в Redis с TTL.

Префиксы cache keys вынесены в `app.cache.*` / `.env`, чтобы менять namespace Redis без правки кода.

Для выдач дополнительно используются Redis-индексы кеша:

- `lending:client:{clientId}`
- `lending:book:{bookId}`

Они нужны, чтобы при обновлении книги или клиента инвалидировать только связанные lending cache keys, а не сбрасывать весь cache выдач.

## Тесты

Есть несколько уровней тестов:

- unit tests для service layer;
- `web_only` tests для controller layer;
- `all_context` tests для controller layer;
- integration tests для service layer;
- repository integration tests;
- abstract classes для JPA/Testcontainers infrastructure.

Быстрый запуск тестов без Docker:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Полный запуск, включая Testcontainers-тесты с PostgreSQL:

```bash
./mvnw verify -Pintegration-tests
```

Windows PowerShell:

```powershell
.\mvnw.cmd verify -Pintegration-tests
```

Быстрая проверка компиляции без запуска тестов:

```powershell
.\mvnw.cmd -DskipTests test-compile
```
