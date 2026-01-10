# Hikki SDK

Hikki SDK is a comprehensive toolkit designed for Android application reverse engineering, providing robust features for hooking, proxying, and bypassing anti-tamper mechanisms to manage them inside Hikki store application.

## Features

- **Core SDK**: Centralized management for settings, hooks, and proxy logic.
- **Ghost Framework**: Advanced anti-tamper bypasses, including signature spoofing and installer spoofing.
- **Remote Settings**: Support for remote configuration via Hikki.
- **Feature Hooks**: Simplified interface for checking application features, subscription status, and more.

## Documentation

Explore the following guides to get started:

- [**Integration Guide**](INTEGRATION.md): Detailed instructions on integrating the SDK into target applications using Smali, Frida, or manual patching.
- [**Compiling Guide**](COMPILING.md): Step-by-step guide on building the SDK and extracting the necessary DEX files.
- [**Settings Configuration Guide**](SETTINGS_GUIDE.md): Information on how to define, manage, and localize SDK settings.

## Quick Start

1. **Build the SDK**: Follow the [Compiling Guide](COMPILING.md) to generate the DEX files.
2. **Configure**: Customize the SDK behavior using the [Settings Configuration Guide](SETTINGS_GUIDE.md).
3. **Integrate**: Use the [Integration Guide](INTEGRATION.md) to inject the SDK into your target APK.