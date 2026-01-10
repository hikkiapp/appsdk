package hikki.sdk.ghost

data class GhostConfig(
    // ==========================================
    // 1. App Identity & Signature Spoofing
    // ==========================================
    /**
     * Base64 encoded byte array of the original APK signatures.
     * Use this to pass genuine signatures to the system.
     */
    val base64Signatures: String = "",

    /**
     * If not null, replaces the PackageInfo.versionCode.
     */
    val spoofVersionCode: Long? = null,

    /**
     * If not null, replaces the PackageInfo.versionName.
     */
    val spoofVersionName: String? = null,

    /**
     * If true, removes the FLAG_DEBUGGABLE from ApplicationInfo.
     */
    val spoofDebugFlag: Boolean = true,

    // ==========================================
    // 2. Installer & Store Spoofing
    // ==========================================
    /**
     * If true, hooks getInstallerPackageName.
     */
    val spoofInstaller: Boolean = true,

    /**
     * The package name to return as the installer (default: Google Play Store).
     */
    val fakeInstallerName: String = "com.android.vending",

    // ==========================================
    // 3. Build & System Integrity Spoofing
    // ==========================================
    /**
     * If true, modifies android.os.Build static fields (TAGS, FINGERPRINT, TYPE)
     * to look like a certified "user" release build.
     */
    val spoofBuildProps: Boolean = true,

    /**
     * If true, filters getInstalledPackages and queryIntentActivities to remove
     * known root/hooking tools (Magisk, SuperSU, Xposed, LuckyPatcher).
     */
    val hideSuspiciousApps: Boolean = true,

    /**
     * A list of permission strings (e.g., "android.permission.CAMERA").
     * The framework will force checkPermission to return PERMISSION_GRANTED for these.
     */
    val forcedPermissions: List<String> = emptyList(),

    /**
     * A list of feature strings (e.g., "android.hardware.nfc").
     * The framework will force hasSystemFeature to return true for these.
     */
    val forcedSystemFeatures: List<String> = emptyList(),
)