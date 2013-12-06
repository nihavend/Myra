-injars AvtGgmDataManagerORJ.jar
-outjars AvtGgmDataManager.jar

-libraryjars 'C:\Program Files\Java\jre6\lib\rt.jar'
-libraryjars cp\axis.jar
-libraryjars cp\jaxrpc.jar

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
