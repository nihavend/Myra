#-injars LikyaTlosOrj-1.7.0.jar
#-outjars LikyaTlos-1.7.0.jar

-libraryjars 'D:\dev\Java\jre6\lib\rt.jar'
-libraryjars 'D:\dev\Java\jre6\lib\jce.jar'
-libraryjars ..\..\SharedLibs\log4j\log4j-1.2.15.jar
-libraryjars ..\..\SharedLibs\javamail\mailapi.jar
-libraryjars ..\..\SharedLibs\javamail\smtp.jar
-libraryjars ..\..\SharedLibs\icefaces_2.x\compat\jxl.jar
-libraryjars ..\..\SharedLibs\winp\winp.jar
-libraryjars ..\..\SharedLibs\winp\winp.dll
-libraryjars ..\..\SharedLibs\winp\winp.x64.dll
-libraryjars ..\..\SharedLibs\eXist_1.4\cocoon\commons-lang-2.0-20041007T2305.jar
-libraryjars ..\..\SharedLibs\apache\commons-codec-1.4.jar

-dontshrink
-dontoptimize
-keepattributes *Annotation*
-dontpreverify
-printseeds ProGuard.seeds


-keepclassmembers class * extends java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclasseswithmembers,allowshrinking class com.likya.tlos.sms.helpers.TlosSMSHandler {
    *** sendSMS(...);
    *** getMsisdnList(...);
}

-keep,allowshrinking class com.likya.tlos.model.FlexAdminConsoleMBean {
    <methods>;
}

-keep,allowshrinking class com.likya.tlos.model.TlosInfo

-keep,allowshrinking class com.likya.tlos.model.CommandType

-keep,allowshrinking class com.likya.tlos.model.JobProperties

-keep,allowshrinking class com.likya.tlos.model.FlexAdminConsole

-keep,allowshrinking class com.likya.tlos.jobs.Job

-keep,allowshrinking class com.likya.tlos.utils.DateUtils {
 	<methods>;
}

-keep,allowshrinking class com.likya.tlos.utils.loaders.ScenarioLoader {
	*** JOB_BASE_TYPE_STANDART;
	*** UNDEFINED_VALUE;
	*** SYSTEM_PROCESS;
	*** JAVA_PROCESS;
	*** readScenario(...);
}

-keep,allowshrinking class com.likya.tlos.encryption.LikyaEncryption {
	*** encryptPassword(...);
	*** decryptPassword(...);
}

-keepclasseswithmembers,allowshrinking class com.likya.tlos.lite.model.TlosData {
 	<methods>;
}
-keepclasseswithmembers,allowshrinking class com.likya.tlos.model.TlosParameters {
 	<methods>;
}
-keepclasseswithmembers,allowshrinking class com.likya.tlos.model.TlosAuthorization {
 	<methods>;
}
-keepclasseswithmembers,allowshrinking class com.likya.tlos.LocaleMessages {
	*** getString(...);
}
-keepclasseswithmembers,allowshrinking class com.likya.tlos.TlosServer {
	*** isEXTERNSmsPermit(...);
}

# Keep - Applications. Keep all application classes, along with their 'main'
# methods.
-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

# Keep names - _class method names. Keep all .class method names. This may be
# useful for libraries that will be obfuscated again with different obfuscators.
-keepclassmembers,allowshrinking class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String,boolean);
}
