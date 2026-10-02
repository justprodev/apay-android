package kz.airbapay.apay_android.ui.pages.home.presentation

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
import kz.airbapay.apay_android.data.constant.RegexConst
import kz.airbapay.apay_android.data.constant.dateExpired
import kz.airbapay.apay_android.ui.ui_components.edit_text.core.ViewEditText

@Composable
internal fun DateExpiredView(
    dateExpiredText: MutableState<TextFieldValue>,
    dateExpiredError: MutableState<String?>,
    dateExpiredFocusRequester: FocusRequester,
    cvvFocusRequester: FocusRequester,
    modifier: Modifier
) {
    ViewEditText(
        text = dateExpiredText,
        mask = "AA/AA",
        isDateExpiredMask = true,
        regex = Regex(RegexConst.NOT_DIGITS),
        errorTitle = dateExpiredError,
        focusRequester = dateExpiredFocusRequester,
        placeholder = dateExpired(),
        keyboardActions = KeyboardActions(
            onNext = {
                cvvFocusRequester.requestFocus()
            }
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        modifierRoot = modifier,
        actionOnTextChanged = {}
    )
}