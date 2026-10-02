package kz.airbapay.apay_android.data.utils

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kz.airbapay.apay_android.AirbaPaySdk

internal object DataHolder {
  const val sdkVersion: String = "2.1.3"
  var baseUrl = ""

  var connectTimeout = 60
  var receiveTimeout = 60
  var sendTimeout = 60

  var isProd = true
  var enabledLogsForProd = false

  var purchaseAmount: Double = 0.0
  var orderNumber: String? = null
  var invoiceId = ""

  var bankCode: String? = null
  var token: String? = null

  var userEmail: String? = null
  var userPhone = ""
  var accountId: String = ""

  var failureBackUrl = "https://site.kz/failure"
  var successBackUrl = "https://site.kz/success"

  var currentLang = AirbaPaySdk.Lang.RU.lang

  val purchaseAmountFormatted = MutableStateFlow("")
  val purchaseNumber = MutableStateFlow("")

  var actionOnCloseProcessing: ((activity: Activity, paymentSubmittingResult: Boolean) -> Unit)? = null
  var shouldOverrideUrlLoading: ((obj: AirbaPaySdk.ShouldOverrideUrlLoading) -> Boolean)? = null

  var openCustomPageSuccess: ((activity: Activity) -> Unit)? = null
  var openCustomPageFinalError: ((activity: Activity) -> Unit)? = null

  var gateway: String? = null
  var gatewayMerchantId: String? = null
  var googlePayButtonUrl: String? = null

  var isGooglePayFlow: Boolean = true
  var hasSavedCards: Boolean = false

  var needDisableScreenShot: Boolean = false

  var isGooglePayNative: Boolean = true

  var renderGooglePay: Boolean? = null
  var renderSavedCards: Boolean? = null
  var renderSecurityCvv: Boolean? = null
  var renderSecurityBiometry: Boolean? = null

  var height: Int? = null
  var width: Int? = null

  fun isRenderGooglePay() = renderGooglePay ?: true
  fun isRenderSavedCards() = renderSavedCards ?: true
  fun isRenderSecurityCvv() = renderSecurityCvv ?: true
  fun isRenderSecurityBiometry() = renderSecurityBiometry ?: true
}
