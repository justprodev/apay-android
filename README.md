## Техническая документация для интеграции sdk AirbaPay в мобильные приложения

------------------------------------------------------------------------------------------------------------------------
## V2
------------------------------------------------------------------------------------------------------------------------

## 1 Подключение sdk

## 2 Настройки PROGUARD

## 3 Flutter - дополнительные шаги интеграции

## 4 Подключение нативного GooglePay

## 5 API создания платежа

## 6 API формы стандартного флоу

## 7 API GooglePay

## 8 API сохраненных карт



## 1 Подключение sdk


В  ```build.gradle ``` модуля добавь ``` implementation 'kz.airbapay:apay_android:~'```

Для инициализации sdk нужно выполнить ```AirbaPaySdk.initSdk()```

| Параметр                  | Тип                                | Обязательный | Описание                                                                                                                                              |
|---------------------------|------------------------------------|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| context                   | Context                            | да           | Контекст приложения                                                                                                                                   |
| isProd                    | Boolean                            | да           | Продовская или тестовая среда airbapay                                                                                                                |
| lang                      | AirbaPaySdk.Lang                   | да           | Код языка для UI                                                                                                                                      |
| phone                     | String                             | да           | Телефон пользователя                                                                                                                                  |
| userEmail                 | String                             | нет          | Емейл пользователя, куда будет отправлена квитанция. В случае отсутствия емейла                                                                       |
| colorBrandMain            | androidx.compose.ui.graphics.Color | нет          | Брендовый цвет кнопок, переключателей и текста                                                                                                        |
| colorBrandInversion       | androidx.compose.ui.graphics.Color | нет          | Цвет текста у кнопок с брендовым цветом                                                                                                               |
| enabledLogsForProd        | Boolean                            | нет          | Флаг для включения логов                                                                                                                              |
| needDisableScreenShot     | Boolean                            | нет          | Флаг включения/отключения защиты от скриншота страниц. По дефолту выключен                                                                            |
| actionOnCloseProcessing   | (Activity, Boolean) -> Unit        | да           | Лямбда, вызываемая при клике на кнопку "Вернуться в магазин" и при отмене процесса. Разработчику в ней нужно прописать код для возврата в приложение. |
| openCustomPageSuccess     | ((Activity) -> Unit)?              | нет          | Лямбда кастомной страницы успеха                                                                                                                      |
| openCustomPageFinalError  | ((Activity) -> Unit)?              | нет          | Лямбда кастомной страницы финальной ошибки                                                                                                            |


При смене значения isProd, требуется выгрузить приложение из памяти.

Пример:

```
    
AirbaPaySdk.initSdk(
    context = this.application,
    isProd = false, 
    lang = AirbaPaySdk.Lang.RU,
    phone = "77051000000",
    userEmail = "test@test.com", 
    colorBrandMain = Color.Red,
    actionOnCloseProcessing = { activity, isSuccess ->

         if (isSuccess) { 
                  startActivity(Intent(activity, SuccessActivity::class.java)) 
         } else {
                  startActivity(Intent(activity, ErrorActivity::class.java)) 
         }
         activity.finish()
    }
)

```

Для проекта без Jetpack Compose потребуются дополнительные настройки проекта.

Добавить в build.gradle app и, в случае модульной архитектуры, в модуль, где будет использоваться:

 ```
android {
 
    buildFeatures {
       compose true
       viewBinding true
    }
    
    composeOptions {
       kotlinCompilerExtensionVersion '1.5.9'
    }
    
    kotlinOptions {
      jvmTarget = 17
    }
}
 ```

В зависимости от версии котлина, потребуется подобрать версию kotlinCompilerExtensionVersion
https://developer.android.com/jetpack/androidx/releases/compose-kotlin

 ``` 

dependencies {

    implementation "kz.airbapay:apay_android:$apay_version"
    implementation platform("androidx.compose:compose-bom:2024.01.00")
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui'

}
 ```

## 2 Настройки PROGUARD

В случае, если используется Proguard, нужно добавить настройки

```
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response
```

## 3 Flutter - дополнительные шаги интеграции

В dart добавьте:

   ```
     final MethodChannel channel = MethodChannel("com.example.testFlutter/AirbaPayChannel");
   
     Future<void> callNativeMethod() async {
       try {
         await channel.invokeMethod('pay');
       } catch (e) {
         print('Error calling native method: $e');
       }
     }
   ```

И нужно вызвать ```callNativeMethod``` для перехода на страницы сдк.

Нужно создать дополнительный промежуточный активити FlutterAirbaPayActivity,  
в котором будет производиться инициализация и переход на страницу сдк.
Это нужно, чтоб при нажатии назад из сдк, не закрывалось приложение.

Добавить в манифест описание этого активити

 ```
<activity android:name=".pay.airba.FlutterAirbaPayActivity"
    android:theme="@style/AppTheme.AppCompat"
    android:exported="false" />
 ```

В ```MainActivity``` нужно добавить

```
 override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.example.testFlutter/AirbaPayChannel")
            .setMethodCallHandler { call: MethodCall, result: Result ->
                if (call.method == "pay") {
                    result.success(null)
                    FlutterAirbaPayActivity.start(this, call, result)
                } else {
                    result.notImplemented()
                }
            }
    }
```

В ```FlutterAirbaPayActivity```

```

class FlutterAirbaPayActivity : AppCompatActivity() {
    /**
     * Два состояния для activity:
     *
     * 1. started airba pay flow
     * 2. finished airba pay flow
     */
    private var isFlowStarted = false

    companion object {
        fun start(context: Activity, call: MethodCall, result: MethodChannel.Result) {
            val intent = Intent(context, FlutterAirbaPayActivity::class.java)
            context.startActivityForResult(intent, 123)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        AirbaPaySdk.initSdk(
            ~~~,
            actionOnCloseProcessing = { activity, paymentSubmittingResult ->
               close(paymentSubmittingResult, activity)
            }
        )

        AirbaPaySdk.standardFlow(this)
    }

    /**
     * Finishes this activity with setting result
     *
     * @see handleAirbaActivityResult
     */
    private fun close(result: Boolean, childActivity: Activity? = null) {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra("result", result)
        )
        finish()
        childActivity?.finish()
    }

    override fun onStart() {
        super.onStart()
        if(!isFlowStarted) {
            // 1. started airba pay flow (invoked after onCreate)
            isFlowStarted = true
        } else {
            // 2. finished airba pay flow (invoked after backButton pressed or etc)
            close(false)
        }
    }
}
```

## 4 Подключение нативного GooglePay

1) Добавить в ```standardFlow``` параметр ```isGooglePayNative: true```

2) Перейти в консоль GooglePay  https://pay.google.com/business/console/ и перейти в пункт меню
   Google Pay API

3) Найти в списке “Integrate with your Android app” ваше приложение и кликнуть “Управление”

4) Выберите тип интеграции “Через шлюз”.

5) Загрузите скриншоты по списку указанному ниже и нажмите “Сохранить”

6) Кликните “Submit for approval”

7) В случае отказа по каким-либо причинам (к примеру, дизайн кнопки не соответствует их
   требованиям), нужно будет ответить на письмо гугла, что вы используете стороннее решение компании
   Airba Pay

   
## 5 API создания платежа

Запрос на авторизацию в системе AirbaPay через передачу логина, пароля и айди терминала. Возвращает токен.
```authPassword()```

| Параметр   | Тип              | Обязательный | Описание                                                                                                              |
|------------|------------------|--------------|-----------------------------------------------------------------------------------------------------------------------|
| terminalId | String           | да           | ID терминала под которым создаётся платеж                                                                             |
| shopId     | String           | да           | ID магазина в системе AirbaPay                                                                                        |
| password   | String           | да           | Пароль в системе AirbaPay                                                                                             |
| paymentId  | String?          | нет          | ID платежа. В абсолютном большинстве случаев в этом запросе будет null. Нужен для обновления токена с новым paymentId |
| onSuccess  | (String) -> Unit | да           | Лямбда на успех. Возвращает токен                                                                                     |
| onError    | () -> Unit       | да           | Лямбда на ошибку                                                                                                      |


```
AirbaPaySdk.authPassword(
        onSuccess = { token -> ~  },
        onError = { ~ },
        shopId = "test", 
        password = "test123!", 
        terminalId = "123qdfssdf"
)
        
```

Запрос на авторизацию в системе AirbaPay через передачу JWT. 
```authJwt()```

| Параметр    | Тип          | Обязательный | Описание           |
|-------------|--------------|--------------|--------------------|
| jwt         | String       | да           | JWT токен          |
| onSuccess   | () -> Unit   | да           | Лямбда на успех.   |
| onError     | () -> Unit   | да           | Лямбда на ошибку   |


```
AirbaPaySdk.authJwt(
        onSuccess = { ~  },
        onError = { ~ },
        jwt = "~"
)
        
```

Запрос на инициализацию платежа в системе AirbaPay.
Возвращает ```AirbaPaySdk.CreatePaymentResult```.
```createPayment()```

| Параметр               | Тип                                         | Обязательный    | Описание                                                                                                              |
|------------------------|---------------------------------------------|-----------------|-----------------------------------------------------------------------------------------------------------------------|
| authToken              | String                                      | да              | Токен, полученный из auth или другой реализацией получения токена                                                     |
| failureCallback        | String                                      | да              | URL вебхука при ошибке                                                                                                |
| successCallback        | String                                      | да              | URL вебхука при успехе                                                                                                |
| purchaseAmount         | Double                                      | да              | Сумма платежа                                                                                                         |
| accountId              | String                                      | да              | ID аккаунта пользователя                                                                                              |
| invoiceId              | String                                      | да              | ID платежа в системе магазина                                                                                         |
| orderNumber            | String                                      | да              | Номер заказа в системе магазина                                                                                       |
| onSuccess              | (AirbaPaySdk.CreatePaymentResult) -> Unit   | да              | Лямбда на успех. Возвращает AirbaPaySdk.CreatePaymentResult                                                           |
| onError                | () -> Unit                                  | да              | Лямбда на ошибку                                                                                                      |
| renderGooglePay        | Boolean?                                    | нет             | Флаг настройки показа функционала GooglePay в стандартном флоу. NULL - параметры с сервера                            |
| renderSavedCards       | Boolean?                                    | нет             | Флаг настройки показа функционала сохраненных карт в стандартном флоу. NULL - параметры с сервера                     |
| renderSecurityBiometry | Boolean?                                    | нет             | Флаг глобальной настройки в сдк для биометрии при оплате сохраненной картой или GooglePay. NULL - параметры с сервера |
| renderSecurityCvv      | Boolean?                                    | нет             | Флаг глобальной настройки в сдк для показа боттомщита с CVV при оплате сохраненной картой. NULL - параметры с сервера |
| autoCharge             | Int                                         | нет             | Автоматическое подтверждение при 2х-стадийном режиме 0 - нет, 1 - да                                                  |
| goods                  | List<AirbaPaySdk.Goods>                     | нет             | Список продуктов для оплаты. Если есть необходимость передачи списка товаров в систему                                |
| settlementPayments     | List<AirbaPaySdk.SettlementPayment>         | нет             | Распределение платежа по компаниям. В случае одной компании, может быть null                                          |


```AirbaPaySdk.CreatePaymentResult```

| Параметр  | Тип     | Описание              |
|-----------|---------|-----------------------|
| token     | String? | Обновленный токен     |
| paymentId | String? | ID созданного платежа |


```
AirbaPaySdk.createPayment(
                authToken = token,
                accountId = "77061111112",
                onSuccess = { result -> ~ },
                onError = { ~ },
                failureCallback = "https://site.kz/failure-clb",
                successCallback = "https://site.kz/success-clb",
                purchaseAmount = 1500.45,
                invoiceId = "1111111111",
                orderNumber = "ab1111111111"
            )            
```

## 6 API формы стандартного флоу

Предварительно выполнить ```AirbaPaySdk.authPassword()``` вместе с ```AirbaPaySdk.createPayment()```
Или выполнить только ```AirbaPaySdk.authJwt()```

Есть два варианта реализации стандартного флоу:
1) ```standardFlow()```.

| Параметр                | Тип                  | Обязательный | Описание                                                                      |
|-------------------------|----------------------|--------------|-------------------------------------------------------------------------------|
| context                 | Context              | да           | Контекст приложения                                                           |
| isGooglePayNative       | Boolean              | нет          | Флаг, определяющий показ нативной кнопки GooglePay вместо вебвьюшки           |

```
    AirbaPaySdk.standardFlow(
        context = context,
        isGooglePayNative = true
    )
```
2) ```standardFlowWebView()```

| Параметр                  | Тип                                                    | Обязательный | Описание                                                   |
|---------------------------|--------------------------------------------------------|--------------|------------------------------------------------------------|
| isLoadingComplete         | () -> Unit                                             | да           | Лямбда на завершение загрузки данных о платеже             |
| onError                   | () -> Unit                                             | да           | Лямбда на ошибку                                           |
| shouldOverrideUrlLoading  | (obj: AirbaPaySdk.ShouldOverrideUrlLoading) -> Boolean | да           | Лямбда, в которой описаны действия на коллбэки с вебвьюшки |

``` 
   AirbaPaySdk.standardFlowWebView(
      isLoadingComplete: { isLoading = false },
      onError: { ~ },
      shouldOverrideUrlLoading = { obj ->
        when {
            obj.isCallbackSuccess -> {
                // открыть страницу успеха
                return@standardFlowWebView true
            }
            obj.isCallbackBackToApp -> {
                // клик на кнопку "Вернуться в магазин" или на стрелку "назад"
                return@standardFlowWebView true
            }

            else -> obj.webView?.loadUrl(obj.url ?: "")
        }

        return@standardFlowWebView false
      }
   )
```

```AirbaPaySdk.ShouldOverrideUrlLoading```

| Параметр            | Тип      | Обязательный | Описание                                                                                                                                    |
|---------------------|----------|--------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| isCallbackSuccess   | Boolean  | да           | Флаг, что можно вызвать переход на страницу успеха                                                                                          |
| isCallbackBackToApp | Boolean  | да           | Флаг, что можно вызвать переход в приложение. true - если была нажата стрелака "назад" или кнопка "Вернуться в магазин" на страницах ошибки |
| url                 | String?  | да           | Проброс урла из коллбэка вебвьюшки                                                                                                          |
| webView             | WebView? | да           | Проброс webview из коллбэка вебвьюшки                                                                                                       |


## 7 API GooglePay

Для работы с GooglePay за пределами стандартного флоу:

1) Полностью реализовать на стороне приложения механизм вызова GooglePay боттомщита 
   и получения GooglePay токена. Как получить ```gatewayMerchantId``` и ```merchantId```, 
   необходимые для GooglePay, будет указано ниже  

2) Выполнить пункт "4 Подключение нативного GooglePay"

3) Предварительно выполнить ```AirbaPaySdk.authPassword()``` вместе с ```AirbaPaySdk.createPayment()```
   Или выполнить только ```AirbaPaySdk.authJwt()```

4) Вызвать ```AirbaPaySdk.getGooglePayMerchantIdAndGateway()``` для получения объекта ```GooglePayMerchantResponse```
     
    | Параметр                     | Тип                                  | Обязательный | Описание                              |
    |------------------------------|--------------------------------------|--------------|---------------------------------------|
    | onSuccess                    | (GooglePayMerchantResponse) -> Unit  | да           | Лямбда на успех                       |
    | onError                      | () -> Unit                           | да           | Лямбда на ошибку                      |
    
     ```GooglePayMerchantResponse```
   
    | Параметр            | Тип     | Описание                        |
    |---------------------|---------|---------------------------------|
    | gatewayMerchantId   | String? | gatewayMerchantId для GooglePay |
    | merchantId          | String? | merchanId для GooglePay         |
 
5) Вызвать после получения токена GooglePay функцию  ```processExternalGooglePay()```

   | Параметр       | Тип      | Обязательный | Описание                 |
   |----------------|----------|--------------|--------------------------|
   | googlePayToken | String   | да           | Токен GooglePay          |
   | activity       | Activity | да           | Активити                 |
   

   ``` 
    AirbaPaySdk.processExternalGooglePay(
                   activity = activity,
                   googlePayToken = googlePayToken
    )
   ```

## 8 API сохраненных карт

Запрос списка сохраненных карт пользователя
```getCards()```
Предварительно выполнить ```AirbaPaySdk.authPassword()``` или ```AirbaPaySdk.authJwt()```

| Параметр  | Тип                      | Обязательный | Описание                                    |
|-----------|--------------------------|--------------|---------------------------------------------|
| onSuccess | (List<BankCard>) -> Unit | да           | Лямбда на успех со списком сохраненных карт |
| onNoCards | () -> Unit               | да           | Лямбда на отсутствие сохраненных карт       |


Запрос удаления сохраненной карты пользователя
```deleteCard()```
Предварительно выполнить ```AirbaPaySdk.authPassword()``` или ```AirbaPaySdk.authJwt()```

| Параметр  | Тип          | Обязательный | Описание                                             |
|-----------|--------------|--------------|------------------------------------------------------|
| cardId    | String       | да           | ```id``` сохраненной карты из запроса ```getCards``` |
| onSuccess | () -> Unit   | да           | Лямбда на успех                                      |
| onError   | () -> Unit   | да           | Лямбда на ошибку                                     |


Запрос проведения оплаты по сохраненной карте пользователя
```paySavedCard()```
Предварительно выполнить ```AirbaPaySdk.authPassword()``` или ```AirbaPaySdk.authJwt()```

| Параметр  | Тип            | Обязательный | Описание                                              |
|-----------|----------------|--------------|-------------------------------------------------------|
| activity  | Activity       | да           | Activity                                              |
| bankCard  | BankCard       | да           | Экземпляр карты, получаемый из запроса ```getCards``` |
| isLoading | (Bool) -> Unit | да           | Лямбда для показа прогрессбара                        |
| onError   | () -> Unit     | да           | Лямбда на ошибку                                      |




------------------------------------------------------------------------------------------------------------------------
## V1
------------------------------------------------------------------------------------------------------------------------

## 1 Подключение sdk

## 2 Настройки PROGUARD

## 3 Вызов стартовой формы

## 4 Подключение нативного GooglePay

## 5 Подключение API внешнего взаимодействия с GooglePay

## 6 Рекомендация в случае интеграции в flutter



## 1  Подключение sdk

В  ```build.gradle ``` модуля добавь ``` implementation 'kz.airbapay:apay_android:~'```

Для инициализации sdk нужно выполнить ```AirbaPaySdk.initSdk()``` перед
вызовом ```AirbaPaySdk.startAirbaPay() ```.

| Параметр                    | Тип                                 | Обязательный | Описание                                                                                                                                                                                             |
|-----------------------------|-------------------------------------|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| shopId                      | String                              | да           | ID магазина в системе AirbaPay                                                                                                                                                                       |
| context                     | Context                             | да           | Контекст приложения                                                                                                                                                                                  |
| password                    | String                              | да           | Пароль в системе AirbaPay                                                                                                                                                                            |
| accountId                   | String                              | да           | ID аккаунта пользователя                                                                                                                                                                             |
| terminalId                  | String                              | да           | ID терминала под которым создали платеж                                                                                                                                                              |
| lang                        | AirbaPaySdk.Lang                    | да           | Код языка для UI                                                                                                                                                                                     |
| isProd                      | Boolean                             | да           | Продовская или тестовая среда airbapay                                                                                                                                                               |
| phone                       | String                              | да           | Телефон пользователя                                                                                                                                                                                 |
| failureCallback             | String                              | да           | URL вебхука при ошибке                                                                                                                                                                               |
| successCallback             | String                              | да           | URL вебхука при успехе                                                                                                                                                                               |
| userEmail                   | String                              | да           | Емейл пользователя, куда будет отправлена квитанция. В случае отсутствия емейла                                                                                                                      |
| colorBrandMain              | androidx.compose.ui.graphics.Color  | нет          | Брендовый цвет кнопок, переключателей и текста                                                                                                                                                       |
| colorBrandInversion         | androidx.compose.ui.graphics.Color  | нет          | Цвет текста у кнопок с брендовым цветом                                                                                                                                                              |
| autoCharge                  | Int                                 | нет          | Автоматическое подтверждение при 2х-стадийном режиме 0 - нет, 1 - да                                                                                                                                 |
| enabledLogsForProd          | Boolean                             | нет          | Флаг для включения логов                                                                                                                                                                             |
| purchaseAmount              | Double                              | да           | Сумма платежа                                                                                                                                                                                        |
| invoiceId                   | String                              | да           | ID платежа в системе магазина                                                                                                                                                                        | 
| orderNumber                 | String                              | да           | Номер заказа в системе магазина                                                                                                                                                                      |
| goods                       | List<AirbaPaySdk.Goods>             | да           | Список продуктов для оплаты                                                                                                                                                                          |
| settlementPayments          | List<AirbaPaySdk.SettlementPayment> | нет          | Распределение платежа по компаниям. В случае одной компании, может быть null                                                                                                                         |
| onProcessingResult          | (Activity, Boolean) -> Unit         | да           | Лямбда, вызываемая при клике на кнопку "Вернуться в магазин" и при отмене процесса. Разработчику в ней нужно прописать код для возврата в приложение. Обязательно добавить в конце activity.finish() |
| hideInternalGooglePayButton | Boolean                             | нет          | Флаг, определяющий, нужно ли скрывать кнопку гуглпэя на страницах сдк                                                                                                                                |
| isGooglePayNative           | Boolean                             | нет          | Флаг, определяющий показ нативной кнопки GooglePay вместо вебвьюшки                                                                                                                                  |
| needDisableScreenShot       | Boolean                             | нет          | Флаг включения/отключения защиты от скриншота страниц. По дефолту выключен                                                                                                                           |

При смене значения isProd, требуется выгрузить приложение из памяти.

Пример:

```
 val goods = listOf(
           AirbaPaySdk.Goods(
               model = "Чай Tess Banana Split черный 20 пирамидок",
               brand = "Tess",
               category = "Черный чай",
               quantity = 1,
               price = 1000
           )
       )

       val settlementPayment = listOf(
           AirbaPaySdk.SettlementPayment(
               amount = 1000.0,
               companyId = "test_id"
           )
       )
       
AirbaPaySdk.initSdk(
    context = this.application,
    shopId = "test-baykanat",
    password = "baykanat123!",
    terminalId = "65c5df69e8037f1b451a0594",
    accountId = "1000009806",
    lang = AirbaPaySdk.Lang.RU,
    isProd = false, 
    phone = "77051000000",
    failureCallback = "https://site.kz/failure-clb", 
    successCallback = "https://site.kz/success-clb",                
    userEmail = "test@test.com", 
    colorBrandMain = Color.Red,
    purchaseAmount = 1000.0,
    invoiceId = invoiceId,
    orderNumber = orderNumber,
    goods = goods,
    settlementPayments = settlementPayment,
    onProcessingReult = { activity, isSuccess ->

         if (isSuccess) { 
                  startActivity(Intent(activity, SuccessActivity::class.java)) 
         } else {
                  startActivity(Intent(activity, ErrorActivity::class.java)) 
         }
         activity.finish()
    } 
)

```

## 2 Настройки PROGUARD

В случае, если используется Proguard, нужно добавить настройки

```
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response
```

## 3 Вызов стартовой формы

Открытие формы AirbaPay выполняется через  ```AirbaPaySdk.startAirbaPay() ```.

| Параметр                       | Тип                | Обязательный | Описание                                                                                                                                                                                                      |
|--------------------------------|--------------------|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| activity                       | Activity           | да           | Activity                                                                                                                                                                                                      |
| redirectToCustomSuccessPage    | (Activity) -> Unit | нет          | Лямбда, вызываемая для перехода на кастомную страницу успешного завершения. Разработчику в ней нужно прописать код для перехода на кастомную страницу                                                         |
| redirectToCustomFinalErrorPage | (Activity) -> Unit | нет          | Лямбда, вызываемая для перехода на кастомную страницу Финальной ошибки (остальные ошибки показываются в дефолтной странице ошибки). Разработчику в ней нужно прописать код для перехода на кастомную страницу |

## Пример без кастомной страницы:

 ```
startAirbaPay(activity = this@MainActivity)
 ```

## Пример с кастомной станицей:

```
startAirbaPay(
    activity = this@MainActivity,
    redirectToCustomSuccessPage = { 
        val intent = Intent(activity, CustomSuccessActivity::java.class)
        activity.startActivity()
        activity.finish()
    }
)
 ```

Для проекта без Jetpack Compose потребуются дополнительные настройки проекта.

Добавить в build.gradle app и, в случае модульной архитектуры, в модуль, где будет использоваться:

 ```
android {
 
    buildFeatures {
       compose true
       viewBinding true
    }
    
    composeOptions {
       kotlinCompilerExtensionVersion '1.5.9'
    }
    
    kotlinOptions {
      jvmTarget = 17
    }
}
 ```

В зависимости от версии котлина, потребуется подобрать версию kotlinCompilerExtensionVersion
https://developer.android.com/jetpack/androidx/releases/compose-kotlin

 ``` 

dependencies {

    implementation "kz.airbapay:apay_android:$apay_version"
    implementation platform("androidx.compose:compose-bom:2024.01.00")
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui'

}
 ```

## 4 Подключение нативного GooglePay

1) Изменить параметр isGooglePayNative в initSdk на true

2) Перейти в консоль GooglePay  https://pay.google.com/business/console/ и перейти в пункт меню
   Google Pay API

3) Найти в списке “Integrate with your Android app” ваше приложение и кликнуть “Управление”

4) Выберите тип интеграции “Через шлюз”.

5) Загрузите скриншоты по списку указанному ниже и нажмите “Сохранить”

6) Кликните “Submit for approval”

7) В случае отказа по каким-либо причинам (к примеру, дизайн кнопки не соответствует их
   требованиям), нужно будет ответить на письмо гугла, что вы используете стороннее решение компании
   Airba Pay

## 5 Подключение API внешнего взаимодействия с GooglePay

1) Добавить ```val googlePayViewModel = GooglePayViewModel()```

   и повесить на клик последовательный вызов функций:

```
AirbaPaySdk.initSdk(
   ~
   isGooglePayNative = true
)

googlePayViewModel.auth(
            activity = activity,
            onSuccess = { response ->
                isLoading.value = false
              
                val gatewayMerchantId: String? = response.gatewayMerchantId
                val gateway: String? = response.gateway
                
                // Здесь вызвать получение токена гугл пэя через шаг №2
            },
            onError = {
                isLoading.value = false
                // вывод сообщения об ошибке
            }
)
               
```

```auth(~)```

| Параметр       | Тип                                 | Обязательный | Описание                                                |
|----------------|-------------------------------------|--------------|---------------------------------------------------------|
| googlePayToken | String                              | да           | Токен, полученный из гугл пэй                           |
| onSuccess      | (GooglePayMerchantResponse) -> Unit | да           | Коллбэк возврата данных для gateway и gatewayMerchantId |
| onError        | () -> Unit                          | да           | Коллбэк ошибки                                          |

2) Пройти шаги интеграции из
   https://developers.google.com/pay/api/android/guides/setup?hl=ru
   и
   https://developers.google.com/pay/api/android/guides/tutorial?hl=ru

   В запросе на получение токена обязательно указать информацию как указано ниже

   .put("merchantInfo", JSONObject().put("merchantName", "AirbaPay"))

   .put("parameters", JSONObject(mapOf(
   "gateway" to gateway,
   "gatewayMerchantId" to gatewayMerchantId
   )))

3) Далее вызвать

```
googlePayViewModel.processingWalletExternal(
        activity = activity,
        coroutineScope = coroutineScope!!,
        googlePayToken = googlePayToken
) 
```

```processingWalletExternal(~)```

| Параметр       | Тип            | Обязательный | Описание                      |
|----------------|----------------|--------------|-------------------------------|
| googlePayToken | String         | да           | Токен, полученный из гугл пэй |
| activity       | Activity       | да           | Активити                      |
| coroutineScope | CoroutineScope | да           | CoroutineScope                |

## 6 Рекомендация в случае интеграции в flutter

В dart добавьте:

```
  final MethodChannel channel = MethodChannel("com.example.testFlutter/AirbaPayChannel");

  Future<void> callNativeMethod() async {
    try {
      await channel.invokeMethod('pay');
    } catch (e) {
      print('Error calling native method: $e');
    }
  }
```

И нужно вызвать ```callNativeMethod``` для перехода на страницы сдк.

Нужно создать дополнительный промежуточный активити FlutterAirbaPayActivity,
в котором будет производиться инициализация и переход на страницу сдк.
Это нужно, чтоб при нажатии назад из сдк, не закрывалось приложение.

Добавить в манифест описание этого активити

 ```
<activity android:name=".pay.airba.FlutterAirbaPayActivity"
    android:theme="@style/AppTheme.AppCompat"
    android:exported="false" />
 ```

В ```MainActivity``` нужно добавить

```
 override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.example.testFlutter/AirbaPayChannel")
            .setMethodCallHandler { call: MethodCall, result: Result ->
                if (call.method == "pay") {
                    result.success(null)
                    FlutterAirbaPayActivity.start(this, call, result)
                } else {
                    result.notImplemented()
                }
            }
    }
```

В ```FlutterAirbaPayActivity```

```

class FlutterAirbaPayActivity : AppCompatActivity() {
    /**
     * Два состояния для activity:
     *
     * 1. started airba pay flow
     * 2. finished airba pay flow
     */
    private var isFlowStarted = false

    companion object {
        fun start(context: Activity, call: MethodCall, result: MethodChannel.Result) {
            val intent = Intent(context, FlutterAirbaPayActivity::class.java)
            context.startActivityForResult(intent, 123)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val someInvoiceId = ~
        val someOrderNumber = ~

        val goods = listOf(
            AirbaPaySdk.Goods(
                model = "description",
                brand = "TechnoFit",
                category = "Services",
                quantity = 1,
                price = 1500L,
            ),
        )
        AirbaPaySdk.initSdk(
            context = this,
            isProd = true,
            phone = "+77051234567",
            shopId = "test-baykanat",
            lang = AirbaPaySdk.Lang.RU,
            password = "baykanat123!",
            terminalId = "65c5df69e8037f1b451a0594",
            failureCallback = "https://site.kz/failure-clb",
            successCallback = "https://site.kz/success-clb",
            userEmail = "test@test.com",
            accountId = "1000000000",
            purchaseAmount = 1500.0,
            invoiceId = someInvoiceId.toString(),
            orderNumber = someOrderId.toString(),
            goods = goods,
        ) { activity, paymentSubmittingResult ->
            close(paymentSubmittingResult, activity)
        }

        AirbaPaySdk.startAirbaPay(
            this,
            redirectToCustomSuccessPage = { activity ->
                close(true, activity)
            },
        )
    }

    /**
     * Finishes this activity with setting result
     *
     * @see handleAirbaActivityResult
     */
    private fun close(result: Boolean, childActivity: Activity? = null) {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra("result", result)
        )
        finish()
        childActivity?.finish()
    }

    override fun onStart() {
        super.onStart()
        if(!isFlowStarted) {
            // 1. started airba pay flow (invoked after onCreate)
            isFlowStarted = true
        } else {
            // 2. finished airba pay flow (invoked after backButton pressed or etc)
            close(false)
        }
    }
}
```

## Fork notes (justprodev/apay-android)

Use via JitPack: add `maven { url "https://jitpack.io" }` and `implementation 'com.github.justprodev:apay-android:<tag>'`.

* card.io sources (`io.card.payment`, `src/main/cpp/card.io-dmz`) are part of the module, see `src/main/cpp/LICENSE-card.io.md` (MIT).
* OpenCV 2.4.13.7 (core, imgproc) is linked statically from `src/main/cpp/opencv/lib`. To rebuild: `opencv/build_opencv.ps1` (needs Android NDK r28+ and cmake/ninja).
* Release: push a git tag, JitPack builds it with `jitpack.yml`.
