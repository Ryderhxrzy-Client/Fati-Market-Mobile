# R8 rules for the release bundle.
#
# Most libraries here (OkHttp, Firebase, ZXing, Credential Manager) ship their
# own consumer rules, which AGP applies automatically -- these are the gaps.
#
# Note on models: the app parses every API response by hand with org.json and
# string literal keys, so MarketplaceModels classes carry no reflective
# contract and are safe for R8 to rename. Do not add blanket -keep rules for
# them; that would defeat the shrinking for no benefit.

# Readable crash reports. Keeping line numbers costs a few KB and makes the
# mapping file actually useful in Play Console / Crashlytics.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Generic signatures and annotations, needed by anything doing reflection over
# parameterised types (Gson inside the Pusher client, for one).
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*,RuntimeVisibleAnnotations,AnnotationDefault

# --- OkHttp / Okio -----------------------------------------------------------
# OkHttp references these optional TLS providers only when they are on the
# classpath; we do not ship them, so silence the resulting warnings.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Gson (pulled in transitively by the Pusher client) ----------------------
# Reflective field access breaks if the fields are stripped or renamed.
-dontwarn sun.misc.**
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# --- Pusher ------------------------------------------------------------------
# Currently unreferenced (the realtime code is commented out), so R8 strips it.
# These rules keep the protocol DTOs intact if that code is switched back on.
-keep class com.pusher.client.connection.websocket.** { *; }
-keepclassmembers class com.pusher.client.** { *; }
-dontwarn com.pusher.client.**

# The Pusher client logs through slf4j, which resolves its backend reflectively
# at runtime. We ship no slf4j binding, so the lookup fails over to a no-op --
# this only silences R8's warning about the class it cannot find.
-dontwarn org.slf4j.**

# --- ZXing / journeyapps scanner ---------------------------------------------
# ScanContract resolves the capture activity by class, and the decoder picks
# format handlers reflectively.
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }
-dontwarn com.google.zxing.**

# --- Credential Manager / Google ID sign-in ----------------------------------
# Request and response types cross a Bundle boundary into Play services, which
# looks them up by their original names.
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**

# --- Firebase Cloud Messaging ------------------------------------------------
-keep class com.google.firebase.messaging.** { *; }
