package kz.airbapay.apay_android.ui.pages.home.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kz.airbapay.apay_android.data.constant.RegexConst.NOT_DIGITS
import kz.airbapay.apay_android.data.constant.cardNumber
import kz.airbapay.apay_android.data.utils.card_utils.getCardTypeFromNumber
import kz.airbapay.apay_android.ui.ui_components.edit_text.core.ViewEditText

@Composable
internal fun CardNumberView(
    cardNumberText: MutableState<TextFieldValue>,
    cardNumberError: MutableState<String?>,
    cardNumberFocusRequester: FocusRequester,
    dateExpiredFocusRequester: FocusRequester,
    actionClickScanCard: () -> Unit,
    paySystemIcon: MutableState<Int?>

) {

    ViewEditText(
        mask = "AAAA AAAA AAAA AAAA",
        text = cardNumberText,
        regex = Regex(NOT_DIGITS),
        paySystemIcon = paySystemIcon,
        errorTitle = cardNumberError,
        focusRequester = cardNumberFocusRequester,
        placeholder = cardNumber(),
        keyboardActions = KeyboardActions(
            onNext = {
                dateExpiredFocusRequester.requestFocus()
            }
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        modifierRoot = Modifier.padding(horizontal = 16.dp),
        actionOnTextChanged = { pan ->
            paySystemIcon.value = getCardTypeFromNumber(pan).icon
        },
        actionClickScanCard = actionClickScanCard,
        isCardNumberMask = true
    )

}