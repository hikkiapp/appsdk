package hikki.sdk.ghost

import android.util.Log
import java.lang.reflect.Field

internal object ReflectionHelper {
    private const val TAG = "GhostFramework"

    fun findField(clazz: Class<*>, fieldName: String): Field {
        Log.d(TAG, "findField() called for class=${clazz.name}, field=$fieldName")
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            try {
                val field = currentClass.getDeclaredField(fieldName)
                field.isAccessible = true
                Log.d(TAG, "Found field $fieldName in ${currentClass.name}")
                return field
            } catch (e: NoSuchFieldException) {
                currentClass = currentClass.superclass
            }
        }
        Log.e(TAG, "Field $fieldName not found in ${clazz.name}")
        throw NoSuchFieldException("Field $fieldName not found in ${clazz.name}")
    }
}