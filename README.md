# SE Labs

Лабораторные работы по дисциплине «Инжиниринг ПО. Часть 1. Паттерны программирования и
проектирования ПО».

## Требования

- **JDK 25+**.
  Gradle-обёртка устанавливается автоматически при первом запуске — отдельно Gradle ставить не
  нужно.
- **Git**. На Windows — с Git Bash, чтобы корректно работали хуки.

## Структура репозитория

```
se-labs/
├─ build-logic/         # Convention-плагины Gradle (правила сборки для всех лаб)
├─ config/
│  ├─ checkstyle/       # Правила Checkstyle
│  └─ pmd/              # Правила PMD
├─ gradle/
│  └─ libs.versions.toml  # Единый каталог версий (JavaFX, JUnit и т.д.)
├─ .githooks/           # Git-хуки (pre-commit, pre-push)
├─ .github/workflows/   # CI на GitHub Actions
├─ lab1/                # Лабораторная работа № 1 — аниматор сортировки
├─ lab2/                # Лабораторная работа № 2 — RxService
├─ lab3/                # Лабораторная работа № 3 — динамический элемент GUI (дБ-индикатор)
├─ lab4/                # Лабораторная работа № 4 — звуковая волна (Observer + Proxy)
├─ settings.gradle.kts
├─ build.gradle.kts
└─ gradlew, gradlew.bat
```

## Быстрый старт

```bash
# 1. Активировать git-хуки (один раз)
./gradlew installHooks

# 2. Собрать всё и прогнать проверки
./gradlew check

# 3. Запустить конкретную лабу
./gradlew :lab1:run    # аниматор сортировки
./gradlew :lab2:run    # RxService
./gradlew :lab3:run    # логарифмический индикатор уровня (дБ)
./gradlew :lab4:run    # звуковая волна с микшированием
```

## Основные команды

| Команда                   | Что делает                                               |
|---------------------------|----------------------------------------------------------|
| `./gradlew check`         | Все тесты + Checkstyle + PMD + SpotBugs                  |
| `./gradlew test`          | Только тесты                                             |
| `./gradlew :lab1:run`     | Запуск приложения из lab1                                |
| `./gradlew :lab2:run`     | Запуск приложения из lab2                                |
| `./gradlew :lab3:run`     | Запуск приложения из lab3                                |
| `./gradlew :lab4:run`     | Запуск приложения из lab4                                |
| `./gradlew :lab1:check`   | Проверки только для lab1                                 |
| `./gradlew :lab2:check`   | Проверки только для lab2                                 |
| `./gradlew :lab3:check`   | Проверки только для lab3                                 |
| `./gradlew :lab4:check`   | Проверки только для lab4                                 |
| `./gradlew spotlessApply` | Автоформат всего кода (Google Java Format, 100 символов) |
| `./gradlew spotlessCheck` | Проверить формат, ничего не менять                       |
| `./gradlew installHooks`  | Активировать git-хуки                                    |
| `./gradlew clean`         | Удалить все сборочные артефакты                          |

## Лабораторные работы

| № | Тема                                                                    | README                             |
|---|-------------------------------------------------------------------------|------------------------------------|
| 1 | Аниматор сортировки с динамической разметкой                            | [lab1/README.md](./lab1/README.md) |
| 2 | Асинхронный вызов методов через аннотации (`@RxMethod`, `@RxParameter`) | [lab2/README.md](./lab2/README.md) |
| 3 | Элемент GUI с динамическим поведением (Индикатор уровня в дБ)           | [lab3/README.md](./lab3/README.md) |
| 4 | Модель с подпиской на события: звуковая волна (Observer + Proxy + Mix)  | [lab4/README.md](./lab4/README.md) |

## Стандарты кода

- **Формат**: [Google Java Format](https://github.com/google/google-java-format), 4 пробела, 100
  символов в строке, LF.
- **Проверки**: Checkstyle, PMD, SpotBugs, Error Prone, Spotless.
- **Версия Java**: 25+ (toolchain).
- **JavaFX**: 27.

Все правила настраиваются в `build-logic/src/main/kotlin/org/nstu/se/` и в `config/`.

## CI

При каждом push в `main` и при открытии pull request GitHub Actions запускает:

```bash
./gradlew spotlessCheck check --no-daemon
```

Если проверка падает — PR нельзя смержить (branch protection).

## Добавление новой лабораторной

1. Создать директорию `labN/`.
2. Положить туда `build.gradle.kts`:

   ```kotlin
   plugins {
	   id("se-labs.javafx-conventions")   // если нужен JavaFX
	   id("se-labs.test-conventions")
	   id("se-labs.quality-conventions")
   }

   application {
	   mainClass.set("org.nstu.se.labN.Main")
   }
   ```

3. Добавить `include("labN")` в `settings.gradle.kts`.
4. Исходники — в `labN/src/main/java/org/nstu/se/labN/`, ресурсы — в
   `labN/src/main/resources/org/nstu/se/labN/`.
5. Завести `labN/README.md` и добавить ссылку в таблицу «Лабораторные работы».
