# Руководство по интеграции

Это руководство объясняет, как интегрировать `HikkiSdk` в целевое приложение в процессе реверс-инжиниринга (например, с
использованием Smali, Frida или патчинга).

## Обзор инициализации

Необходимо инициализировать два основных компонента:

1. **HikkiSdk Core**: Управляет настройками, хуками и логикой прокси.
2. **Ghost Framework**: Обрабатывает обходы защиты (подмена подписи, подмена установщика и т. д.).

---

## 1. Инициализация Core SDK

В идеале SDK должен быть инициализирован как можно раньше, обычно в `Application.onCreate()` или
`Application.attachBaseContext()`.

### Интеграция Smali (класс Application)

Найдите метод `onCreate` в классе `Application` целевого приложения и добавьте следующее:

```smali
# Lcom/target/app/MyApplication; -> onCreate()V

sget-object v1, Lhikki/sdk/hooks/HikkiSdk;->INSTANCE:Lhikki/sdk/hooks/HikkiSdk;

invoke-virtual {v1, p0}, Lhikki/sdk/hooks/HikkiSdk;->init(Landroid/app/Application;)V
```

### Ручной патчинг (MainActivity/Application)

Если вы не можете изменить класс Application, вы можете инициализировать его в `onCreate` класса `MainActivity`:

```smali
# Lcom/target/app/MainActivity; -> onCreate(Landroid/os/Bundle;)V

invoke-virtual {p0}, Landroid/app/Application;->getApplicationContext()Landroid/content/Context;

move-result-object v0

sget-object v1, Lhikki/sdk/hooks/HikkiSdk;->INSTANCE:Lhikki/sdk/hooks/HikkiSdk;

invoke-virtual {v1, v0}, Lhikki/sdk/hooks/HikkiSdk;->init(Landroid/content/Context;)V
```

---

## 2. Ghost Framework (Защита от взлома)

Метод `initGhost` специально разработан для обхода проверок безопасности. Его следует вызывать в `attachBaseContext`,
чтобы гарантировать, что он запустится до того, как приложение выполнит проверку подписи.

### Интеграция Smali (attachBaseContext)

```smali
# Lcom/target/app/MyApplication; -> attachBaseContext(Landroid/content/Context;)V

sget-object v1, Lhikki/sdk/hooks/HikkiSdk;->INSTANCE:Lhikki/sdk/hooks/HikkiSdk;

invoke-virtual {v1, v0}, Lhikki/sdk/hooks/HikkiSdk;->initGhost(Landroid/content/Context;)V
```

---

## 3. Требования к Manifest

Для работы SDK (удаленные настройки) в `AndroidManifest.xml` должно быть следующее:

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

## 4. Интеграция функций

Вы можете использовать `FeatureHooks` для проверки определенных функций или статуса подписки внутри целевого приложения.

### Интеграция Smali (Проверка подписки)

Чтобы проверить, подписан ли пользователь, вы можете создать экземпляр `FeatureHooks` и вызвать `isSubscribed()`:

```smali
new-instance v1, Lhikki/sdk/hooks/FeatureHooks;

invoke-direct {v1}, Lhikki/sdk/hooks/FeatureHooks;-><init>()V

invoke-virtual {v1}, Lhikki/sdk/hooks/FeatureHooks;->isSubscribed()Z

move-result v0

# v0 теперь содержит булев результат (0 или 1)
```

---

## 5. Устранение неполадок

### Проблемы с контекстом (Context)

Если `HikkiSdk.context` выдает `IllegalStateException`, это означает, что `init(context)` никогда не вызывался. У SDK
есть резервный механизм, использующий рефлексию для поиска `ActivityThread`, но безопаснее вызывать `init` явно.

### Прокси не применяется

SDK вызывает `settingsManager.applyProxyOnStart()` во время `init`. Убедитесь, что `init` вызывается до того, как
приложение сделает свой первый сетевой запрос.

### Сбои Ghost Framework

`initGhost` должен быть вызван **до** любого кода, который проверяет подпись APK. Рекомендуемое место —
`attachBaseContext`.
