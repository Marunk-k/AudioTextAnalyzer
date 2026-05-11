# AudioText Analyzer

Веб-MVP для дипломной работы: загрузка аудио, транскрибация (mock/Vosk), постобработка, анализ и экспорт.

## Что уже реализовано
- Загрузка аудиофайла и создание проекта.
- Сохранение проектов в SQLite (`data/db/audiotext.db`).
- Обработка проекта (mock-транскрибация -> постобработка -> анализ).
- Просмотр raw/processed текста.
- Редактирование processed текста и повторная постобработка.
- Экспорт в TXT и JSON.
- Глобальная обработка ошибок.

## Запуск
```bash
mvn spring-boot:run
```
Открыть: http://localhost:8080

## Хранилища
- uploads: `data/uploads`
- converted: `data/converted`
- exports: `data/exports`
- db: `data/db/audiotext.db`

## Mock-режим
Если модель Vosk недоступна, можно демонстрировать поток через `MockTranscriptionService` и файл `src/main/resources/samples/sample_transcription.txt`.


## Новое в текущем шаге
- Endpoint статуса `GET /projects/{id}/status` + JS polling на странице проекта.
- Кнопка `Улучшить через AI` с `MockGigaChatService` и сохранением `aiText`.

- Экспорт расширен: DOCX и PDF (PDF в текущем MVP использует базовый ASCII-safe вывод).
