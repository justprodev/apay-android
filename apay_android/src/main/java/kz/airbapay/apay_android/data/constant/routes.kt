package kz.airbapay.apay_android.data.constant

internal const val routesErrorSomethingWrong = "/error_something_wrong"

internal const val ARG_ERROR_CODE = "errorCode"
internal const val ARG_ACTION = "action"
internal const val ARG_IS_RETRY = "is_retry"

internal const val ROUTES_HOME = "/home_page"
internal const val ROUTES_SUCCESS = "/success"
internal const val ROUTES_REPEAT = "/repeat"
internal const val ROUTES_ERROR = "/error"
internal const val ROUTES_ERROR_WITH_INSTRUCTION = "/error_with_instruction"
internal const val ROUTES_ERROR_FINAL = "/error_final"

internal const val TEMPLATE_DEEP_LINK_WEB_VIEW = "/web_view_page"
internal const val ROUTES_WEB_VIEW = "/web_view_page?$ARG_IS_RETRY={$ARG_IS_RETRY}?$ARG_ACTION={$ARG_ACTION}"



