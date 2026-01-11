# Руководство по настройке параметров

Это руководство объясняет, как определять и управлять настройками в SDK.

## Обзор

Настройки определяются как список объектов `ConfigItem`. Эти элементы обычно инициализируются в `SettingsUtils.kt`
внутри функции `getDefaultSettings()`.

## Поддерживаемые типы настроек

Следующие типы поддерживаются через запечатанный интерфейс (sealed interface) `ConfigItem`:

| Тип                          | Класс         | Описание                                                     |
|:-----------------------------|:--------------|:-------------------------------------------------------------|
| **Раздел**                   | `SectionItem` | Группирует связанные настройки вместе.                       |
| **Логический**               | `BooleanItem` | Простой переключатель (true/false).                          |
| **Строковый**                | `StringItem`  | Текстовое поле ввода.                                        |
| **Целое число**              | `IntItem`     | Числовое поле ввода для целых чисел.                         |
| **Число с плавающей точкой** | `FloatItem`   | Числовое поле ввода для чисел с плавающей точкой.            |
| **Enum (Перечисление)**      | `EnumItem`    | Выпадающий список/выбор из предопределенных вариантов.       |
| **Дата**                     | `DateItem`    | Выбор даты (сохраняет значение как метку времени `Long`).    |
| **Время**                    | `TimeItem`    | Выбор времени (сохраняет значение как метку времени `Long`). |
| **Кнопка**                   | `ButtonItem`  | Кнопка действия (например, "Применить", "Сброс").            |

## Как создавать настройки

### 1. Определите раздел

Каждая настройка должна принадлежать разделу. Сначала вы должны определить `SectionItem` для группировки ваших настроек.

```kotlin
ConfigItem.SectionItem(
    title = "General".localized(ru = "Общие"),
    name = "general_section",
    section = "general",
    description = "General settings description"
)
```

### 2. Добавьте элементы настроек

Добавьте элементы в список, убедившись, что свойство `section` совпадает с `section` вашего `SectionItem`.

```kotlin
ConfigItem.BooleanItem(
    title = "Enable Feature".localized(ru = "Включить функцию"),
    name = "feature_enabled",
    section = "general", // Совпадает с именем раздела выше
    description = "Toggle this feature on or off",
    value = true
)
```

### 3. Локализация

Используйте функцию расширения `.localized()` для строк, которые должны отображаться на разных языках.

```kotlin
"English Text".localized(
    ru = "Русский текст",
    be = "Беларускі тэкст"
)
```

## Пример реализации

В `SettingsUtils.kt`:

```kotlin
fun getDefaultSettings(): List<ConfigItem> {
    return listOf(
        ConfigItem.SectionItem(
            title = "Proxy".localized(ru = "Прокси"),
            name = "proxy_section",
            section = "proxy",
            description = "Configure your proxy settings"
        ),
        ConfigItem.StringItem(
            title = "Host".localized(ru = "Хост"),
            name = "proxy_host",
            section = "proxy",
            description = "Enter proxy address",
            value = "127.0.0.1"
        ),
        ConfigItem.EnumItem(
            title = "Type".localized(ru = "Тип"),
            name = "proxy_type",
            section = "proxy",
            options = listOf("HTTP", "SOCKS5"),
            value = "HTTP"
        )
    )
}
```

## Обработка нажатий кнопок

При нажатии на `ButtonItem` вызывается функция `processButtonItem` в `SettingsUtils.kt`. Вы должны обработать действие
кнопки на основе ее `name`.

```kotlin
fun processButtonItem(settingsManager: SettingsManager, item: ConfigItem.ButtonItem) {
    when (item.name) {
        "my_button_name" -> {
            // Выполнить действие
        }
    }
}
```

## Ключевые свойства

- **title**: Отображаемое имя, показанное пользователю.
- **name**: Уникальный идентификатор настройки (используется для хранения/извлечения).
- **section**: Идентификатор группы, к которой принадлежит эта настройка.
- **description**: Необязательная подсказка или пояснительный текст.
- **value**: Значение по умолчанию для настройки.
- **options**: (Только для Enum) Список доступных строк для выбора.
