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

## Запуск PostgreSQL в Docker
В проекте есть `docker-compose.yml` с PostgreSQL 16.

Параметры контейнера:
- DB: `audiotext_analyzer`
- User: `audiotext_user`
- Password: `audiotext_password`
- Port: `5432:5432`
- Volume: `audiotext_postgres_data`

## Настройка подключения
`src/main/resources/application.yml` уже настроен на локальный PostgreSQL в Docker:
- `jdbc:postgresql://localhost:5432/audiotext_analyzer`
- `username: audiotext_user`
- `password: audiotext_password`

## Быстрый старт
```bash
docker compose up -d
mvn spring-boot:run
docker compose down
```

Приложение: http://localhost:8080

## Переменные GigaChat
Для реальной AI-постобработки задайте:
- `GIGACHAT_CREDENTIALS`
- `GIGACHAT_SCOPE` (по умолчанию `GIGACHAT_API_PERS`)
- `GIGACHAT_MODEL` (по умолчанию `GigaChat`)
