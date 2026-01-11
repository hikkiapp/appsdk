# Руководство по настройке параметров

В этом руководстве мы разберем, как создавать и управлять параметрами конфигурации внутри SDK.

## Как устроены настройки

Все настройки представляют собой список объектов `ConfigItem`. Обычно они описываются в файле `SettingsUtils.kt` внутри
функции `getDefaultSettings()`.

## Поддерживаемые типы данных

Для разных задач предусмотрены разные типы элементов:

| Тип               | Класс         | Описание                                            |
|:------------------|:--------------|:----------------------------------------------------|
| **Раздел**        | `SectionItem` | Группирует настройки в логические блоки.            |
| **Переключатель** | `BooleanItem` | Обычный чекбокс (вкл/выкл).                         |
| **Текст**         | `StringItem`  | Поле для ввода текстовой строки.                    |
| **Целое число**   | `IntItem`     | Поле для ввода чисел (Integer).                     |
| **Дробное число** | `FloatItem`   | Поле для ввода чисел с плавающей точкой.            |
| **Список (Enum)** | `EnumItem`    | Выбор одного варианта из предложенного списка.      |
| **Дата**          | `DateItem`    | Календарь для выбора даты (сохраняется как `Long`). |
| **Время**         | `TimeItem`    | Выбор времени (сохраняется как `Long`).             |
| **Кнопка**        | `ButtonItem`  | Кнопка для запуска какого-либо действия.            |

## Создание новых настроек

### 1. Создаем раздел

Любая настройка должна находиться внутри раздела. Сначала определите `SectionItem`:

```kotlin
ConfigItem.SectionItem(
    title = "General".localized(ru = "Общие"),
    name = "general_section",
    section = "general",
    description = "Основные настройки приложения"
)
```

### 2. Добавляем параметры

Теперь можно добавить сами элементы. Главное, чтобы поле `section` совпадало с именем созданного ранее раздела:

```kotlin
ConfigItem.BooleanItem(
    title = "Enable Feature".localized(ru = "Включить функцию"),
    name = "feature_enabled",
    section = "general", // Привязка к разделу
    description = "Активирует дополнительный функционал",
    value = true
)
```

### 3. Локализация (Перевод)

Чтобы интерфейс был понятен пользователям на разных языках, используйте метод `.localized()`:

```kotlin
"English Text".localized(
    ru = "Русский текст",
    be = "Беларускі тэкст"
)
```

## Пример в коде

Вот как это выглядит в `SettingsUtils.kt`:

```kotlin
fun getDefaultSettings(): List<ConfigItem> {
    return listOf(
        ConfigItem.SectionItem(
            title = "Proxy".localized(ru = "Прокси"),
            name = "proxy_section",
            section = "proxy",
            description = "Настройки сетевого соединения"
        ),
        ConfigItem.StringItem(
            title = "Host".localized(ru = "Хост"),
            name = "proxy_host",
            section = "proxy",
            description = "Введите IP-адрес или домен",
            value = "127.0.0.1"
        )
    )
}
```

## Обработка нажатий на кнопки

Если вы добавили `ButtonItem`, логику его работы нужно прописать в функции `processButtonItem` в `SettingsUtils.kt`,
используя проверку по `name`.

```kotlin
fun processButtonItem(settingsManager: SettingsManager, item: ConfigItem.ButtonItem) {
    when (item.name) {
        "reset_settings" -> {
            // Код для сброса настроек
        }
    }
}
```

## Основные поля ConfigItem

- **title**: Заголовок, который видит пользователь.
- **name**: Техническое имя (ключ), по которому SDK находит значение.
- **section**: К какому разделу относится настройка.
- **description**: Подсказка или описание под заголовком.
- **value**: Значение по умолчанию.
- **options**: Список строк (только для `EnumItem`).
