package hikki.sdk

import java.util.*


const val CURRENT_VERSION = 1

fun String.localized(
    ru: String? = null,
    be: String? = null
): String {
    return when (Locale.getDefault().language) {
        "ru" -> ru ?: this
        "be" -> be ?: this
        // for future languages
        else -> this
    }
}

fun getDefaultSettings(): List<ConfigItem> {
    return listOf(
        ConfigItem.SectionItem(
            title = "General".localized(
                ru = "Общие",
                be = "Агульныя"
            ),
            name = "general",
            section = "general",
            description = "General settings".localized(
                ru = "Общие настройки",
                be = "Агульныя налады"
            )
        ),
        ConfigItem.BooleanItem(
            title = "Enable subscription".localized(
                ru = "Включить подписку",
                be = "Уключыць падпіску"
            ),
            name = "enable_subscription",
            section = "general",
            description = "Enable or disable the subscription feature".localized(
                ru = "Включить или отключить функцию подписки",
                be = "Уключыць або адключыць функцыю падпіскі"
            ),
            value = false
        ),
        ConfigItem.DateItem(
            title = "Subscription expire date".localized(
                ru = "Дата окончания подписки",
                be = "Дата заканчэння падпіскі"
            ),
            name = "subscription_expire_date",
            section = "general",
            description = "Select a date".localized(
                ru = "Выберите дату",
                be = "Абярыце дату"
            ),
            value = -1L
        ),
        ConfigItem.SectionItem(
            title = "Proxy".localized(
                ru = "Прокси",
                be = "Проксі"
            ),
            name = "proxy",
            section = "proxy",
            description = "Proxy settings".localized(
                ru = "Настройки прокси",
                be = "Налады проксі"
            )
        ),
        ConfigItem.EnumItem(
            title = "Proxy type".localized(
                ru = "Тип прокси",
                be = "Тып проксі"
            ),
            name = "proxy_type",
            section = "proxy",
            description = "Select a proxy type".localized(
                ru = "Выберите тип прокси",
                be = "Абярыце тып проксі"
            ),
            options = listOf("HTTP", "HTTPS", "SOCKS"),
            value = "HTTP"
        ),
        ConfigItem.StringItem(
            title = "Proxy host".localized(
                ru = "Хост прокси",
                be = "Хост проксі"
            ),
            name = "proxy_host",
            section = "proxy",
            description = "The proxy host".localized(
                ru = "Хост прокси",
                be = "Хост проксі"
            ),
            value = ""
        ),
        ConfigItem.IntItem(
            title = "Proxy port".localized(
                ru = "Порт прокси",
                be = "Порт проксі"
            ),
            name = "proxy_port",
            section = "proxy",
            description = "The proxy port".localized(
                ru = "Порт прокси",
                be = "Порт проксі"
            ),
            value = 8080
        ),
        ConfigItem.StringItem(
            title = "Proxy user".localized(
                ru = "Пользователь прокси",
                be = "Карыстальнік проксі"
            ),
            name = "proxy_user",
            section = "proxy",
            description = "The proxy user (not supported for SOCKS)".localized(
                ru = "Пользователь прокси (не поддерживается для SOCKS)",
                be = "Карыстальнік проксі (не падтрымліваецца для SOCKS)"
            ),
            value = ""
        ),
        ConfigItem.StringItem(
            title = "Proxy password".localized(
                ru = "Пароль прокси",
                be = "Пароль проксі"
            ),
            name = "proxy_password",
            section = "proxy",
            description = "The proxy password (not supported for SOCKS)".localized(
                ru = "Пароль прокси (не поддерживается для SOCKS)",
                be = "Пароль проксі (не падтрымліваецца для SOCKS)"
            ),
            value = ""
        ),
        ConfigItem.ButtonItem(
            title = "Apply proxy".localized(
                ru = "Применить прокси",
                be = "Прымяніць проксі"
            ),
            name = "apply_proxy",
            section = "proxy",
            description = "Apply the proxy settings".localized(
                ru = "Применить настройки прокси",
                be = "Прымяніць налады проксі"
            )
        ),
        ConfigItem.ButtonItem(
            title = "Reset proxy".localized(
                ru = "Сбросить прокси",
                be = "Скінуць проксі"
            ),
            name = "reset_proxy",
            section = "proxy",
            description = "Reset the proxy settings".localized(
                ru = "Сбросить настройки прокси",
                be = "Скінуць налады проксі"
            )
        )
    )
}