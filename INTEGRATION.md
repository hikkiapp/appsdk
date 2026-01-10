# Integration Guide

This guide explains how to integrate `HikkiSdk` into a target application during reverse engineering (e.g., using Smali,
Frida, or patching).

## Initialization Overview

There are two main components to initialize:

1. **HikkiSdk Core**: Manages settings, hooks, and proxy logic.
2. **Ghost Framework**: Handles anti-tamper bypasses (signature spoofing, installer spoofing, etc.).

---

## 1. Core SDK Initialization

The SDK should ideally be initialized as early as possible, typically in the `Application.onCreate()` or
`Application.attachBaseContext()`.

### Smali Integration (Application class)

Find the `onCreate` method in the target's `Application` class and add the following:

```smali
# Lcom/target/app/MyApplication; -> onCreate()V

sget-object v1, Lhikki/sdk/hooks/HikkiSdk;->INSTANCE:Lhikki/sdk/hooks/HikkiSdk;

invoke-virtual {v1, p0}, Lhikki/sdk/hooks/HikkiSdk;->init(Landroid/app/Application;)V
```

### Manual Patching (MainActivity/Application)

If you cannot modify the Application class, you can initialize it in the `onCreate` of the `MainActivity`:

```smali
# Lcom/target/app/MainActivity; -> onCreate(Landroid/os/Bundle;)V

invoke-virtual {p0}, Landroid/app/Application;->getApplicationContext()Landroid/content/Context;

move-result-object v0

sget-object v1, Lhikki/sdk/hooks/HikkiSdk;->INSTANCE:Lhikki/sdk/hooks/HikkiSdk;

invoke-virtual {v1, v0}, Lhikki/sdk/hooks/HikkiSdk;->init(Landroid/content/Context;)V
```

---

## 2. Ghost Framework (Anti-Tamper)

The `initGhost` method is specifically designed to bypass security checks. It should be called in `attachBaseContext` to
ensure it runs before the app performs signature checks.

### Smali Integration (attachBaseContext)

```smali
# Lcom/target/app/MyApplication; -> attachBaseContext(Landroid/content/Context;)V

sget-object v1, Lhikki/sdk/hooks/HikkiSdk;->INSTANCE:Lhikki/sdk/hooks/HikkiSdk;

invoke-virtual {v1, v0}, Lhikki/sdk/hooks/HikkiSdk;->initGhost(Landroid/content/Context;)V
```

---

## 3. Manifest Requirements

For the SDK to function (remote settings), the following must be present in the `AndroidManifest.xml`:

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

## 4. Feature Integration

You can use `FeatureHooks` to check for specific features or subscription status within the target app.

### Smali Integration (Checking Subscription)

To check if a user is subscribed, you can instantiate `FeatureHooks` and call `isSubscribed()`:

```smali
new-instance v1, Lhikki/sdk/hooks/FeatureHooks;

invoke-direct {v1}, Lhikki/sdk/hooks/FeatureHooks;-><init>()V

invoke-virtual {v1}, Lhikki/sdk/hooks/FeatureHooks;->isSubscribed()Z

move-result v0

# v0 now contains the boolean result (0 or 1)
```

---

## 5. Troubleshooting

### Context Issues

If `HikkiSdk.context` throws an `IllegalStateException`, it means `init(context)` was never called. The SDK has a
fallback mechanism using reflection to find the `ActivityThread`, but it is safer to call `init` explicitly.

### Proxy Not Applying

The SDK calls `settingsManager.applyProxyOnStart()` during `init`. Ensure `init` is called before the app makes its
first network request.

### Ghost Framework Failures

`initGhost` must be called **before** any code that validates the APK signature. `attachBaseContext` is the recommended
location.
