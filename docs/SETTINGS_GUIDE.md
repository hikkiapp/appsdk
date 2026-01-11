# Settings Configuration Guide

This guide explains how to define and manage settings in the SDK.

## Overview

Settings are defined as a list of `ConfigItem` objects. These items are typically initialized in `SettingsUtils.kt` within the `getDefaultSettings()` function.

## Supported Setting Types

The following types are supported via the `ConfigItem` sealed interface:

| Type | Class | Description |
| :--- | :--- | :--- |
| **Section** | `SectionItem` | Groups related settings together. |
| **Boolean** | `BooleanItem` | A simple toggle (true/false). |
| **String** | `StringItem` | A text input field. |
| **Integer** | `IntItem` | A numeric input field for integers. |
| **Float** | `FloatItem` | A numeric input field for floating-point numbers. |
| **Enum** | `EnumItem` | A dropdown/selection list from predefined options. |
| **Date** | `DateItem` | A date picker (stores value as `Long` timestamp). |
| **Time** | `TimeItem` | A time picker (stores value as `Long` timestamp). |
| **Button** | `ButtonItem` | An actionable button (e.g., "Apply", "Reset"). |

## How to Create Settings

### 1. Define a Section
Every setting must belong to a section. You should first define a `SectionItem` to group your settings.

```kotlin
ConfigItem.SectionItem(
    title = "General".localized(ru = "Общие"),
    name = "general_section",
    section = "general",
    description = "General settings description"
)
```

### 2. Add Setting Items
Add items to the list, ensuring the `section` property matches the `section` of your `SectionItem`.

```kotlin
ConfigItem.BooleanItem(
    title = "Enable Feature".localized(ru = "Включить функцию"),
    name = "feature_enabled",
    section = "general", // Matches the section name above
    description = "Toggle this feature on or off",
    value = true
)
```

### 3. Localization
Use the `.localized()` extension function for strings that need to be displayed in different languages.

```kotlin
"English Text".localized(
    ru = "Русский текст",
    be = "Беларускі тэкст"
)
```

## Example Implementation

In `SettingsUtils.kt`:

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

## Handling Button Clicks

When a `ButtonItem` is clicked, the `processButtonItem` function in `SettingsUtils.kt` is called. You should handle the
button action based on its `name`.

```kotlin
fun processButtonItem(settingsManager: SettingsManager, item: ConfigItem.ButtonItem) {
    when (item.name) {
        "my_button_name" -> {
            // Perform action
        }
    }
}
```

## Key Properties

- **title**: The display name shown to the user.
- **name**: The unique identifier for the setting (used for storage/retrieval).
- **section**: The identifier of the group this setting belongs to.
- **description**: Optional hint or explanation text.
- **value**: The default value for the setting.
- **options**: (Enum only) A list of available strings to choose from.
