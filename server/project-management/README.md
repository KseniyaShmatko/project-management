# Project Management — Spring Boot + PostgreSQL + MongoDB

## Описание проекта

Бэкенд для системы управления проектами с хранением основной структуры в PostgreSQL и динамического содержимого файлов и связанных сущностей в MongoDB.

---

## Требования

- **Java 17+**
- **Kotlin 1.9.x**
- **PostgreSQL** (рекомендуется >= 13)
- **MongoDB** (рекомендуется >= 5)
- **Gradle** (используется wrapper — скачивать вручную не требуется)

---

## Быстрый старт

### 1. Клонирование репозитория

```bash
git clone https://github.com/KseniyaShmatko/project-management.git
cd project-management
```

### 2. Установка зависимостей

**PostgreSQL**
1. Установите PostgreSQL (если ещё нет).
2. Создайте БД и пользователя:
```bash
psql -U postgres
```

В консоли psql введите:
```sql
CREATE DATABASE project_db;
CREATE USER project_user WITH ENCRYPTED PASSWORD 'project_pass';
GRANT ALL PRIVILEGES ON DATABASE project_db TO project_user;
```

**MongoDB**

1. Установите MongoDB
2. Запустите службу (macOS):
```bash
brew services start mongodb-community
```

3. MongoDB создаст базу автоматически при первом сохранении данных.

### 3. Настройка приложения

Все параметры уже прописаны в src/main/resources/application.properties:

```
spring.data.mongodb.uri=mongodb://localhost:27017/project_db
spring.datasource.url=jdbc:postgresql://localhost:5432/project_db
spring.datasource.username=project_user
spring.datasource.password=project_pass
```
При необходимости поменяйте параметры подключения под свою среду.

### 4. Сборка и запуск

В корне проекта выполните:
```bash
./gradlew clean build
./gradlew bootRun
```

или только запуск:
```bash
./gradlew bootRun
```

### 5. API

- По умолчанию приложение работает на порту **8080**.

- Базовые ручки:
  - /super-objects — работа с файлами (MongoDB)
  - /content-blocks — блоки контента
  - /styles — стили
  - /styles-maps — карта применения стилей
  - /projects, /users, /files — проекты, пользователи, файл (PostgreSQL)

- Описание ручек смотрите в OpenAPI/Swagger-спецификации или в папке controllers.

### 6. Тестирование

Для запуска тестов выполните команду: 
```bash
./gradlew test
```

Для измерения тестового покрытия выполните: 
```bash
./gradlew test jacocoTestReport
```
и затем откройте отчет:
```bash
open build/jacocoHtml/index.html
```