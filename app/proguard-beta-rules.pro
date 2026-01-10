-repackageclasses 'hikki.sdk'
-keepattributes SourceFile,InnerClasses,EnclosingMethod,Signature,RuntimeVisibleAnnotations
-allowaccessmodification

-dontusemixedcaseclassnames
-verbose

-keep class hikki.sdk.hooks.*, hikki.sdk.receiver.*, hikki.sdk.manager.*, hikki.sdk.ghost.** { *; }
-keep,allowobfuscation,allowshrinking,allowoptimization class hikki.sdk.utils.* { *; }