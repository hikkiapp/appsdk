# Руководство по интеграции

В этом руководстве мы разберем, как интегрировать `HikkiSdk` в стороннее приложение. Эти методы применимы при
реверс-инжиниринге с использованием Smali, Frida или прямого патчинга байт-кода.

## Как это работает

Для полноценной работы SDK необходимо инициализировать два ключевых модуля:

1. **HikkiSdk Core**: Отвечает за базовую логику, работу с настройками и прокси.
2. **Ghost Framework**: Занимается обходом защитных механизмов (подмена подписи, проверка установщика и т.д.).

---

## 1. Инициализация Core SDK

Мы рекомендуем инициализировать SDK как можно раньше — лучше всего в методах `Application.onCreate()` или
`Application.attachBaseContext()`.

### Интеграция через Smali (в классе Application)

Найдите метод `onCreate` в классе `Application` и вставьте следующий код:

```smali
# Lcom/target/app/MyApplication; -> onCreate()V

sget-object v1, Lhikki/sdk/hooks/HikkiSdk;->INSTANCE:Lhikki/sdk/hooks/HikkiSdk;

invoke-virtual {v1, p0}, Lhikki/sdk/hooks/HikkiSdk;->init(Landroid/app/Application;)V
```

### Если нельзя изменить Application (патчинг MainActivity)

Если доступ к классу Application ограничен, инициализацию можно выполнить в `onCreate` главной Activity:

```smali
# Lcom/target/app/MainActivity; -> onCreate(Landroid/os/Bundle;)V

invoke-virtual {p0}, Landroid/app/Application;->getApplicationContext()Landroid/content/Context;

move-result-object v0

sget-object v1, Lhikki/sdk/hooks/HikkiSdk;->INSTANCE:Lhikki/sdk/hooks/HikkiSdk;

invoke-virtual {v1, v0}, Lhikki/sdk/hooks/HikkiSdk;->init(Landroid/content/Context;)V
```

---

## 2. Ghost Framework (Обход защиты)

Метод `initGhost` критически важен для нейтрализации проверок целостности. Его необходимо вызывать строго в
`attachBaseContext`, чтобы он сработал до того, как приложение начнет проверять свою подпись.

### Интеграция через Smali (в attachBaseContext)

```smali
# Lcom/target/app/MyApplication; -> attachBaseContext(Landroid/content/Context;)V

sget-object v1, Lhikki/sdk/hooks/HikkiSdk;->INSTANCE:Lhikki/sdk/hooks/HikkiSdk;

invoke-virtual {v1, v0}, Lhikki/sdk/hooks/HikkiSdk;->initGhost(Landroid/content/Context;)V
```

---

## 3. Настройка AndroidManifest

Чтобы SDK мог принимать настройки удаленно, добавьте в `AndroidManifest.xml` следующий ресивер:

```xml
<receiver
        android:name="hikki.sdk.receiver.RemoteSettingsReceiver"
        android:exported="true">
    <intent-filter>
        <action android:name="app.hikki.api.GET_CONFIG"/>
        <action android:name="app.hikki.api.SET_CONFIG"/>
    </intent-filter>
</receiver>
```

---

## 4. Использование функциональных хуков

Класс `FeatureHooks` позволяет легко проверять состояние подписок или доступность функций внутри приложения.

### Пример проверки подписки (Smali)

```smali
new-instance v1, Lhikki/sdk/hooks/FeatureHooks;

invoke-direct {v1}, Lhikki/sdk/hooks/FeatureHooks;-><init>()V

invoke-virtual {v1}, Lhikki/sdk/hooks/FeatureHooks;->isSubscribed()Z

move-result v0

# v0 теперь содержит результат (0 или 1)
```

---

## 5. Решение проблем

### Ошибка Context (IllegalStateException)

Если вы видите эту ошибку, значит `init(context)` не был вызван вовремя. Хотя SDK пытается найти контекст через
рефлексию, лучше всегда вызывать `init` явно.

### Прокси не работает

Убедитесь, что `init` вызывается до того, как приложение отправляет свои первые сетевые запросы. SDK применяет настройки
прокси именно в момент инициализации.

### Сбои Ghost Framework

Помните: `initGhost` должен быть вызван **до** любой проверки подписи. Если приложение падает или обнаруживает подмену,
проверьте, не вызывается ли проверка раньше, чем отрабатывает `attachBaseContext`.
