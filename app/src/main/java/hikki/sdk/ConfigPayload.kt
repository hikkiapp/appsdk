package hikki.sdk

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import java.security.MessageDigest

@Serializable
data class ConfigPayload(val version: Int, val settings: List<ConfigItem>)

object ConfigActions {
    const val GET = "app.hikki.api.GET_CONFIG"
    const val SET = "app.hikki.api.SET_CONFIG"
}

fun calculateConfigItemHash(name: String, section: String): String {
    val data = "$name:$section"
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val bytes = messageDigest.digest(data.toByteArray())
    return bytes.fold("") { str, it -> str + "%02x".format(it) }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface ConfigItem {
    val title: String
    val name: String
    val section: String
    val description: String?
    val hash: String?

    @Serializable @SerialName("boolean")
    data class BooleanItem(
        override val title: String,
        override val name: String,
        override val section: String,
        override val description: String?,
        var value: Boolean,
        override val hash: String? = calculateConfigItemHash(name, section)
    ) : ConfigItem

    @Serializable @SerialName("string")
    data class StringItem(
        override val title: String,
        override val name: String,
        override val section: String,
        override val description: String?,
        var value: String,
        override val hash: String? = calculateConfigItemHash(name, section)
    ) : ConfigItem

    @Serializable @SerialName("int")
    data class IntItem(
        override val title: String,
        override val name: String,
        override val section: String,
        override val description: String?,
        var value: Int,
        override val hash: String? = calculateConfigItemHash(name, section)
    ) : ConfigItem

    @Serializable @SerialName("float")
    data class FloatItem(
        override val title: String,
        override val name: String,
        override val section: String,
        override val description: String?,
        var value: Float,
        override val hash: String? = calculateConfigItemHash(name, section)
    ) : ConfigItem

    @Serializable @SerialName("enum")
    data class EnumItem(
        override val title: String,
        override val name: String,
        override val section: String,
        override val description: String?,
        val options: List<String>,
        var value: String,
        override val hash: String? = calculateConfigItemHash(name, section)
    ) : ConfigItem

    @Serializable @SerialName("date")
    data class DateItem(
        override val title: String,
        override val name: String,
        override val section: String,
        override val description: String?,
        var value: Long,
        override val hash: String? = calculateConfigItemHash(name, section)
    ) : ConfigItem

    @Serializable @SerialName("time")
    data class TimeItem(
        override val title: String,
        override val name: String,
        override val section: String,
        override val description: String?,
        var value: Long,
        override val hash: String? = calculateConfigItemHash(name, section)
    ) : ConfigItem

    @Serializable
    @SerialName("section")
    data class SectionItem(
        override val title: String,
        override val name: String,
        override val section: String,
        override val description: String?,
        override val hash: String? = calculateConfigItemHash(name, section)
    ) : ConfigItem

    @Serializable
    @SerialName("button")
    data class ButtonItem(
        override val title: String,
        override val name: String,
        override val section: String,
        override val description: String?,
        override val hash: String? = calculateConfigItemHash(name, section)
    ) : ConfigItem
}