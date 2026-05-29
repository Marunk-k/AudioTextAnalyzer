# AudioText Analyzer

Java/Spring Boot приложение для загрузки аудио, распознавания речи (FFmpeg + Vosk), предобработки текста, AI-постобработки (GigaChat), анализа и экспорта (TXT/DOCX/PDF/JSON).

## Технологии
- Java 17
- Spring Boot
- Thymeleaf
- PostgreSQL
- Spring JDBC
- FFmpeg
- Vosk
- GigaChat
- Apache POI
- PDFBox
- Jackson
- Maven

## База данных PostgreSQL

Проект использует PostgreSQL. Таблицы создаются из `src/main/resources/schema.sql` двумя способами:

1. Docker монтирует `schema.sql` в `/docker-entrypoint-initdb.d/001-schema.sql` и создаёт таблицы при первом создании volume.
2. Spring Boot дополнительно запускает `schema.sql` на старте приложения (`spring.sql.init.mode=always`).

### Быстрый запуск с чистой БД

Если контейнер уже запускался раньше и поднялся без таблиц, удалите старый volume и создайте БД заново:

```bash
docker compose down -v
docker compose up -d postgres
mvn spring-boot:run
```

Приложение: http://localhost:8080

### Проверить, что таблицы создались

```bash
docker compose exec postgres psql -U audiotext_user -d audiotext_analyzer -c "\dt"
```

Должны быть таблицы:

- `users`
- `projects`
- `audio_files`
- `project_texts`
- `analysis_results`
- `dictionaries`
- `dictionary_entries`
- `export_files`

### Создать таблицы вручную без пересоздания volume

Если volume удалять нельзя, выполните схему вручную:

```bash
docker compose up -d postgres
docker compose exec -T postgres psql -U audiotext_user -d audiotext_analyzer < src/main/resources/schema.sql
```

## Параметры PostgreSQL в Docker

В проекте есть `docker-compose.yml` с PostgreSQL 16.

- DB: `audiotext_analyzer`
- User: `audiotext_user`
- Password: `audiotext_password`
- Port: `5432:5432`
- Volume: `audiotext_postgres_data`

`src/main/resources/application.yml` уже настроен на это подключение:

- `jdbc:postgresql://localhost:5432/audiotext_analyzer`
- `username: audiotext_user`
- `password: audiotext_password`

## Полный старт/остановка

```bash
docker compose up -d
mvn spring-boot:run
```

Остановить без удаления данных:

```bash
docker compose down
```

Остановить и удалить данные БД:

```bash
docker compose down -v
```

## Внешние зависимости

### FFmpeg

Для обработки реальных аудиофайлов установите FFmpeg и убедитесь, что команда `ffmpeg` доступна в PATH, либо укажите путь в `app.audio.ffmpeg-path`.

### Vosk

Путь к модели задаётся в `app.vosk.model-path`. По умолчанию используется `models/vosk-model-small-ru`.

### GigaChat

Для реальной AI-постобработки задайте переменные окружения:

- `GIGACHAT_CREDENTIALS`
- `GIGACHAT_SCOPE` (по умолчанию `GIGACHAT_API_PERS`)
- `GIGACHAT_MODEL` (по умолчанию `GigaChat`)
