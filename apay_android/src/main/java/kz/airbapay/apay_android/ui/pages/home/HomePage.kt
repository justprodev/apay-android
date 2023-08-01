package kz.airbapay.apay_android.ui.pages.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kz.airbapay.apay_android.R
import kz.airbapay.apay_android.data.constant.paymentOfPurchase
import kz.airbapay.apay_android.data.constant.saveCardData
import kz.airbapay.apay_android.data.constant.sendCheckToEmail
import kz.airbapay.apay_android.ui.pages.dialog.InitDialogExit
import kz.airbapay.apay_android.ui.pages.home.presentation.BottomImages
import kz.airbapay.apay_android.ui.pages.home.presentation.CardNumberView
import kz.airbapay.apay_android.ui.pages.home.presentation.ConfirmButton
import kz.airbapay.apay_android.ui.pages.home.presentation.CvvView
import kz.airbapay.apay_android.ui.pages.home.presentation.DateExpiredView
import kz.airbapay.apay_android.ui.pages.home.presentation.EmailView
import kz.airbapay.apay_android.ui.pages.home.presentation.NameHolderView
import kz.airbapay.apay_android.ui.pages.home.presentation.SwitchedView
import kz.airbapay.apay_android.ui.pages.home.presentation.TopInfoView
import kz.airbapay.apay_android.ui.resources.ColorsSdk
import kz.airbapay.apay_android.ui.ui_components.BackHandler
import kz.airbapay.apay_android.ui.ui_components.ViewToolbar

@Composable
internal fun HomePage(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    scrollState: ScrollState = rememberScrollState(),

) {
    val context = LocalContext.current
    val showDialogExit = remember { mutableStateOf(false) }
    val switchSaveCard = remember { mutableStateOf(false) }
    val switchSendToEmail = remember { mutableStateOf(false) }

    val cardNumberFocusRequester = FocusRequester()
    val nameHolderFocusRequester = FocusRequester()
    val dateExpiredFocusRequester = FocusRequester()
    val cvvFocusRequester = FocusRequester()
    val emailFocusRequester = FocusRequester()

    val cardNumberText = remember { mutableStateOf("") }
    val nameHolderText = remember { mutableStateOf("") }
    val dateExpiredText = remember { mutableStateOf("") }
    val cvvText = remember { mutableStateOf("") }
    val emailText = remember { mutableStateOf("") }

    val cardNumberError = remember { mutableStateOf<String?>(null) }
    val nameHolderError = remember { mutableStateOf<String?>(null) }
    val dateExpiredError = remember { mutableStateOf<String?>(null) }
    val cvvError = remember { mutableStateOf<String?>(null) }
    val emailError = remember { mutableStateOf<String?>(null) }

    val paySystemIcon = remember { mutableStateOf<Int?>(null) }

    BackHandler {
        showDialogExit.value = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorsSdk.bgBlock)
    ) {
        ViewToolbar(
            title = paymentOfPurchase(),
            backIcon = R.drawable.ic_arrow_back,
            actionBack = {
                showDialogExit.value = true
            }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(ColorsSdk.bgBlock)
                .weight(1f)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Image(
                painter = painterResource(R.drawable.top_info),
                contentDescription = "top_info",
                modifier = Modifier
                    .padding(horizontal = 46.dp)
                    .height(32.dp)
            )

            TopInfoView()

            Spacer(modifier = Modifier.height(16.dp))
            CardNumberView(
                cardNumberText = cardNumberText,
                paySystemIcon = paySystemIcon,
                cardNumberError = cardNumberError,
                cardNumberFocusRequester = cardNumberFocusRequester,
                nameHolderFocusRequester = nameHolderFocusRequester
            )

            Spacer(modifier = Modifier.height(16.dp))
            NameHolderView(
                nameHolderText = nameHolderText,
                nameHolderError = nameHolderError,
                nameHolderFocusRequester = nameHolderFocusRequester,
                dateExpiredFocusRequester = dateExpiredFocusRequester
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                DateExpiredView(
                    dateExpiredFocusRequester = dateExpiredFocusRequester,
                    dateExpiredError = dateExpiredError,
                    dateExpiredText = dateExpiredText,
                    cvvFocusRequester = cvvFocusRequester,
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(end = 6.dp)
                )
                CvvView(
                    cvvError = cvvError,
                    cvvFocusRequester = cvvFocusRequester,
                    cvvText = cvvText,
                    emailFocusRequester = if(switchSendToEmail.value) emailFocusRequester else null,
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            SwitchedView(
                text = saveCardData(),
                switchCheckedState = switchSaveCard,
                actionOnTrue = { },
                actionOnFalse = { }
            )

            SwitchedView(
                text = sendCheckToEmail(),
                switchCheckedState = switchSendToEmail,
                actionOnTrue = { },
                actionOnFalse = { }
            )

            if (switchSendToEmail.value) {
                EmailView(
                    emailText = emailText,
                    emailError = emailError,
                    emailFocusRequester = emailFocusRequester
                )
            }

            BottomImages()
        }

        ConfirmButton("13 500")//todo

        if (showDialogExit.value) {
            InitDialogExit(
                onDismissRequest = {
                    showDialogExit.value = false
                }
            )
        }
    }
}


