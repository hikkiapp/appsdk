package hikki.sdk.utils

import android.util.Base64
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.Assert.assertEquals
import org.junit.Test

class EncryptorTest {
    @Test
    fun `encode and decode returns original string`() {
        val original = "Hello World! 123"
        val encoded = Encryptor.encode(original)
        val decoded = Encryptor.decode(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `encode and decode with non-ASCII characters`() {
        val original = "Привет, мир! 🌍"
        val encoded = Encryptor.encode(original)
        val decoded = Encryptor.decode(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `decode with empty data returns empty string`() {
        assertEquals("", Encryptor.decode(byteArrayOf()))
    }

    @Test
    fun `encodeToBase64 and decodeFromBase64 integration`() {
        mockkStatic(Base64::class)

        val original = "SecretMessage"

        every { Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(it.invocation.args[0] as ByteArray)
        }

        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getDecoder().decode(it.invocation.args[0] as String)
        }

        val base64 = Encryptor.encodeToBase64(original)
        val decoded = Encryptor.decodeFromBase64(base64)

        assertEquals(original, decoded)

        unmockkAll()
    }
}