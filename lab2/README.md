# Лабораторная работа №2

Асинхронный вызов методов через аннотации (`RxService`).

Сервисный слой, который по текстовому запросу `метод:ключ=значение,...` находит помеченный
аннотацией `@RxMethod` метод, связывает параметры через `@RxParameter`, приводит строки к нужным
типам и вызывает метод в отдельном потоке. Результат возвращается через `Future<Object>`.
К сервису прилагается JavaFX-интерфейс: выбор реализации, таблица аннотированных методов,
поле ввода запроса и журнал выполнения.

## Что реализовано

- [x] Аннотации `@RxMethod` и `@RxParameter` с `RUNTIME`-хранением.
- [x] Парсер запроса `метод:ключ=значение;ключ=значение` с поддержкой кавычек, экранирования
  и разделителей `,`/`;`.
- [x] Сканер класса (`ServiceScanner`): сбор `@RxMethod`, валидация имён, дедупликация,
  сортировка по имени.
- [x] Связывание аргументов (`ArgumentBinder`) с конвертерами `int`, `double`, `String` и
  значениями по умолчанию для неаннотированных параметров.
- [x] `RxService.submit(...)` на виртуальных потоках с поддержкой `Future`, отменой
  незавершённых задач и `shutdown`.
- [x] Готовые сервисы: `SumService`, `CalculatorService`, `TextService`, `MathService`.
- [x] JavaFX-интерфейс: селектор сервиса, таблица методов с примером запроса, поле ввода,
  статус-бар, журнал, индикатор выполнения.
- [x] Юнит-тесты (`JUnit 5`, `AssertJ`) — парсинг, сканирование, конвертация, асинхронный
  вызов, отмена, готовые сервисы.

## Запуск приложения

```bash
./gradlew :lab2:run
```

## Тестирование

```bash
# Запуск тестов для лабораторной работы №2
./gradlew :lab2:test

# Запуск всех проверок (тесты + Checkstyle + PMD + SpotBugs)
./gradlew :lab2:check
```

## Формат запроса

```
метод:параметр1=значение1,параметр2=значение2
```

- Разделители пар — `,` или `;`.
- Пробелы вокруг разделителей и `=` игнорируются.
- Значения можно брать в двойные кавычки: `"foo,bar"` — тогда `,` и `;` внутри не считаются
  разделителями.
- Внутри кавычек работают экранирования `\"`, `\\`, `\n`, `\r`, `\t`.

Примеры для готовых сервисов:

```
sum:inp1=15,inp2=16,inp3=4
add:a=2,b=3
sub:a=5,b=3
mul:a=4,b=3
div:a=12,b=3
pow:base=2,exp=10
sqrt:value=9
concat:left="foo,bar",right="baz;qux"
upper:text=hello
length:text=hello
sleep:ms=500
```

## Структура

```
lab2/
├─ build.gradle.kts
└─ src/
   ├─ main/
   │  ├─ java/
   │  │  ├─ module-info.java                 — описание модуля org.nstu.se.rx
   │  │  └─ org/nstu/se/lab2/
   │  │     ├─ Launcher.java                 — точка входа JVM
   │  │     ├─ AnnotationsApplication.java   — класс JavaFX Application
   │  │     ├─ Messages.java                 — интернационализация
   │  │     ├─ annotations/                  — @RxMethod, @RxParameter
   │  │     ├─ core/                         — RxService, RxRequest, дескрипторы методов
   │  │     │  ├─ convert/                   — ValueConverter и регистрация конвертеров
   │  │     │  ├─ exceptions/                — RxException, ConverterException,
   │  │     │  │                                RxInvocationException
   │  │     │  ├─ parsing/                   — RequestParser, ParameterParser
   │  │     │  └─ reflection/                — ServiceScanner, ArgumentBinder
   │  │     ├─ services/                     — Sum/Calculator/Text/Math + ServiceEntry,
   │  │     │                                    ServiceCatalog
   │  │     └─ ui/                           — MainController, CommandExecutor,
   │  │                                          ServiceSwitcher, MethodInfo и др.
   │  └─ resources/org/nstu/se/lab2/
   │     ├─ main_view.fxml                   — FXML экрана
   │     ├─ app.properties                   — текстовые ресурсы
   │     └─ css/app.css                      — стили приложения
   └─ test/java/org/nstu/se/lab2/
      ├─ CalculatorServiceTest.java          — арифметические операции
      ├─ MathServiceTest.java                — pow/sqrt и ошибки
      ├─ TextServiceTest.java                — concat/upper/length, значения в кавычках
      └─ core/
         ├─ RxServiceConverterTest.java      — отсутствие конвертера, ошибки конвертации
         ├─ RxServiceInvocationTest.java     — асинхронный вызов, отмена, shutdown,
         │                                      static-методы, значения по умолчанию
         ├─ RxServiceParsingTest.java        — форматы запроса, кавычки, экранирование, ошибки
         └─ RxServiceScanTest.java           — сбор аннотированных методов, дубликаты,
                                                пустые имена
```

## Ключевые классы

- **`RxService`** — фасад. Строит карту `@RxMethod`, регистрирует конвертеры, парсит запрос,
  связывает аргументы и вызывает метод в `Executors.newVirtualThreadPerTaskExecutor()`. Возвращает
  `Future<Object>`; `shutdown()` отменяет незавершённые задачи.
- **`ServiceScanner`** — рефлексивно обходит `getMethods()` и `getDeclaredMethods()`, собирает
  `MethodDescriptor` и валидирует имена `@RxMethod`/`@RxParameter`.
- **`ArgumentBinder`** — по `@RxParameter.name()` достаёт строку из `Map<String, String>`,
  конвертирует через `ValueConverter` и подставляет значения по умолчанию для неаннотированных
  параметров.
- **`RequestParser` / `ParameterParser`** — разбор `метод:ключ=значение,...` с поддержкой
  кавычек и экранирования.
- **`CommandExecutor`** (UI) — оборачивает `Future` с таймаутом и разворачивает
  `CompletionException`/`ExecutionException` до причины.
