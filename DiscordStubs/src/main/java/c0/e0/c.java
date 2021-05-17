package c0.e0;

import java.util.List;

// kotlin.reflect.KClass
// https://github.com/JetBrains/kotlin-native/blob/master/runtime/src/main/kotlin/kotlin/reflect/KClass.kt
@SuppressWarnings("unused")
public interface c<T> {
    T getObjectInstance();
    String getQualifiedName();
    List<c<? extends T>> getSealedSubclasses();
}
