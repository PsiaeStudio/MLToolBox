#
# keep JNA class and interface
#
-keep class com.sun.jna.** {*;}
-keep class * implements com.sun.jna.** {*;}

#
# ProGuard 7.5.0, "java.lang.VerifyError: Bad type on operand stack"
#
-keepclassmembers public class **$$serializer {
    private ** descriptor;
}
-keep class kotlinx.serialization.** {*;}
-keep class androidx.compose.runtime.** { *; }
-keep class kotlin.** {*;}
-keep class kotlinx.** {*;}
-keep class java.** {*;}

#keep runtime annotation for JNA
-keepattributes RuntimeVisibleAnnotations

-printseeds obfuscation/seeds.txt
-printmapping obfuscation/mapping.txt
