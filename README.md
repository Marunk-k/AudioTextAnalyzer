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


## Включение реального GigaChat

1. Получите Authorization Key в личном кабинете проекта GigaChat API.
2. Не добавляйте к нему префикс `Basic`.
3. Запустите приложение с переменными окружения:

Windows PowerShell:

```powershell
$env:APP_GIGACHAT_ENABLED="true"
$env:GIGACHAT_CREDENTIALS="ваш_authorization_key"
$env:GIGACHAT_SCOPE="GIGACHAT_API_PERS"
$env:GIGACHAT_MODEL="GigaChat"
mvn spring-boot:run
```

Linux/macOS:

```bash
export APP_GIGACHAT_ENABLED=true
export GIGACHAT_CREDENTIALS="ваш_authorization_key"
export GIGACHAT_SCOPE="GIGACHAT_API_PERS"
export GIGACHAT_MODEL="GigaChat"
mvn spring-boot:run
```

4. В логах должна появиться строка:

`AI-постобработка: используется RealGigaChatService`

5. Если в логах написано `fallback-сервис`, значит real GigaChat не включился.

6. Если возникает SSL/PKIX ошибка, значит где-то всё ещё используется ручной RestClient, а не SDK с `verifySslCerts(false)`, либо `verifySslCerts=true`.
