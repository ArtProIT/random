# LeetCode Random Number Generator

Приложение для генерации случайных чисел с возможностью автоматического исключения уже решённых задач LeetCode.

## 📋 Содержание

- [Описание](#описание)
- [Требования](#требования)
- [Установка](#установка)
- [Архитектура](#архитектура)
- [Документация классов](#документация-классов)
- [Использование](#использование)
- [Структура проекта](#структура-проекта)
- [Зависимости](#зависимости)

## 🎯 Описание

Это Java-приложение с графическим интерфейсом, которое позволяет:
- Генерировать случайные числа в заданном диапазоне
- Автоматически исключать номера уже решённых задач LeetCode
- Получать данные о решенных задачах через веб-скрапинг профиля пользователя

## ⚙️ Требования

- **Java**: 11 или выше
- **Gradle**: 8.0+
- **Браузер**: Chromium/Chrome (для Playwright)

### Зависимости Gradle:
```gradle
dependencies {
    implementation 'com.microsoft.playwright:playwright:1.40.0'
    implementation 'org.json:json:20231013'
}
```

### Полный build.gradle:
```gradle
plugins {
    id 'java'
    id 'application'
}

group = 'com.example'
version = '1.0.0'
sourceCompatibility = '11'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.microsoft.playwright:playwright:1.40.0'
    implementation 'org.json:json:20231013'
    
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:4.11.0'
}

application {
    mainClass = 'com.example.random.RandomNumberApp'
}

tasks.register('installPlaywright', JavaExec) {
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'com.microsoft.playwright.CLI'
    args = ['install']
}

jar {
    manifest {
        attributes 'Main-Class': 'com.example.random.RandomNumberApp'
    }
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
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

#### Ответственность:
- ✅ Инициализация приложения
- ✅ Создание и связывание сервисов
- ✅ Запуск пользовательского интерфейса

---

### 🖥️ MainWindow
**Пакет**: `com.example.random.ui`
**Назначение**: Главное окно приложения с пользовательским интерфейсом и интерактивным прогрессом

```java
public class MainWindow
```

#### Поля:
- `RandomGeneratorService randomService` - Сервис генерации чисел
- `LeetCodeService leetCodeService` - Сервис работы с LeetCode
- `JFrame frame` - Главное окно
- `JTextField profileField, minField, maxField, excludeField` - Поля ввода
- `JLabel resultLabel` - Поле результата
- `JButton fetchButton, generateButton` - Кнопки действий
- `JTextArea progressArea` - Область отображения прогресса
- `JScrollPane progressScrollPane` - Прокрутка для логов

#### Методы:
- `MainWindow(RandomGeneratorService, LeetCodeService)` - Конструктор
- `show()` - Отображение окна
- `createInputPanel()` - Создание панели ввода данных
- `createButtonPanel()` - Создание панели кнопок
- `createProgressPanel()` - Создание панели прогресса
- `onFetchButtonClick(ActionEvent)` - Обработка получения задач из LeetCode
- `onGenerateButtonClick(ActionEvent)` - Обработка генерации числа
- `handleFetchResult(Set<Integer>)` - Обработка результата загрузки задач
- `addProgressStep(String)` - Добавление сообщения в лог прогресса
- `clearProgress()` - Очистка области прогресса

#### Ответственность:
- ✅ Создание и управление GUI компонентами
- ✅ Обработка пользовательских событий
- ✅ Асинхронное выполнение длительных операций
- ✅ **Отображение интерактивного прогресса в реальном времени**
- ✅ Отображение результатов и ошибок
- ✅ Валидация пользовательского ввода

#### Особенности:
- Использует `SwingWorker` для асинхронных операций
- Блокирует UI во время загрузки данных
- **Показывает детальный прогресс выполнения операций**
- **Автоматическая прокрутка логов к последнему сообщению**
- **Временные метки для каждого события**
- **Эмодзи для визуального разделения типов сообщений**
- Автоматически форматирует список исключений

---

### 🎲 RandomGeneratorService
**Пакет**: `com.example.random.service`
**Назначение**: Сервис для генерации случайных чисел с исключениями

```java
public class RandomGeneratorService
```

#### Методы:
- `generateRandomNumber(int min, int max, Set<Integer> excludeSet)` - Генерация случайного числа
- `parseExcludeNumbers(String excludeText)` - Парсинг строки исключений
- `formatExcludeNumbers(Set<Integer> numbers)` - Форматирование множества чисел в строку

#### Параметры генерации:
- **min**: Минимальное значение (включительно)
- **max**: Максимальное значение (включительно)
- **excludeSet**: Множество исключаемых чисел

#### Возвращаемые значения:
- `Integer` - Случайное число или `null` если нет доступных чисел
- `Set<Integer>` - Множество исключаемых чисел
- `String` - Отформатированная строка чисел

#### Ответственность:
- ✅ Генерация случайных чисел в диапазоне
- ✅ Исключение заданных чисел из выборки
- ✅ Парсинг и валидация входных данных
- ✅ Форматирование результатов

#### Алгоритм генерации:
1. Создание списка всех возможных чисел в диапазоне
2. Исключение заданных чисел
3. Перемешивание списка (`Collections.shuffle`)
4. Возврат первого элемента

---

### 🌐 LeetCodeService
**Пакет**: `com.example.random.service`
**Назначение**: Высокоуровневый сервис для работы с данными LeetCode

```java
public class LeetCodeService
```

#### Поля:
- `LeetCodeScraper scraper` - Экземпляр скрапера

#### Методы:
- `LeetCodeService()` - Конструктор по умолчанию
- `LeetCodeService(LeetCodeScraper scraper)` - Конструктор с внедрением зависимости
- `getSolvedProblems(String username)` - Получение решенных задач
- `getSolvedProblems(String username, Consumer<String> progressCallback)` - **Получение с callback для прогресса**
- `isValidUsername(String username)` - Валидация имени пользователя

#### Ответственность:
- ✅ Фасад для работы с LeetCode API
- ✅ Валидация входных параметров
- ✅ Обработка бизнес-логики
- ✅ Инкапсуляция сложности скрапинга
- ✅ **Передача прогресса выполнения через callback**

#### Исключения:
- `IllegalArgumentException` - При пустом или null username

#### Новые возможности:
- **Progress Callback**: Позволяет получать уведомления о ходе выполнения операций
- **Real-time Updates**: Информация передается по мере выполнения, а не по завершении

---

### 🕷️ LeetCodeScraper
**Пакет**: `com.example.random.scraper`
**Назначение**: Веб-скрапинг данных с LeetCode с интерактивным прогрессом

```java
public class LeetCodeScraper
```

#### Константы:
- `DEFAULT_WAIT_TIME = 3000` - Время ожидания по умолчанию
- `JSON_WAIT_TIME = 2000` - Время ожидания для JSON API
- `SOLVED_PROBLEMS_WAIT = 5000` - Время ожидания решенных задач

#### Поля:
- `Consumer<String> progressCallback` - **Callback для отображения прогресса**

#### Методы:
- `setProgressCallback(Consumer<String> callback)` - **Установка callback для прогресса**
- `fetchSolvedProblems(String username)` - Главный метод получения решенных задач
- `getSolvedProblemsFromProfile(Page, String)` - Получение задач с профиля
- `clickRecentAC(Page)` - Поиск и клик по кнопке "Recent AC"
- `extractSolvedProblems(Page)` - Извлечение решенных задач со страницы
- `getAllProblemsFromAPI(Page)` - Получение всех задач через JSON API
- `parseProblemsFromJSON(String)` - Парсинг JSON ответа
- `matchSolvedProblemsWithNumbers(Set, Map)` - Сопоставление задач с номерами
- `logProgress(String message)` - **Логирование прогресса с передачей в callback**

#### Ответственность:
- ✅ Управление веб-браузером через Playwright
- ✅ Навигация по страницам LeetCode
- ✅ Извлечение данных с веб-страниц
- ✅ Парсинг JSON API ответов
- ✅ Обработка ошибок веб-скрапинга
- ✅ **Предоставление детального прогресса выполнения**
- ✅ **Real-time уведомления о ходе операций**

#### Особенности:
- Использует несколько стратегий поиска элементов
- Адаптируется к изменениям в UI LeetCode
- **Передает прогресс через Consumer<String> callback**
- **Показывает статистику на каждом этапе**
- **Информативные сообщения с эмодзи для лучшего UX**
- Автоматически закрывает браузер после работы

---

### 🔍 ProblemMatcher
**Пакет**: `com.example.random.scraper`
**Назначение**: Сопоставление названий задач с их номерами

```java
public class ProblemMatcher
```

#### Константы:
- `SIMILARITY_THRESHOLD = 0.8` - Порог схожести для нечеткого поиска

#### Методы:
- `findProblemNumber(String solvedTitle, Map<String, Integer> allProblems)` - Поиск номера задачи
- `findBestMatch(String, Map)` - Нечеткое сопоставление
- `isMatchingTitle(String, String)` - Проверка соответствия названий
- `normalizeTitle(String)` - Нормализация названия задачи
- `calculateSimilarity(String, String)` - Вычисление коэффициента схожести
- `levenshteinDistance(String, String)` - Расстояние Левенштейна

#### Алгоритм сопоставления:
1. **Точное совпадение** - проверка прямого соответствия
2. **Нормализация** - приведение к нижнему регистру, удаление спецсимволов
3. **Проверка вхождения** - один заголовок содержит другой
4. **Нечеткое сравнение** - использование расстояния Левенштейна

#### Нормализация заголовка:
```java
title.toLowerCase()
     .replaceAll("[^a-z0-9\\s]", "")  //Удаляю спецсимволов
     .replaceAll("\\s+", " ")         //Множественные пробелы в один
     .trim()
```

#### Ответственность:
- ✅ Точное сопоставление названий задач
- ✅ Нечеткий поиск при различиях в форматировании
- ✅ Нормализация текста для сравнения
- ✅ Вычисление метрик схожести строк

---

## 🎮 Использование

### Запуск приложения:
```bash
./gradlew run
```

### Интерфейс приложения:
1. **Профиль LeetCode** - введите username
2. **Значение от/до** - задайте диапазон чисел
3. **Исключить** - числа через запятую (заполняется автоматически)
4. **Получить решенные задачи** - загрузка данных с LeetCode
5. **Сгенерировать** - создание случайного числа
6. **Панель прогресса** - отображение хода выполнения операций

### Интерактивный прогресс выполнения:
Приложение показывает детальный прогресс всех операций в реальном времени


## 📁 Структура проекта

```
├── build.gradle                      # Конфигурация сборки
├── gradle/
│   └── wrapper/                      # Gradle Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/example/random/
│   │   │   ├── RandomNumberApp.java              # Главный класс
│   │   │   ├── ui/
│   │   │   │   └── MainWindow.java               # UI с интерактивным прогрессом
│   │   │   ├── service/
│   │   │   │   ├── LeetCodeService.java          # Сервис с callback поддержкой
│   │   │   │   └── RandomGeneratorService.java   # Сервис генерации чисел
│   │   │   └── scraper/
│   │   │       ├── LeetCodeScraper.java          # Веб-скрапинг с real-time логами
│   │   │       └── ProblemMatcher.java           # Сопоставление задач
│   │   └── resources/                # Ресурсы приложения
│   └── test/
│       └── java/                     # Юнит тесты
└── README.md                         # Документация
```

## 🔧 Конфигурация

### Playwright настройки:
- **Браузер**: Chromium
- **Режим**: Headless отключен (для отладки)
- **Замедление**: 300ms между действиями
- **Таймауты**: Настроены для стабильной работы

### Логирование:
- Подробные логи процесса скрапинга
- Информация о найденных задачах
- Ошибки парсинга и сопоставления

## 🚨 Ограничения

- Зависит от текущей структуры сайта LeetCode
- Требует стабильного интернет-соединения
- Может быть медленным из-за веб-скрапинга
- Ограничения антибот-систем LeetCode

## 🔄 Возможные улучшения

- [ ] Кэширование результатов скрапинга
- [ ] Сохранение профилей пользователей
- [ ] Темная тема интерфейса
- [ ] Настройка таймаутов и селекторов