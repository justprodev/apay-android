package kz.airbapay.apay_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import kz.airbapay.apay_android.data.constant.ARG_ACTION
import kz.airbapay.apay_android.data.constant.ARG_ERROR_CODE
import kz.airbapay.apay_android.data.constant.ARG_IS_RETRY
import kz.airbapay.apay_android.data.constant.ROUTES_ERROR
import kz.airbapay.apay_android.data.constant.ROUTES_ERROR_FINAL
import kz.airbapay.apay_android.data.constant.ROUTES_ERROR_WITH_INSTRUCTION
import kz.airbapay.apay_android.data.constant.ROUTES_HOME
import kz.airbapay.apay_android.data.constant.ROUTES_REPEAT
import kz.airbapay.apay_android.data.constant.ROUTES_SUCCESS
import kz.airbapay.apay_android.data.constant.ROUTES_WEB_VIEW
import kz.airbapay.apay_android.data.constant.initErrorsCodeByCode
import kz.airbapay.apay_android.data.constant.routesErrorSomethingWrong
import kz.airbapay.apay_android.data.utils.DataHolder
import kz.airbapay.apay_android.network.api.Api
import kz.airbapay.apay_android.network.base.ClientConnector
import kz.airbapay.apay_android.network.repository.AuthRepository
import kz.airbapay.apay_android.network.repository.CardRepository
import kz.airbapay.apay_android.network.repository.PaymentsRepository
import kz.airbapay.apay_android.ui.pages.error.ErrorFinalPage
import kz.airbapay.apay_android.ui.pages.error.ErrorPage
import kz.airbapay.apay_android.ui.pages.error.ErrorSomethingWrongPage
import kz.airbapay.apay_android.ui.pages.error.ErrorWithInstructionPage
import kz.airbapay.apay_android.ui.pages.error.RepeatPage
import kz.airbapay.apay_android.ui.pages.home.HomePage
import kz.airbapay.apay_android.ui.pages.success.SuccessPage
import kz.airbapay.apay_android.ui.pages.webview.WebViewPage

class AirbaPayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val clientConnector = ClientConnector(this)
        val api = clientConnector.retrofit.create(Api::class.java)

        val authRepository = AuthRepository(api)
        val cardRepository = CardRepository(api)
        val paymentsRepository = PaymentsRepository(api)

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = ROUTES_HOME
            ) {
                composable(ROUTES_HOME) {
                    HomePage(
                        navController = navController,
                        authRepository = authRepository,
                        paymentsRepository = paymentsRepository
                    )
                }

                composable(
                    route = ROUTES_ERROR,
                    arguments = listOf(
                        navArgument(ARG_ERROR_CODE) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    ErrorPage(
                        errorCode = initErrorsCodeByCode(
                            (backStackEntry.arguments?.getString(ARG_ERROR_CODE) ?: "1").toInt()
                        ),
                        navController = navController
                    )
                }

                composable(ROUTES_ERROR_FINAL) {
                    ErrorFinalPage()
                }

                composable(routesErrorSomethingWrong) {
                    ErrorSomethingWrongPage()
                }

                composable(
                    route = ROUTES_ERROR_WITH_INSTRUCTION,
                    deepLinks = listOf(
                        navDeepLink { uriPattern = ROUTES_ERROR_WITH_INSTRUCTION }
                    ),
                    arguments = listOf(
                        navArgument(ARG_ERROR_CODE) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    ErrorWithInstructionPage(
                        errorCode = initErrorsCodeByCode(
                            (backStackEntry.arguments?.getString(ARG_ERROR_CODE) ?: "1").toInt()
                        ),
                        navController = navController
                    )
                }

                composable(ROUTES_REPEAT) {
                    RepeatPage(
                        navController = navController,
                        paymentsRepository = paymentsRepository
                    )
                }

                composable(
                    route = ROUTES_WEB_VIEW,
                    deepLinks = listOf(
                        navDeepLink { uriPattern = ROUTES_WEB_VIEW }
                    ),
                    arguments = listOf(
                        navArgument(ARG_ACTION) {
                            type = NavType.StringType
                        },
                        navArgument(ARG_IS_RETRY) {
                            type = NavType.BoolType
                        }
                    )
                ) { backStackEntry ->
                    WebViewPage(
                        url = backStackEntry.arguments?.getString(ARG_ACTION).orEmpty(),
                        isRetry = backStackEntry.arguments?.getBoolean(ARG_IS_RETRY) ?: false,
                        navController = navController
                    )
                }

                composable(ROUTES_SUCCESS) {
                    if (DataHolder.needShowSdkSuccessPage) {
                        SuccessPage()
                    } else {
                        this@AirbaPayActivity.finish()
                    }
                }
            }

        }
    }
}

