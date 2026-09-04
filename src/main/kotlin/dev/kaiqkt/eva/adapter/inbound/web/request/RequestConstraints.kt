package dev.kaiqkt.eva.adapter.inbound.web.request

internal object RequestConstraints {
    const val NAME_MAX = 50
    const val SLUG_MAX = 50
    const val DESCRIPTION_MAX = 255
    const val NAME_FORMAT = "^[a-zA-Z0-9 ]+$"
    const val SLUG_FORMAT = "^[a-z0-9]+(?:-[a-z0-9]+)*$"
}
