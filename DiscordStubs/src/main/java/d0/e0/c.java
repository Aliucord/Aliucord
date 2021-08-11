package d0.e0;

import java.util.List;

/**
 * kotlin.reflect.KClass
 * @see <a href="https://github.com/JetBrains/kotlin-native/blob/master/runtime/src/main/kotlin/kotlin/reflect/KClass.kt">KClass on Github</a>
 */
@SuppressWarnings("unused")
public interface c<T> {
    T getObjectInstance();
    String getQualifiedName();
    List<c<? extends T>> getSealedSubclasses();
    String getSimpleName();
}
