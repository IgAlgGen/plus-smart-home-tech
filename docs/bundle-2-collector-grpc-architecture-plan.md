# План переноса архитектурного подхода из `plus-smart-home-tech.bundle` (до `2-collector-grpc`) в `work`

## Файлы, которые затрагивает план
- **Контракт (proto/avro) не трогаем**: только адаптация модульной структуры и пакетов приложений.
- Основная зона изменений: `telemetry` (collector + новый hubrouter-модуль), root `pom.xml` и `telemetry/pom.xml`.

## 1) Сравнение архитектуры `work` и `bundle` (до `2-collector-grpc`)

### 1.1 Maven multi-module

**Сейчас в `work`:**
- `smart-home-tech`
  - `telemetry`
    - `serialization`
      - `avro-schemas`
      - `proto-schemas`
    - `collector`
  - `infra`
  - `commerce`
- Отдельно лежит `hub-router/` как внешний jar + скрипты (вне Maven-модулей).

**В `bundle` (целевой подход до `2-collector-grpc`):**
- `smart-home-tech`
  - `telemetry`
    - `serialization`
      - `avro-schemas`
      - `proto-schemas`
    - `collector`
    - `hubrouter`  ← отдельный Java/Spring Boot модуль внутри Maven reactor
  - `infra`
  - `commerce`

**Ключевая разница:** в bundle клиент-генератор событий (`hubrouter`) встроен в multi-module как отдельный сервис, а не вынесен наружу как бинарник.

### 1.2 Collector: интерфейсы и сервисы

**`work`:**
- Контроллер HTTP (`/events/...`) принимает локальные DTO (`dto.sensor.*`, `dto.hub.*`).
- `Base*EventHandler` сериализуют Avro в `byte[]` и шлют в Kafka как `Producer<String, byte[]>`.
- Есть mapper-слой (`EventAvroMapper`) и отдельный `AvroBinarySerializer`.

**`bundle`:**
- gRPC-контроллер (`CollectorControllerGrpc`) принимает `SensorEventProto`/`HubEventProto`.
- `Base*EventHandler` работают с protobuf-событиями и собирают `SensorEventAvro/HubEventAvro`.
- Kafka-пайплайн ориентирован на `SpecificRecordBase` + Avro-serializer (типизированный путь без промежуточного DTO-контракта).

**Архитектурный смысл bundle-подхода:**
- транспортный контракт = protobuf;
- обработчики диспетчеризуются по `PayloadCase` gRPC-сообщений;
- сервис генерации/отправки событий (hubrouter) отделён в самостоятельный модуль.

### 1.3 Зависимости

Для целевого подхода из bundle критичны:
- В root `dependencyManagement`:
  - `grpc-client-spring-boot-starter`;
  - (опционально) compile-annotaions (`org.jetbrains:annotations`) — не обязательная часть фичи.
- В `telemetry/collector`:
  - `proto-schemas`, `grpc-server-spring-boot-starter`, `grpc-stub`, `grpc-protobuf`.
- В `telemetry/hubrouter`:
  - `proto-schemas`, `grpc-client-spring-boot-starter`, `grpc-stub`, `grpc-protobuf`.

## 2) Ограничения миграции
- **Не менять контракт** (`telemetry/serialization/**` с proto/avro схемами) — только переиспользовать текущий.
- Минимальные изменения: не делать сопутствующий рефакторинг вне перехода на bundle-подход.

## 3) План коммитов (предлагаемая последовательность)

### Commit 1 — модульная рамка telemetry/hubrouter
**Цель:** перенести в Maven-архитектуру идею отдельного hubrouter-сервиса.

Изменения:
1. `telemetry/pom.xml`: добавить модуль `hubrouter`.
2. Создать `telemetry/hubrouter/pom.xml` с зависимостями gRPC client + `proto-schemas`.
3. Добавить каркас приложения:
   - `telemetry/hubrouter/src/main/java/.../HubRouterApp.java`
   - `telemetry/hubrouter/src/main/resources/application.yaml`

### Commit 2 — перенос runtime-логики hub-router в модуль
**Цель:** встроить генерацию/отправку событий в кодовую базу Maven.

Изменения по пакетам:
- `ru.yandex.practicum.telemetry.hubrouter.config`
  - `SensorConfig` (конфиг источников данных/диапазонов).
- `ru.yandex.practicum.telemetry.hubrouter.producer`
  - `EventDataProducer` (генерация protobuf-событий + отправка в collector gRPC).
- (при необходимости) адаптер для существующих скриптов запуска, без смены контракта API.

### Commit 3 — collector transport alignment (gRPC вход)
**Цель:** привести слой входа collector к bundle-подходу.

Изменения по пакетам:
- `ru.yandex.practicum.telemetry.collector.controller`
  - добавить gRPC endpoint (на базе сгенерированного `CollectorControllerGrpc`).
  - HTTP-контроллер оставить только если это требуется текущим окружением; иначе удалить в отдельном коммите.
- `ru.yandex.practicum.telemetry.collector.service.sensor`
  - интерфейс/реализации на `SensorEventProto.PayloadCase`.
- `ru.yandex.practicum.telemetry.collector.service.hub`
  - интерфейс/реализации на `HubEventProto.PayloadCase`.

### Commit 4 — collector processing internals (без изменения контракта)
**Цель:** синхронизировать внутренние зависимости/потоки данных с bundle.

Изменения:
1. `telemetry/collector/pom.xml` — выровнять зависимости под gRPC + proto, убрать лишнее для устаревшего transport-path.
2. `collector` пакеты:
   - минимизировать/удалить локальные DTO (`dto.*`) если они больше не используются;
   - свести маппинг к protobuf → avro в handler-слое.
3. Конфигурация Kafka:
   - оставить совместимый способ публикации в существующие топики (`telemetry.sensors.v1`, `telemetry.hubs.v1`),
   - не менять имена топиков и формат контрактов.

### Commit 5 — cleanup и проверка сборки reactor
**Цель:** зафиксировать консистентную multi-module архитектуру.

Изменения:
1. Убрать дубли и мёртвый код, оставшийся после переключения transport-пути.
2. Проверить `mvn -q -DskipTests package` на корне.
3. Проверить запуск:
   - collector (gRPC server),
   - hubrouter (gRPC client),
   - smoke через существующие `hub-router` тестовые сценарии/их эквивалент.

## 4) Изменения по пакетам (целевой срез)

### Оставить
- `ru.yandex.practicum.telemetry.collector.service.sensor`
- `ru.yandex.practicum.telemetry.collector.service.hub`
- `ru.yandex.practicum.telemetry.collector.config`

### Перевести на protobuf-first
- `ru.yandex.practicum.telemetry.collector.controller`
- `ru.yandex.practicum.telemetry.collector.service.sensor.*Handler`
- `ru.yandex.practicum.telemetry.collector.service.hub.*Handler`

### Добавить
- `ru.yandex.practicum.telemetry.hubrouter.*` (новый Maven-модуль)

### Деприоритизировать/убрать после миграции
- `ru.yandex.practicum.telemetry.collector.dto.*` (если больше не нужен HTTP-вход)
- отдельный внешний `hub-router` binary-flow как основной путь (можно оставить как fallback-инструмент).

## 5) Критерии готовности (Definition of Done)
1. Внутри Maven reactor есть модуль `telemetry/hubrouter`.
2. Collector принимает события по gRPC (`CollectSensorEvent`, `CollectHubEvent`) с текущим proto-контрактом.
3. Обработчики collector выбираются по protobuf payload type, публикуют в те же Kafka-топики.
4. Контрактные файлы `telemetry/serialization/**` не изменены.
