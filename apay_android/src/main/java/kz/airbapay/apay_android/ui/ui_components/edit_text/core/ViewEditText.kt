package kz.airbapay.apay_android.ui.ui_components.edit_text.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import kz.airbapay.apay_android.R
import kz.airbapay.apay_android.ui.resources.ColorsSdk
import kz.airbapay.apay_android.ui.resources.LocalFonts
import kz.airbapay.apay_android.ui.ui_components.InitActionIcon

@Composable
internal fun ViewEditText(
    actionOnTextChanged: (String) -> Unit,
    actionClickInfo: (() -> Unit)? = null,
    actionClickScanCard: (() -> Unit)? = null,
    text: MutableState<TextFieldValue>,
    errorTitle: MutableState<String?>,
    placeholder: String,
    mask: String? = null,
    regex: Regex? = null,
    focusRequester: FocusRequester,
    modifierRoot: Modifier = Modifier,
    modifierChild: Modifier = Modifier,
    paySystemIcon: MutableState<Int?> = mutableStateOf(null),
    keyboardActions: KeyboardActions,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        capitalization = KeyboardCapitalization.None,
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    ),
    visualTransformation: VisualTransformation? = null,
    isDateExpiredMask: Boolean = false,
    isCardNumberMask: Boolean = false
) {

    val hasFocus = remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifierRoot
    ) {

        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = 0.dp,
            border = BorderStroke(
                0.1.dp,
                if (errorTitle.value != null) ColorsSdk.stateError else ColorsSdk.gray20
            ),
            modifier = modifierChild
                .wrapContentHeight()
                .heightIn(min = 48.dp),
            onClick = {}
        ) {
            ConstraintLayout {
                val (clearIconRef) = createRefs()

                CoreEditText(
                    isError = errorTitle.value != null,
                    mask = mask,
                    regex = regex,
                    placeholder = placeholder,
                    keyboardActions = keyboardActions,
                    keyboardOptions = keyboardOptions,
                    hasFocus = hasFocus,
                    text = text,
                    focusRequester = focusRequester,
                    actionOnTextChanged = actionOnTextChanged,
                    visualTransformation = visualTransformation,
                    isDateExpiredMask = isDateExpiredMask,
                    actionClickInfo = actionClickInfo,
                    paySystemIcon = paySystemIcon.value
                )

                InitIconEnd(
                    clearIconRef = clearIconRef,
                    hasFocus = hasFocus.value,
                    text = text.value.text,
                    isError = errorTitle.value != null,
                    isCardNumberMask = isCardNumberMask,
                    actionClickClear = {
                        text.value = TextFieldValue(
                            text = "",
                            selection = TextRange(0)
                        )
                        paySystemIcon.value = null
                    },
                    actionClickScanCard = actionClickScanCard
                )
            }
        }

        if (errorTitle.value != null) {
            Row {
                Icon(
                    painter = painterResource(R.drawable.alarm),
                    contentDescription = "alarm",
                    tint = ColorsSdk.stateError,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    style = LocalFonts.current.caption400,
                    text = errorTitle.value!!,
                    color = ColorsSdk.stateError,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .padding(start = 6.dp)
                )
            }

        }
    }
}

@Composable
private fun ConstraintLayoutScope.InitIconEnd(
    isError: Boolean,
    isCardNumberMask: Boolean,
    text: String,
    hasFocus: Boolean,
    actionClickClear: () -> Unit,
    actionClickScanCard: (() -> Unit)?,
    clearIconRef: ConstrainedLayoutReference
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(40.dp)
            .padding(horizontal = 10.dp)
            .constrainAs(clearIconRef) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }

    ) {
        if (
            text.isNotBlank()
            && hasFocus
        ) {
            InitActionIcon(
                action = actionClickClear,
                iconSrc = R.drawable.ic_close,
                modifier = Modifier,
                _outlinedButtonColor = if (isError) ColorsSdk.stateBgError else ColorsSdk.bgBlock
            )

        }

        if (isCardNumberMask) {
            InitActionIcon(
                action = { actionClickScanCard?.invoke() },
                iconSrc = R.drawable.ic_card_scanner,
                modifier = Modifier.padding(start = 10.dp),
                _outlinedButtonColor = if (isError) ColorsSdk.stateBgError else ColorsSdk.bgBlock
            )
        }
    }
}