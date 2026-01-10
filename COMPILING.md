# Compiling Guide

This guide explains how to compile the `HikkiSdk` and extract the resulting DEX files for integration.

## Build & Extract

The most efficient way to build the SDK is to use the extraction tasks. These tasks automatically trigger the compilation (assemble) and then extract the `.dex` files from the generated APK.

### 1. Release Build
Intended for production use. Includes full R8 optimization and strips all logs.
```bash
./gradlew extractReleaseDex
```
`./gradlew extractReleaseDex`

**Output Directory:** `app/build/outputs/apk/release/dex/`

### 2. Beta Build
Intended for testing. Preserves logs and uses a relaxed Proguard configuration for easier debugging.
```bash
./gradlew extractBetaDex
```
`./gradlew extractBetaDex`

**Output Directory:** `app/build/outputs/apk/beta/dex/`

---

If you only need the APK file without extracting DEX files, you can run:
- `./gradlew assembleRelease`
- `./gradlew assembleBeta`
