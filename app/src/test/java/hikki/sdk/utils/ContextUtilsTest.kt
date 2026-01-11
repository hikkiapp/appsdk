package hikki.sdk.utils

import org.junit.Assert.assertNull
import org.junit.Test

class ContextUtilsTest {
    @Test
    fun `getApplicationContextViaReflection returns null in local unit test environment`() {
        val result = ContextUtils.getApplicationContextViaReflection()
        assertNull(result)
    }
}