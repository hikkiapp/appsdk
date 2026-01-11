package hikki.sdk

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ConfigPayloadTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `calculateConfigItemHash produces consistent hash`() {
        val name = "test_name"
        val section = "test_section"
        val hash1 = calculateConfigItemHash(name, section)
        val hash2 = calculateConfigItemHash(name, section)

        assertEquals(hash1, hash2)
        assertNotNull(hash1)
        assertEquals(64, hash1.length) // SHA-256 hex string length
    }

    @Test
    fun `ConfigItem serialization and deserialization`() {
        val item: ConfigItem = ConfigItem.BooleanItem(
            title = "Test",
            name = "test_bool",
            section = "general",
            description = "desc",
            value = true
        )

        val serialized = json.encodeToString(ConfigItem.serializer(), item)
        val deserialized = json.decodeFromString(ConfigItem.serializer(), serialized)

        assertEquals(item, deserialized)
    }

    @Test
    fun `ConfigPayload serialization`() {
        val settings = listOf(
            ConfigItem.StringItem("Title", "name", "section", "desc", "value")
        )
        val payload = ConfigPayload(version = 1, settings = settings)

        val serialized = json.encodeToString(ConfigPayload.serializer(), payload)
        val deserialized = json.decodeFromString(ConfigPayload.serializer(), serialized)

        assertEquals(payload, deserialized)
        assertEquals(1, deserialized.version)
        assertEquals(1, deserialized.settings.size)
    }

    @Test
    fun `EnumItem serialization`() {
        val item = ConfigItem.EnumItem(
            "Title", "name", "section", "desc",
            options = listOf("A", "B"),
            value = "A"
        )
        val serialized = json.encodeToString(ConfigItem.serializer(), item)
        val deserialized = json.decodeFromString(ConfigItem.serializer(), serialized) as ConfigItem.EnumItem

        assertEquals(item.options, deserialized.options)
        assertEquals(item.value, deserialized.value)
    }
}