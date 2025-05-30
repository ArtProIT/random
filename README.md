# LeetCode Random Number Generator

Приложение для генерации случайных чисел с возможностью автоматического исключения уже решённых задач LeetCode и фильтрацией по уровням сложности.

## 📋 Содержание

- [Описание](#описание)
- [Новые возможности](#новые-возможности)
- [Требования](#требования)
- [Установка](#установка)
- [Архитектура](#архитектура)
- [Документация классов](#документация-классов)
- [Использование](#использование)
- [Структура проекта](#структура-проекта)
- [Зависимости](#зависимости)

## 🎯 Описание

Это Java-приложение с современным графическим интерфейсом, которое позволяет:
- Генерировать случайные числа в заданном диапазоне
- Автоматически исключать номера уже решённых задач LeetCode
- **Фильтровать задачи по уровням сложности (Easy/Medium/Hard)**
- Получать данные о решенных задачах через веб-скрапинг профиля пользователя
- **Отображать подробную статистику доступных задач**
- **Показывать интерактивный прогресс выполнения операций**
- **Выбирать режим работы браузера (headless/видимый)**

## 🆕 Новые возможности

### ✨ Фильтрация по сложности
- Загрузка полной информации о всех задачах LeetCode через API
- Выбор уровней сложности: Easy, Medium, Hard
- Генерация случайных чисел только из задач выбранной сложности
- Статистика по количеству доступных задач каждого уровня

### 🖥️ Управление браузером
- **Headless режим**: быстрая работа в фоне (по умолчанию)
- **Видимый режим**: возможность наблюдать процесс скрапинга
- Переключение режима через чекбокс в интерфейсе

### 📊 Улучшенная аналитика
- Подробная статистика доступных задач
- Информация о выбранной задаче (номер, название, сложность)
- Интеллектуальное кэширование данных API на 24 часа

### 🏗️ Современная архитектура
- **Page Object Model** для веб-скрапинга
- **Retry Manager** для обработки ошибок
- **Cache Service** для управления кэшем
- **Browser Manager** для управления жизненным циклом браузера

## ⚙️ Требования

- **Java**: 11 или выше
- **Gradle**: 8.0+
- **Браузер**: Chromium/Chrome (для Playwright)

### Зависимости Gradle:
```gradle
dependencies {
    implementation 'com.microsoft.playwright:playwright:1.40.0'
    implementation 'org.json:json:20240303'
    implementation 'org.slf4j:slf4j-api:2.0.9'
    implementation 'ch.qos.logback:logback-classic:1.4.14'
    implementation 'org.projectlombok:lombok:1.18.30'
    annotationProcessor 'org.projectlombok:lombok:1.18.30'
}
```

## 🚀 Установка

1. Клонируйте репозиторий
2. Установите зависимости Gradle:
   ```bash
   ./gradlew build
   ```
3. Установите браузеры для Playwright:
   ```bash
   ./gradlew installPlaywright
   ```
4. Запустите приложение:
   ```bash
   ./gradlew run
   ```

## 🏗️ Архитектура

Приложение построено на принципах чистой архитектуры с четким разделением ответственности и современными паттернами проектирования:

### Слои архитектуры:
- **UI Layer** (`ui/`) - Пользовательский интерфейс
- **Service Layer** (`service/`) - Бизнес-логика
- **Scraper Layer** (`scraper/`) - Веб-скрапинг с подслоями:
   - `browser/` - Управление браузером
   - `pages/` - Page Object Model
   - `services/` - Сервисы скрапинга
   - `parsers/` - Парсинг данных
- **Model Layer** (`model/`) - Модели данных
- **Config Layer** (`config/`) - Конфигурация
- **Exception Layer** (`exception/`) - Обработка исключений

### Паттерны проектирования:
- **Page Object Model** - для организации веб-скрапинга
- **Service Layer** - для инкапсуляции бизнес-логики
- **Retry Pattern** - для обработки временных сбоев
- **Cache Pattern** - для оптимизации производительности
- **Builder Pattern** - в конфигурации Playwright

## 📚 Документация классов

### 🎯 RandomNumberApp
**Пакет**: `com.example.random`
**Назначение**: Главный класс приложения, точка входа

```java
public class RandomNumberApp
```

#### Методы:
- `main(String[] args)` - Точка входа в приложение
- `createAndShowGUI()` - Создает сервисы и инициализирует UI

---

### 🖥️ MainWindow
**Пакет**: `com.example.random.ui`
**Назначение**: Главное окно приложения с улучшенным UI и фильтрацией по сложности

```java
public class MainWindow
```

#### Ключевые поля:
- `JCheckBox easyCheckBox, mediumCheckBox, hardCheckBox` - Фильтры сложности
- `JCheckBox headlessCheckBox` - **Переключатель режима браузера**
- `JLabel statisticsLabel, difficultyHintLabel` - Статистика и подсказки
- `Map<String, ProblemInfo> allProblemsInfo` - Кэш информации о задачах
- `boolean problemsLoaded` - Флаг загрузки данных

#### Ключевые методы:
- `createBrowserSettingsPanel()` - **Создание панели настроек браузера**
- `createDifficultyPanel()` - Создание панели фильтрации по сложности
- `updateUIState()` - Интеллектуальное управление состоянием UI
- `updateStatistics()` - Обновление статистики в реальном времени
- `onLoadProblemsButtonClick()` - Загрузка всех задач через API
- `getSelectedDifficulties()` - Получение выбранных уровней сложности
- `showProblemInfo(Integer)` - Отображение информации о выбранной задаче

---

### 🎲 RandomGeneratorService
**Пакет**: `com.example.random.service`
**Назначение**: Расширенный сервис генерации с поддержкой фильтрации по сложности

```java
public class RandomGeneratorService
```

#### Ключевые методы:
- `generateRandomNumber(int, int, Set<Integer>)` - Простая генерация
- `generateRandomProblemNumber(...)` - **Генерация с фильтрацией по сложности**
- `getProblemStatistics(...)` - **Получение статистики задач**
- `parseExcludeNumbers(String)` - Парсинг исключений
- `formatExcludeNumbers(Set<Integer>)` - Форматирование исключений

#### Внутренний класс ProblemStatistics:
```java
@Value
public static class ProblemStatistics {
    long total, easy, medium, hard;
}
```

---

### 🌐 LeetCodeService
**Пакет**: `com.example.random.service`
**Назначение**: Главный сервис для работы с LeetCode

```java
@Slf4j
public class LeetCodeService
```

#### Методы:
- `getSolvedProblems(String, boolean, Consumer<String>)` - **С поддержкой headless**
- `getAllProblemsInfo(boolean, Consumer<String>)` - **С поддержкой headless**
- `isValidUsername(String)` - Валидация username
- `getCacheInfo()` - Информация о кэше
- `clearCache()` - Очистка кэша

---

### 🕷️ LeetCodeScrapingService
**Пакет**: `com.example.random.scraper.services`
**Назначение**: Координирует работу всех компонентов скрапинга

```java
@Slf4j
public class LeetCodeScrapingService
```

#### Ключевые методы:
- `fetchSolvedProblems(String, boolean)` - **Получение решенных задач с headless**
- `getAllProblemsInfo(boolean)` - **Получение всех задач с headless**
- `getSolvedProblemsFromProfile(...)` - Скрапинг профиля с retry
- `getAllProblemsInfo(BrowserManager)` - Получение через API
- `matchSolvedProblems(...)` - Сопоставление названий с номерами

---

### 🌐 BrowserManager
**Пакет**: `com.example.random.scraper.browser`
**Назначение**: Управляет жизненным циклом браузера

```java
@Slf4j
public class BrowserManager implements AutoCloseable
```

#### Конструкторы:
- `BrowserManager(boolean headless)` - **С выбором режима**
- `BrowserManager()` - По умолчанию headless=true

#### Методы:
- `createPage()` - Создание страницы с настройками
- `isHeadless()` - Получение текущего режима
- `close()` - Освобождение ресурсов

---

### 🔄 RetryManager
**Пакет**: `com.example.random.scraper.browser`
**Назначение**: Управляет логикой повторных попыток

```java
@Slf4j
public class RetryManager
```

#### Методы:
- `executeWithRetry(Supplier<T>, String, ResultValidator<T>)` - Выполнение с валидацией
- `executeWithRetry(Supplier<T>, String)` - Упрощенная версия

#### Интерфейс валидации:
```java
@FunctionalInterface
public interface ResultValidator<T> {
    boolean isValid(T result);
}
```

---

### 💾 CacheService
**Пакет**: `com.example.random.scraper.services`
**Назначение**: Управление кэшированием данных

```java
@Slf4j
public class CacheService
```

#### Методы:
- `cacheProblems(Map<String, ProblemInfo>)` - Сохранение в кэш
- `getCachedProblems()` - Получение из кэша
- `clearCache()` - Очистка кэша
- `getStats()` - Статистика кэша

#### Внутренние классы:
```java
@AllArgsConstructor
@Getter
private static class CacheEntry<T> { ... }

@AllArgsConstructor
@Getter
public static class CacheStats { ... }
```

---

### 📄 Page Object Classes

#### BasePage
**Пакет**: `com.example.random.scraper.pages`
**Назначение**: Базовый класс для всех Page Objects

```java
@Slf4j
public abstract class BasePage
```

#### LeetCodeApiPage
**Пакет**: `com.example.random.scraper.pages`
**Назначение**: Page Object для API страницы LeetCode

#### LeetCodeProfilePage
**Пакет**: `com.example.random.scraper.pages`
**Назначение**: Page Object для страницы профиля пользователя

---

### 🔍 ApiJsonParser
**Пакет**: `com.example.random.scraper.parsers`
**Назначение**: Парсер JSON данных из LeetCode API

```java
@Slf4j
public class ApiJsonParser
```

#### Методы:
- `parseProblemsFromJson(String)` - Парсинг задач из JSON
- `isValidJson(String)` - Валидация JSON
- `getStatistics(String)` - Статистика по уровням сложности

---

### 🔍 ProblemMatcher
**Пакет**: `com.example.random.scraper`
**Назначение**: Сопоставление названий задач с номерами

```java
@Slf4j
public class ProblemMatcher
```

#### Методы:
- `findProblemInfo(String, Map<String, ProblemInfo>)` - Поиск задачи
- `matchProblemsToNumbers(Set<String>, Map<String, ProblemInfo>)` - Массовое сопоставление
- `isPartialMatch(String, String)` - Проверка частичного совпадения
- `calculateLevenshteinDistance(String, String)` - Расчет расстояния Левенштейна

---

### 📊 Model Classes

#### ProblemInfo
**Пакет**: `com.example.random.model`

```java
@Value
@EqualsAndHashCode(of = "number")
public class ProblemInfo {
    int number;
    String title;
    ProblemDifficulty difficulty;
}
```

#### ProblemDifficulty
**Пакет**: `com.example.random.model`

```java
public enum ProblemDifficulty {
    EASY(1, "Easy"),
    MEDIUM(2, "Medium"), 
    HARD(3, "Hard");
}
```

---

### ⚙️ ScrapingConfig
**Пакет**: `com.example.random.config`
**Назначение**: Централизованная конфигурация скрапинга

```java
public class ScrapingConfig
```

#### Константы времени ожидания:
- `DEFAULT_WAIT_TIME = 3000` - Стандартное ожидание
- `JSON_WAIT_TIME = 2000` - Ожидание JSON API
- `SOLVED_PROBLEMS_WAIT = 5000` - Ожидание решенных задач

#### Retry конфигурация:
- `MAX_RETRY_ATTEMPTS = 3` - Максимум попыток
- `RETRY_DELAY = 1000` - Задержка между попытками

#### Селекторы и URL:
- `LEETCODE_API_URL` - URL для API всех задач
- `LEETCODE_PROFILE_URL_TEMPLATE` - Шаблон URL профиля
- `RECENT_AC_SELECTORS[]` - Селекторы для кнопки Recent AC
- `SOLVED_PROBLEM_SELECTORS[]` - Селекторы для решенных задач
- `USER_AGENTS[]` - Массив User-Agent строк

---

### ⚠️ LeetCodeExceptions
**Пакет**: `com.example.random.exception`
**Назначение**: Иерархия кастомных исключений

```java
public class LeetCodeExceptions
```

#### Исключения:
- `LeetCodeScrapingException` - Базовое исключение скрапинга
- `ApiDataException extends LeetCodeScrapingException` - Ошибки API
- `ValidationException` - Ошибки валидации входных данных

## 🎮 Использование

### Запуск приложения:
```bash
./gradlew run
```

### Пошаговый процесс работы:

#### 1. Настройка режима браузера
- **Headless режим** ✅ (по умолчанию): быстрая работа в фоне
- **Видимый режим** ❌: можно наблюдать процесс скрапинга
- Переключение через чекбокс "Скрыть браузер (headless режим)"

#### 2. Загрузка данных о задачах (рекомендуется)
- Нажмите **"Загрузить все задачи"**
- Дождитесь завершения загрузки (~30 секунд)
- Активируются чекбоксы фильтрации по сложности

#### 3. Настройка фильтров
- Выберите уровни сложности: Easy ✅, Medium ✅, Hard ✅
- Посмотрите обновленную статистику доступных задач
- При необходимости измените диапазон чисел

#### 4. Загрузка решенных задач (опционально)
- Введите username LeetCode (по умолчанию: "ArtProIT")
- Нажмите **"Получить решенные задачи с LeetCode"**
- Решенные задачи автоматически добавятся в исключения

#### 5. Генерация случайного числа
- Убедитесь, что выбран хотя бы один уровень сложности
- Нажмите **"Сгенерировать"**
- Получите результат с информацией о задаче

### Интерфейс приложения:

#### Основные поля:
- **Профиль LeetCode** - username пользователя
- **Минимальное/Максимальное значение** - диапазон задач
- **Исключить** - номера через запятую (заполняется автоматически)

#### Настройки браузера:
- **Скрыть браузер (headless режим)** ✅ - быстрая работа в фоне
- **Без галочки** ❌ - видимый браузер для наблюдения процесса

#### Фильтрация по сложности:
- **Easy** 🟢 - Простые задачи
- **Medium** 🟠 - Средние задачи
- **Hard** 🔴 - Сложные задачи

#### Кнопки действий:
- **Загрузить все задачи** - получение данных через API
- **Получить решенные задачи** - скрапинг профиля пользователя
- **Сгенерировать** - создание случайного числа

#### Панель прогресса:
- Детальный лог всех операций с временными метками
- Информация о режиме браузера
- Автоматическая прокрутка к последним сообщениям

## 📁 Структура проекта

```
├── build.gradle                      # Конфигурация сборки Gradle
├── gradle/
│   └── wrapper/                      # Gradle Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── RandomNumberApp.java              # Главный класс
│   │   │   └── random/
│   │   │       ├── ui/
│   │   │       │   └── MainWindow.java           # UI с headless настройками
│   │   │       ├── service/
│   │   │       │   ├── LeetCodeService.java      # Главный сервис с headless
│   │   │       │   └── RandomGeneratorService.java # Сервис с фильтрацией
│   │   │       ├── scraper/
│   │   │       │   ├── browser/                  # Управление браузером
│   │   │       │   │   ├── BrowserManager.java  # Менеджер браузера
│   │   │       │   │   └── RetryManager.java    # Менеджер повторов
│   │   │       │   ├── pages/                   # Page Object Model
│   │   │       │   │   ├── BasePage.java        # Базовый Page Object
│   │   │       │   │   ├── LeetCodeApiPage.java # API страница
│   │   │       │   │   └── LeetCodeProfilePage.java # Страница профиля
│   │   │       │   ├── services/                # Сервисы скрапинга
│   │   │       │   │   ├── CacheService.java    # Сервис кэширования
│   │   │       │   │   └── LeetCodeScrapingService.java # Главный сервис скрапинга
│   │   │       │   ├── parsers/                 # Парсеры данных
│   │   │       │   │   └── ApiJsonParser.java   # Парсер JSON API
│   │   │       │   └── ProblemMatcher.java      # Сопоставление задач
│   │   │       ├── model/                        # Модели данных
│   │   │       │   ├── ProblemInfo.java          # Информация о задаче
│   │   │       │   └── ProblemDifficulty.java    # Enum уровней сложности
│   │   │       ├── config/                       # Конфигурация
│   │   │       │   └── ScrapingConfig.java       # Настройки скрапинга
│   │   │       └── exception/                    # Исключения
│   │   │           └── LeetCodeExceptions.java   # Кастомные исключения
│   │   └── resources/                # Ресурсы приложения
│   └── test/
│       └── java/                     # Unit тесты (планируются)
└── README.md                         # Документация
```

## 🔧 Конфигурация

### ScrapingConfig параметры:
```java
// Таймауты
DEFAULT_WAIT_TIME = 3000ms
JSON_WAIT_TIME = 2000ms
SOLVED_PROBLEMS_WAIT = 5000ms

// Retry логика
MAX_RETRY_ATTEMPTS = 3
RETRY_DELAY = 1000ms (с экспоненциальной задержкой)

// Кэширование
CACHE_EXPIRY_TIME = 24 часа (CacheService)
```

### Playwright настройки:
- **Браузер**: Chromium
- **Режим по умолчанию**: Headless включен
- **Видимый режим**: SlowMo 300ms между действиями
- **User-Agent**: Ротация между тремя различными агентами

### Логирование:
- SLF4J + Logback для структурированного логирования
- Lombok @Slf4j аннотации для всех классов
- Подробные логи процесса скрапинга и API вызовов
- Отдельные логи для UI прогресса

## 🚨 Ограничения

- Зависит от текущей структуры сайта и API LeetCode
- Требует стабильного интернет-соединения
- Первая загрузка всех задач может занять до 30 секунд
- Ограничения антибот-систем LeetCode при частых запросах
- Кэш данных обновляется только раз в 24 часа

## 🐛 Известные проблемы

1. **Медленная загрузка**: Первый запуск API может быть медленным
2. **Селекторы UI**: Могут устареть при обновлении дизайна LeetCode
3. **Блокировка браузера**: Частые запросы могут вызвать капчу
4. **Headless проблемы**: Некоторые элементы могут не загружаться в headless режиме

## 🎯 Планы развития

- [ ] Unit тесты для всех компонентов
---

**Последнее обновление**: 2025  
**Автор**: Артём Проходцев