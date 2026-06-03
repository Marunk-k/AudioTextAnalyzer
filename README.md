# AudioText Analyzer

Java Spring Boot web-приложение для автоматического перевода аудиофайлов в текстовый формат с форматированием, анализом и экспортом результата. Пользователь работает через браузер: создаёт проект, загружает аудио, запускает обработку FFmpeg + Vosk, редактирует версии текста, использует пользовательские словари, при наличии настроек GigaChat запускает AI-постобработку и формирование краткого содержания, затем экспортирует результат в TXT, DOCX, PDF или JSON.

## Технологии

- Java 17
- Spring Boot + Thymeleaf + Spring Security
- PostgreSQL + Spring JDBC
- FFmpeg / ffprobe
- Vosk
- GigaChat (опционально, только при заданных credentials)
- Apache POI (DOCX)
- PDFBox + Unicode-шрифт для PDF с кириллицей (classpath `fonts/DejaVuSans.ttf` или системный DejaVuSans)
- Jackson
- Maven

## Хранение данных

Проект использует PostgreSQL. Загруженные аудиофайлы сохраняются в `audio_files.file_data`, экспортированные документы — в `export_files.file_data`. Временные файлы могут создаваться на сервере только на время обработки и экспорта; основной pipeline обработки берёт исходное аудио из БД.

Таблицы создаются из `src/main/resources/schema.sql`:

- `users`
- `projects`
- `audio_files`
- `project_texts`
- `analysis_results`
- `dictionaries`
- `dictionary_entries`
- `export_files`

## Быстрый запуск

### 1. Требования

Установите:

- JDK 17
- Maven
- Docker и Docker Compose
- FFmpeg: команда `ffmpeg` должна быть доступна в `PATH`, либо укажите путь в `app.audio.ffmpeg-path`
- модель Vosk для русского языка; по умолчанию ожидается каталог `models/vosk-model-small-ru`
- Unicode-шрифт для PDF с кириллицей: установите системный DejaVuSans или положите `DejaVuSans.ttf` в `src/main/resources/fonts` перед сборкой

### 2. Запуск PostgreSQL через Docker

```bash
docker compose up -d
```

По умолчанию используется PostgreSQL 16:

- DB: `audiotext_analyzer`
- User: `audiotext_user`
- Password: `audiotext_password`
- Port: `5432:5432`

Если нужен полностью чистый старт БД:

```bash
docker compose down -v
docker compose up -d
```

### 3. Настройка GigaChat (опционально)

Для реальной AI-постобработки и AI-summary задайте переменные окружения:

```bash
export GIGACHAT_CREDENTIALS="<base64-credentials>"
export GIGACHAT_SCOPE="GIGACHAT_API_PERS"
export GIGACHAT_MODEL="GigaChat"
```

Если `app.gigachat.enabled=false` или `GIGACHAT_CREDENTIALS` пустой, GigaChat считается недоступным: приложение продолжает работать с `rawText`/`processedText`/`manualText`, а при нажатии AI-кнопки показывает понятное сообщение.

### 4. Запуск приложения

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: http://localhost:8080

## Vosk и mock-режим

В финальной конфигурации mock-распознавание выключено по умолчанию:

```yaml
app.processing.use-mock-transcription-if-vosk-unavailable: false
```

Если модель Vosk отсутствует, обработка проекта завершается статусом `ERROR` с сообщением о проблеме. Mock-режим можно включать только явно для разработки или тестов.

## Проверка таблиц

```bash
docker compose exec postgres psql -U audiotext_user -d audiotext_analyzer -c "\dt"
```

Если volume уже существовал и схему нужно применить вручную:

```bash
docker compose exec -T postgres psql -U audiotext_user -d audiotext_analyzer < src/main/resources/schema.sql
```

## Основной пользовательский сценарий

1. Зарегистрироваться и войти.
2. Создать проект с уникальным названием.
3. Загрузить WAV/MP3/M4A/OGG/FLAC.
4. Запустить обработку: FFmpeg приводит аудио к WAV mono 16000 Hz 16-bit PCM, Vosk распознаёт речь.
5. Проверить `rawText`, `processedText`, при необходимости запустить AI-постобработку или отредактировать финальный текст вручную.
6. Использовать словари слов-паразитов и замен в настройках.
7. Проверить анализ: статистика считается по лучшей версии текста `manualText → aiText → processedText → rawText`, слова-паразиты — по `rawText`.
8. Скачать экспорт TXT, DOCX, PDF или JSON; повторное скачивание отдаёт сохранённый файл из БД.
