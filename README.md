# Online Learning Platform

Spring Boot-приложение для онлайн-обучения: управление курсами, модулями, уроками и заданиями, загрузка файлов, REST API и веб-интерфейс на Thymeleaf.

---

## Содержание

- [Возможности](#возможности)
- [Стек технологий](#стек-технологий)
- [Требования](#требования)
- [Быстрый старт (Docker)](#быстрый-старт-docker)
- [Локальный запуск](#локальный-запуск)
- [Создание admin-аккаунта](#создание-admin-аккаунта)
- [Роли и доступ](#роли-и-доступ)
- [Веб-интерфейс](#веб-интерфейс)
- [REST API](#rest-api)
- [Переменные окружения](#переменные-окружения)
- [Docker: управление и данные](#docker-управление-и-данные)
- [Тесты](#тесты)
- [Структура проекта](#структура-проекта)
- [Устранение неполадок](#устранение-неполадок)

---

## Возможности

- Регистрация и авторизация пользователей (форма + REST)
- Роли: **ADMIN** и **STUDENT**
- CRUD для курсов, модулей, уроков и заданий (REST + админ-панель)
- Запись студентов на курсы (enrollments)
- Отправка и проверка домашних заданий (submissions)
- Отслеживание прогресса по урокам
- Загрузка и скачивание файлов (уроки, submissions)
- Swagger UI для REST API
- Автоматические миграции БД через Liquibase

---

## Стек технологий

| Компонент | Технология |
|---|---|
| Язык | Java 21 |
| Фреймворк | Spring Boot 3.5 |
| Безопасность | Spring Security |
| ORM | Spring Data JPA / Hibernate |
| БД | PostgreSQL 16 |
| Миграции | Liquibase |
| Шаблоны | Thymeleaf |
| API docs | SpringDoc OpenAPI |
| Маппинг | MapStruct |
| Сборка | Maven Wrapper |

---

## Требования

### Для Docker (рекомендуется)

- Docker 24+
- Docker Compose v2+

### Для локальной разработки

- JDK 21
- Maven или Maven Wrapper (`./mvnw`)
- PostgreSQL 16+

---

## Быстрый старт (Docker)

Самый простой способ — запустить всё одной командой:

```bash
docker compose up --build
```

После успешного старта приложение полностью готово к работе:

- PostgreSQL поднимается с healthcheck
- Liquibase автоматически выполняет миграции
- загруженные файлы сохраняются в Docker volume

**Приложение:** http://localhost:8080  
**Swagger UI:** http://localhost:8080/swagger-ui.html

Дополнительных действий (ручной SQL, миграции, настройка БД) не требуется.

Запуск в фоне:

```bash
docker compose up --build -d
```

Просмотр логов:

```bash
docker compose logs -f app
```

---

## Локальный запуск

### 1. Подготовка PostgreSQL

Создайте базу данных:

```sql
CREATE DATABASE online_learning;
```

Или через `psql`:

```bash
psql -U postgres -c "CREATE DATABASE online_learning;"
```

### 2. Настройка подключения

По умолчанию приложение использует параметры из `src/main/resources/application.yml`:

| Параметр | Значение по умолчанию |
|---|---|
| URL | `jdbc:postgresql://localhost:5432/online_learning` |
| Пользователь | `postgres` |
| Пароль | `1234` |

При необходимости задайте переменные окружения:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/online_learning
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password
```

### 3. Запуск приложения

```bash
./mvnw spring-boot:run
```

Или собрать JAR и запустить:

```bash
./mvnw clean package -DskipTests
java -jar target/online-learning-0.0.1-SNAPSHOT.jar
```

Приложение будет доступно по адресу: http://localhost:8080

Liquibase выполнит миграции автоматически при первом запуске.

---

## Создание admin-аккаунта

При регистрации через `/register` (сайт или REST API) пользователь **всегда** получает роль `ROLE_STUDENT`.  
Admin-аккаунт в проекте создаётся вручную через базу данных.

### Способ 1: зарегистрировать → повысить до admin (рекомендуется)

**Шаг 1.** Зарегистрируйте пользователя.

Через REST API:

```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin2",
    "email": "admin2@example.com",
    "password": "password123",
    "firstName": "Admin",
    "lastName": "User"
  }'
```

Или через форму: http://localhost:8080/register

**Шаг 2.** Назначьте роль admin.

Если проект запущен в Docker:

```bash
docker compose exec postgres psql -U postgres -d online_learning -c \
  "UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN') WHERE username = 'admin';"
```

Если PostgreSQL установлен локально:

```bash
psql -U postgres -d online_learning -c \
  "UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN') WHERE username = 'admin';"
```

**Шаг 3.** Выйдите из аккаунта и войдите снова — Spring Security подхватит новую роль.

Проверка:

```bash
curl -u admin:password123 http://localhost:8080/users/me
```

В ответе должно быть `"role": "ROLE_ADMIN"`.

### Способ 2: создать admin напрямую в БД

Сгенерируйте BCrypt-хеш пароля:

```bash
python3 -c "import bcrypt; print(bcrypt.hashpw(b'password123', bcrypt.gensalt()).decode())"
```

Вставьте пользователя в PostgreSQL:

```sql
INSERT INTO users (username, email, password, first_name, last_name, enabled, role_id, created_at, updated_at)
VALUES (
  'admin',
  'admin@example.com',
  '$2b$10$...ваш_bcrypt_хеш...',
  'Admin',
  'User',
  true,
  (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'),
  NOW(),
  NOW()
);
```

В Docker:

```bash
docker compose exec postgres psql -U postgres -d online_learning
```

---

## Роли и доступ

| Роль | Описание |
|---|---|
| `ROLE_STUDENT` | Просмотр курсов, запись на курсы, отправка заданий, прогресс |
| `ROLE_ADMIN` | Полный доступ к админ-панели и REST API на запись |

### Публичные страницы (без авторизации)

- `/` — главная
- `/login` — вход
- `/register` — регистрация
- `/css/**` — статика
- `/swagger-ui/**`, `/v3/api-docs/**` — документация API

### Для авторизованных пользователей

- `/app/**` — студенческий интерфейс (ADMIN + STUDENT)
- `/files/**`, `/users/**` — файлы и профили

### Только для ADMIN

- `/app/admin/**` — админ-панель
- REST: `POST/PUT/PATCH/DELETE` для `/courses`, `/modules`, `/lessons`, `/assignments`

### Только для STUDENT

- `/enrollments/**` — запись на курсы
- `/progress/**` — прогресс по урокам

---

## Веб-интерфейс

### Студент

| URL | Описание |
|---|---|
| http://localhost:8080/ | Главная страница |
| http://localhost:8080/login | Вход |
| http://localhost:8080/register | Регистрация |
| http://localhost:8080/app/courses | Список курсов |
| http://localhost:8080/app/courses/{id} | Детали курса |
| http://localhost:8080/app/modules/{id} | Модуль |
| http://localhost:8080/app/lessons/{id} | Урок |

### Администратор

| URL | Описание |
|---|---|
| http://localhost:8080/app/admin | Панель управления |
| http://localhost:8080/app/admin/courses | Управление курсами |
| http://localhost:8080/app/admin/users | Список пользователей |

Структура контента в админке:

```
Courses → Modules → Lessons → Assignments
```

---

## REST API

Полная документация доступна в Swagger UI:

http://localhost:8080/swagger-ui.html

### Основные эндпоинты

| Метод | URL | Описание | Доступ |
|---|---|---|---|
| `POST` | `/register` | Регистрация студента | Публичный |
| `GET` | `/users/me` | Текущий пользователь | Auth |
| `GET` | `/courses` | Список курсов | Auth |
| `POST` | `/courses` | Создать курс | ADMIN |
| `GET` | `/courses/{id}` | Курс по ID | Auth |
| `POST` | `/courses/{courseId}/modules` | Создать модуль | ADMIN |
| `POST` | `/modules/{moduleId}/lessons` | Создать урок | ADMIN |
| `POST` | `/lessons/{lessonId}/assignments` | Создать задание | ADMIN |
| `POST` | `/enrollments/courses/{courseId}` | Записаться на курс | STUDENT |
| `POST` | `/submissions/assignments/{assignmentId}` | Отправить задание | STUDENT |
| `POST` | `/files/lessons/{lessonId}` | Загрузить файл к уроку | Auth |
| `GET` | `/files/{id}/content` | Скачать файл | Auth |

### Аутентификация в REST API

Поддерживаются два способа:

**HTTP Basic Auth:**

```bash
curl -u admin:password123 http://localhost:8080/courses
```

**Form login (сессия):** войдите через http://localhost:8080/login, затем используйте cookie в браузере.

---

## Переменные окружения

| Переменная | Описание | По умолчанию |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL PostgreSQL | `jdbc:postgresql://localhost:5432/online_learning` |
| `SPRING_DATASOURCE_USERNAME` | Пользователь БД | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Пароль БД | `1234` |
| `POSTGRES_DB` | Имя БД (Docker Compose) | `online_learning` |
| `POSTGRES_USER` | Пользователь PostgreSQL (Docker Compose) | `postgres` |
| `POSTGRES_PASSWORD` | Пароль PostgreSQL (Docker Compose) | `1234` |
| `SERVER_PORT` | Порт приложения | `8080` |
| `APP_UPLOAD_DIR` | Каталог загружаемых файлов | `uploads` (локально), `/app/uploads` (Docker) |
| `SPRING_THYMELEAF_CACHE` | Кэширование шаблонов | `false` (локально), `true` (Docker) |
| `JAVA_OPTS` | JVM-параметры (Docker) | `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` |

Пример `.env` для Docker Compose (опционально):

```env
POSTGRES_DB=online_learning
POSTGRES_USER=postgres
POSTGRES_PASSWORD=1234
SERVER_PORT=8080
```

---

## Docker: управление и данные

### Остановка

```bash
docker compose down
```

Данные сохраняются в volumes `postgres_data` и `uploads_data`.

### Полная очистка (удалить данные)

```bash
docker compose down -v
```

### Пересборка после изменений кода

```bash
docker compose up --build
```

### Volumes

| Volume | Назначение |
|---|---|
| `postgres_data` | Данные PostgreSQL (пользователи, курсы, миграции) |
| `uploads_data` | Загруженные пользователем файлы |

Файлы не теряются при пересоздании контейнера `app`.

### Архитектура Docker

```
┌─────────────────────────────────────────────┐
│           online-learning-network           │
│                                             │
│  ┌──────────────┐      ┌─────────────────┐  │
│  │   postgres   │◄─────│       app       │  │
│  │  :5432       │      │  :8080          │  │
│  │              │      │  (Spring Boot)  │  │
│  └──────┬───────┘      └────────┬────────┘  │
│         │                       │           │
│  postgres_data            uploads_data        │
└─────────────────────────────────────────────┘
         ▲                       ▲
         │                       │
    localhost:8080          (не exposed)
```

- **Dockerfile:** multi-stage build (JDK 21 → JRE 21), Maven Wrapper, непривилегированный пользователь `appuser`
- **Healthcheck:** PostgreSQL проверяется перед стартом приложения (`depends_on: service_healthy`)
- **Liquibase:** миграции выполняются автоматически при каждом старте приложения

---

## Тесты

Запуск всех тестов:

```bash
./mvnw test
```

Тесты используют in-memory H2 (конфигурация в `src/test/resources/application.yml`), PostgreSQL для тестов не нужен.

---

## Структура проекта

```
online_learning/
├── Dockerfile                  # Multi-stage Docker-образ
├── docker-compose.yml          # App + PostgreSQL
├── .dockerignore
├── pom.xml
├── mvnw
└── src/
    ├── main/
    │   ├── java/com/ithub/online_learning/
    │   │   ├── config/         # Security, OpenAPI, JPA
    │   │   ├── controller/     # REST API
    │   │   ├── controller/web/ # MVC (Thymeleaf)
    │   │   ├── dto/            # Request / Response
    │   │   ├── entity/         # JPA-сущности
    │   │   ├── repository/     # Spring Data
    │   │   ├── service/        # Бизнес-логика
    │   │   ├── mapper/         # MapStruct
    │   │   └── security/       # UserDetails
    │   └── resources/
    │       ├── application.yml
    │       ├── db/changelog/   # Liquibase миграции
    │       ├── templates/      # Thymeleaf шаблоны
    │       └── static/         # CSS
    └── test/
        └── ...
```

---

## Устранение неполадок

### Приложение не стартует — ошибка подключения к БД

Убедитесь, что PostgreSQL запущен и доступен:

```bash
# Docker
docker compose ps
docker compose logs postgres

# Локально
psql -U postgres -d online_learning -c "SELECT 1;"
```

### Liquibase: миграции уже выполнены

При повторном запуске Liquibase пропускает уже применённые changeset'ы — это нормальное поведение.

### Admin-панель возвращает 403

1. Проверьте роль пользователя:

```bash
curl -u your_username:your_password http://localhost:8080/users/me
```

2. Если `"role": "ROLE_STUDENT"` — выполните SQL из раздела [Создание admin-аккаунта](#создание-admin-аккаунта).
3. Выйдите и войдите снова после смены роли.

### Порт 8080 занят

Измените порт через переменную окружения:

```bash
SERVER_PORT=9090 docker compose up --build
```

### Очистить все данные и начать заново

```bash
docker compose down -v
docker compose up --build
```

После этого потребуется заново создать admin-аккаунт и зарегистрировать пользователей.
