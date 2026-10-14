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
├─ lab1/                # Лабораторная работа № 1
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
./gradlew :lab1:run
```

## Основные команды

| Команда                   | Что делает                                               |
|---------------------------|----------------------------------------------------------|
| `./gradlew check`         | Все тесты + Checkstyle + PMD + SpotBugs                  |
| `./gradlew test`          | Только тесты                                             |
| `./gradlew :lab1:run`     | Запуск приложения из lab1                                |
| `./gradlew :lab1:check`   | Проверки только для lab1                                 |
| `./gradlew spotlessApply` | Автоформат всего кода (Google Java Format, 100 символов) |
| `./gradlew spotlessCheck` | Проверить формат, ничего не менять                       |
| `./gradlew installHooks`  | Активировать git-хуки                                    |
| `./gradlew clean`         | Удалить все сборочные артефакты                          |

## Стандарты кода

- **Формат**: [Google Java Format](https://github.com/google/google-java-format), 4 пробела, 100
  символов в строке, LF.
- **Проверки**: Checkstyle, PMD, SpotBugs, Error Prone.
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
       application
   }

   application {
       mainClass.set("org.nstu.se.labN.Main")
   }
   ```

3. Добавить `include("labN")` в `settings.gradle.kts`.
4. Исходники — в `labN/src/main/java/org/nstu/se/labN/`, ресурсы — в
   `labN/src/main/resources/org/nstu/se/labN/`.

## Полезные ссылки

- [lab1/README.md](./lab1/README.md)
