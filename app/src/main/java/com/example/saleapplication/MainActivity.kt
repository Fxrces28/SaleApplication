package com.example.saleapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saleapplication.ui.theme.SaleApplicationTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowForward
import kotlinx.coroutines.launch
import retrofit2.Converter
import okhttp3.ResponseBody
import java.lang.reflect.Type
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.LaunchedEffect
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query
import androidx.compose.material3.CircularProgressIndicator
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayInputStream

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import retrofit2.http.Path

import java.text.SimpleDateFormat
import java.util.Locale
data class ImagesResponse(
    val id: Long,
    val image: String
)

data class ProductsResponse(
    val content: List<ApiProduct>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int,
    val size: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)
// ... существующие data class ...

// Модели для корзины
data class CartItemRequest(
    val productId: Long,
    val quantity: Int
)

data class CartItemResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val pricePerItem: Int,
    val totalPrice: Int
)

data class CartResponse(
    val id: Long,
    val userId: Long,
    val items: List<CartItemResponse>,
    val grandTotal: Int
)

// Модель для создания заказа
data class CreateOrderRequest(
    val shippingAddress: String,
    val phoneNumber: String
)

data class OrderResponse(
    val id: Long,
    val user: UserInfo,
    val totalPrice: Int,
    val status: String,
    val shippingAddress: String,
    val phoneNumber: String,
    val orderItems: List<OrderItemResponse>,
    val payment: PaymentInfo,
    val createdAt: String,
    val updatedAt: String
)

data class UserInfo(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val address: String?,
    val roles: List<Role>,
    val active: Boolean
)

data class Role(
    val id: Long,
    val name: String
)

data class OrderItemResponse(
    val id: Long,
    val productReadDTO: ProductReadDTO,
    val quantity: Int
)

data class ProductReadDTO(
    val id: Long,
    val name: String,
    val description: String,
    val price: Int,
    val stockQuantity: Int
)

// Добавьте в существующие data classes

data class ReviewResponse(
    @SerializedName("content")
    val content: List<Review>,
    @SerializedName("totalElements")
    val totalElements: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("number")
    val number: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("first")
    val first: Boolean,
    @SerializedName("last")
    val last: Boolean,
    @SerializedName("empty")
    val empty: Boolean
)

data class Review(
    @SerializedName("id")
    val id: Long,
    @SerializedName("username")
    val username: String,
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("text")
    val text: String,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class ReviewUser(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String
)

data class CreateReviewRequest(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("text")
    val text: String,
    @SerializedName("rating")
    val rating: Int
)

data class PaymentInfo(
    val paymentId: Long,
    val orderId: Long,
    val amount: Int,
    val status: String,
    val currency: String,
    val transactionId: String?,
    val confirmationUrl: String
)
data class ApiProduct(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("stockQuantity")
    val stockQuantity: Int,

    @SerializedName("height")
    val height: Int? = null, // Добавьте это поле

    @SerializedName("width")
    val width: Int? = null,   // Добавьте это поле

    @SerializedName("color") // Добавляем цвет
    val color: String? = null,

    @SerializedName("averageRating") // Добавляем средний рейтинг
    val averageRating: Double? = null

)

// Data classes for API requests and responses
data class RegisterRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class PaymentResponse(
    val id: Long,
    val order: OrderForPayment,
    val amount: Int,
    val currency: String,
    val status: String,
    val transactionId: String?,
    val confirmationUrl: String
)

data class OrderForPayment(
    val id: Long,
    val user: UserInfo,
    val totalPrice: Int,
    val status: String,
    val shippingAddress: String,
    val phoneNumber: String,
    val createdAt: String,
    val orderItems: List<OrderItemForPayment>
)

data class OrderItemForPayment(
    val id: Long,
    val product: ProductForPayment,
    val quantity: Int
)

data class ProductForPayment(
    val id: Long,
    val name: String,
    val description: String,
    val price: Int,
    val stockQuantity: Int
)

// API Service interface - измените возвращаемые типы
interface ApiService {
    @POST("api/v1/users/register")
    suspend fun register(@Body request: RegisterRequest): String

    @POST("api/v1/users/login")
    suspend fun login(@Body request: LoginRequest): String
    @GET("api/v1/products")
    suspend fun getProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: List<String> = emptyList()
    ): ProductsResponse
    // Новые методы для корзины и заказов
    @POST("api/v1/cart/items")
    suspend fun addToCart(@Body request: CartItemRequest): CartResponse

    @GET("api/v1/cart")
    suspend fun getCart(): CartResponse

    @POST("api/v1/orders/from-cart")
    suspend fun createOrderFromCart(@Body request: CreateOrderRequest): OrderResponse
    @GET("api/v1/orders/my-orders")
    suspend fun getMyOrders(): List<OrderResponse>

    @GET("api/v1/images/allByProduct")
    suspend fun getProductImages(@Query("productId") productId: Long): ImagesResponse

    @POST("api/v1/payments/create/{orderId}")
    suspend fun createPayment(@Path("orderId") orderId: Long): PaymentResponse

    @GET("api/v1/reviews/product/{productId}")
    suspend fun getProductReviews(
        @Path("productId") productId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ReviewResponse

    @POST("api/v1/reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Review

}



object ImageUtils {
    fun base64ToImageBitmap(base64String: String): ImageBitmap? {
        return try {
            println("Base64 string received, length: ${base64String.length}")

            // Если строка слишком короткая, возможно это ссылка или ошибка
            if (base64String.length < 100) {
                println("Base64 string is too short, might be an error or URL")
                return null
            }

            // Проверяем наличие префикса
            val pureBase64 = if (base64String.startsWith("data:image")) {
                base64String.substringAfter(",")
            } else if (base64String.startsWith("{")) {
                // Возможно это JSON, попробуем распарсить
                println("String starts with '{', might be JSON")
                return null
            } else {
                base64String
            }

            println("Processing base64 of length: ${pureBase64.length}")

            // Проверяем, что это валидный base64
            if (!isValidBase64(pureBase64)) {
                println("Not a valid base64 string")
                return null
            }

            val decodedBytes = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
            println("Decoded bytes: ${decodedBytes.size} bytes")

            if (decodedBytes.isEmpty()) {
                println("Decoded bytes are empty")
                return null
            }

            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap == null) {
                println("Failed to decode bitmap from bytes")
                // Попробуем другой метод
                val inputStream = ByteArrayInputStream(decodedBytes)
                val bitmap2 = BitmapFactory.decodeStream(inputStream)
                if (bitmap2 == null) {
                    println("Also failed with decodeStream")
                    return null
                }
                println("Successfully decoded with decodeStream")
                return bitmap2.asImageBitmap()
            }

            println("Bitmap created: ${bitmap.width}x${bitmap.height}")
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            println("Error decoding base64 image: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun isValidBase64(str: String): Boolean {
        return try {
            android.util.Base64.decode(str, android.util.Base64.DEFAULT)
            true
        } catch (e: Exception) {
            false
        }
    }
}


// Кастомный конвертер для обработки строкового ответа
class StringConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (type == String::class.java) {
            return Converter<ResponseBody, String> { responseBody ->
                responseBody.string()
            }
        }
        return null
    }
}

// Retrofit instance
object RetrofitClient {
    private const val BASE_URL = "https://xn----dtbwmdc.xn--p1ai/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val originalRequest = chain.request()

            // Добавляем заголовок авторизации, если токен есть
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")

            if (TokenManager.token.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer ${TokenManager.token}")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()


    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(StringConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}

// Token manager (you can store token in SharedPreferences)
object TokenManager {
    var token: String by mutableStateOf("")
}


val CreamColor = Color(0xFFFEF1E1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaleApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = CreamColor) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf("main") }
    var isLoggedIn by remember { mutableStateOf(TokenManager.token.isNotEmpty()) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var currentUserName by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            if (currentScreen == "main" || currentScreen == "products" ||
                currentScreen == "orders" || currentScreen == "cart") {
                NavigationBar(
                    containerColor = Color(0xE8B5982B).copy(alpha = 0.17f)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "products",
                        onClick = { currentScreen = "products" },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.orders),
                                contentDescription = "Товары"
                            )
                        },
                        label = { Text("Товары") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "main",
                        onClick = { currentScreen = "main" },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Главная",
                                tint = Color.Unspecified
                            )
                        },
                        label = { Text("Главная") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "cart",
                        onClick = {
                            currentScreen = "cart" // Всегда переходим в корзину
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (CartManager.cartItemCount > 0) {
                                        Badge {
                                            Text(CartManager.cartItemCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.cart),
                                    contentDescription = "Корзина"
                                )
                            }
                        },
                        label = { Text("Корзина") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "orders",
                        onClick = {
                            currentScreen = if (isLoggedIn) "orders" else "login"
                        },
                        icon = {
                            if (isLoggedIn) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_orders),
                                    contentDescription = "Мои заказы"
                                )
                            }
                            else {
                                Icon(
                                    painter = painterResource(id = R.drawable.out),
                                    contentDescription = "Вход"
                                )
                            }
                        },
                        label = { Text(if (isLoggedIn) "Заказы" else "Вход") }
                    )
                    if (isLoggedIn) {
                        NavigationBarItem(
                            selected = false,
                            onClick = {
                                showLogoutDialog = true
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.logout),
                                    contentDescription = "Выйти"
                                )
                            },
                            label = { Text("Выйти") }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        when (currentScreen) {
            "main" -> MainScreen(Modifier.padding(paddingValues))
            "login" -> LoginScreen(
                modifier = Modifier.padding(paddingValues),
                onRegisterClick = { currentScreen = "register" },
                onBackClick = { currentScreen = "main" },
                onLoginSuccess = { username ->
                    isLoggedIn = true
                    currentUserName = username
                    currentScreen = "main"
                }
            )
            "register" -> RegisterScreen(
                modifier = Modifier.padding(paddingValues),
                onLoginClick = { currentScreen = "login" },
                onBackClick = { currentScreen = "main" },
                onRegisterSuccess = { currentScreen = "login" }
            )
            "products" -> ProductsScreen(
                modifier = Modifier.padding(paddingValues),
                isLoggedIn = isLoggedIn,
                currentUserName = currentUserName,
                onLoginRequired = { currentScreen = "login" }
            )
            "cart" -> CartScreen(
                modifier = Modifier.padding(paddingValues),
                isLoggedIn = isLoggedIn,
                onBackClick = { currentScreen = "main" },
                onLoginRequired = { currentScreen = "login" }
            )
            "orders" -> OrdersScreen(
                modifier = Modifier.padding(paddingValues),
                isLoggedIn = isLoggedIn,
                onLoginRequired = { currentScreen = "login" }
            )
        }
    }

    // Диалог подтверждения выхода
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Выход из аккаунта",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите выйти из аккаунта?",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        TokenManager.token = ""
                        isLoggedIn = false
                        currentUserName = ""
                        CartManager.clear() // Очищаем корзину при выходе
                        currentScreen = "main"
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Text("Выйти", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Отмена", color = Color.White)
                }
            }
        )
    }
}
@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    onLoginRequired: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var orders by remember { mutableStateOf<List<OrderResponse>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    // Функция загрузки заказов
    fun loadOrders() {
        if (!isLoggedIn) return

        coroutineScope.launch {
            try {
                isLoading = true
                orders = RetrofitClient.instance.getMyOrders()
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = when {
                    e is java.net.ConnectException -> "Не удалось подключиться к серверу"
                    e is java.net.SocketTimeoutException -> "Таймаут подключения"
                    e is retrofit2.HttpException -> {
                        when (e.code()) {
                            404 -> "Заказы не найдены"
                            401 -> "Требуется авторизация"
                            403 -> "Доступ запрещен"
                            500 -> "Ошибка сервера (500). Попробуйте позже"
                            else -> "Ошибка загрузки: ${e.code()}"
                        }
                    }
                    else -> "Ошибка загрузки заказов: ${e.message}"
                }
                println("Orders loading error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    // Загружаем заказы при открытии экрана
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            loadOrders()
        } else {
            isLoading = false
        }
    }



    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Мои заказы",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )

        // Кнопка обновления
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { if (isLoggedIn) loadOrders() },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Обновить",
                    tint = if (isLoading) Color.Gray else Color(0xFFC8744F)
                )
            }
        }

        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Для просмотра заказов",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "необходимо войти в систему",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLoginRequired,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F))
                    ) {
                        Text("Войти")
                    }
                }
            }
        } else if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFC8744F))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка заказов...", color = Color.Gray)
                }
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { loadOrders() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F))
                    ) {
                        Text("Повторить")
                    }
                }
            }
        } else if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "У вас пока нет заказов",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Создайте свой первый заказ в корзине",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Список заказов
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders.sortedByDescending { it.createdAt }) { order ->
                    OrderCard(order)
                }
            }
        }
    }
}
@Composable
fun OrderCard(order: OrderResponse) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок с номером заказа
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Заказ №${order.id}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Статус заказа
                Box(
                    modifier = Modifier
                        .background(
                            color = when (order.status) {
                                "PENDING" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                "PROCESSING" -> Color(0xFFFFC107).copy(alpha = 0.2f)
                                "SHIPPED" -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                "DELIVERED" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                "CANCELLED" -> Color(0xFFF44336).copy(alpha = 0.2f)
                                else -> Color.Gray.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (order.status) {
                            "PENDING" -> "Ожидание"
                            "PROCESSING" -> "В обработке"
                            "SHIPPED" -> "Отправлен"
                            "DELIVERED" -> "Доставлен"
                            "CANCELLED" -> "Отменен"
                            else -> order.status
                        },
                        color = when (order.status) {
                            "PENDING" -> Color(0xFFFF9800)
                            "PROCESSING" -> Color(0xFFA7D8E9)
                            "SHIPPED" -> Color(0xFFD5A7E9)
                            "DELIVERED" -> Color(0xFF4CAF50)
                            "CANCELLED" -> Color(0xFFF44336)
                            else -> Color.Gray
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Основная информация
            Column {
                // Дата заказа
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Дата",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Дата: ${formatDate(order.createdAt)}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Адрес доставки
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Адрес",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Адрес: ${order.shippingAddress}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = if (expanded) Int.MAX_VALUE else 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Сумма заказа
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Сумма: ${order.totalPrice} ₽",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопка "Подробнее"
                Button(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC8744F).copy(alpha = 0.1f),
                        contentColor = Color(0xFFC8744F)
                    ),
                    elevation = null
                ) {
                    Text(if (expanded) "Скрыть детали" else "Показать детали")
                }

                // Детали заказа (раскрывающиеся)
                if (expanded) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Товары в заказе
                    Text(
                        text = "Товары:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    order.orderItems.forEach { item ->
                        OrderItemRow(item)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Статус оплаты
                    PaymentStatus(order.payment, order.id)
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(item: OrderItemResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productReadDTO.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = item.productReadDTO.description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${item.productReadDTO.price} ₽ × ${item.quantity} шт.",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Итого: ${item.productReadDTO.price * item.quantity} ₽",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC8744F)
            )
        }
    }
}

@Composable
fun PaymentStatus(payment: PaymentInfo?, orderId: Long) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Функция для получения ссылки на оплату
    fun getPaymentUrl(): String? {
        return payment?.confirmationUrl?.takeIf { it.isNotEmpty() }
    }

    // Функция для создания платежа
    fun createPayment() {
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Создаем платеж
                val paymentResponse = RetrofitClient.instance.createPayment(orderId)

                // Получаем ссылку для оплаты
                val url = paymentResponse.confirmationUrl

                // Открываем ссылку в браузере
                if (url.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } else {
                    errorMessage = "Ссылка для оплаты не получена"
                }

            } catch (e: Exception) {
                errorMessage = when {
                    e is java.net.ConnectException -> "Не удалось подключиться к серверу"
                    e is java.net.SocketTimeoutException -> "Таймаут подключения"
                    e is retrofit2.HttpException -> {
                        when (e.code()) {
                            401 -> "Требуется авторизация"
                            403 -> "Доступ запрещен"
                            404 -> "Заказ не найден"
                            400 -> "Невозможно создать платеж для этого заказа"
                            500 -> "Ошибка сервера при создании платежа"
                            else -> "Ошибка сервера: ${e.code()}"
                        }
                    }
                    else -> "Ошибка создания платежа: ${e.message}"
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Функция для открытия существующей ссылки оплаты
    fun openExistingPayment() {
        val url = getPaymentUrl()
        if (url != null && url.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } else {
            errorMessage = "Ссылка для оплаты недоступна"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (payment?.status) {
                "SUCCEEDED" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                "PENDING" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                "CANCELED" -> Color(0xFFF44336).copy(alpha = 0.1f)
                else -> Color.Gray.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Оплата",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                payment?.let {
                    Text(
                        text = when (it.status) {
                            "SUCCEEDED" -> "Оплачено"
                            "PENDING" -> "Ожидает оплаты"
                            "CANCELED" -> "Отменена"
                            else -> it.status
                        },
                        color = when (it.status) {
                            "SUCCEEDED" -> Color(0xFF4CAF50)
                            "PENDING" -> Color(0xFFFF9800)
                            "CANCELED" -> Color(0xFFF44336)
                            else -> Color.Gray
                        },
                        fontWeight = FontWeight.Medium
                    )
                } ?: run {
                    Text(
                        text = "Не создан",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Если есть информация о платеже
            payment?.let {
                Text(
                    text = "Сумма: ${payment.amount} ${payment.currency}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (payment.transactionId != null) {
                    Text(
                        text = "ID транзакции: ${payment.transactionId}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Показать ошибку, если есть
            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }

            // Кнопки действий
            Spacer(modifier = Modifier.height(12.dp))

            when {
                payment == null -> {
                    // Если платеж не создан - кнопка "Создать оплату"
                    Button(
                        onClick = { createPayment() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC8744F)
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Создать оплату", color = Color.White)
                        }
                    }
                }
                payment.status == "PENDING" -> {
                    // Если платеж в ожидании - только кнопка "Открыть страницу оплаты"
                    val paymentUrl = getPaymentUrl()
                    if (paymentUrl != null) {
                        Button(
                            onClick = { openExistingPayment() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Открыть страницу оплаты", color = Color.White)
                        }
                    }
                }
                payment.status == "SUCCEEDED" -> {
                    Text(
                        text = "✓ Оплата успешно завершена",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
                payment.status == "CANCELED" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "✗ Платеж отменен",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Medium
                        )
                        Button(
                            onClick = { createPayment() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC8744F)
                            ),
                            enabled = !isLoading
                        ) {
                            Text("Повторить оплату")
                        }
                    }
                }
            }
        }
    }
}

// Функция для форматирования даты
fun formatDate(dateString: String): String {
    return try {
        // Преобразуем ISO дату в читаемый формат
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val outputFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun BackButton(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Назад",
            modifier = Modifier
                .size(32.dp)
                .clickable { onBackClick() }
                .align(Alignment.TopStart)
        )
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamColor)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "ОКНО в Россию",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )
        Text(
            text = "Свет под вашим контролем",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))
        SwipeImageSlider()

        Spacer(modifier = Modifier.height(16.dp))
        ProductList()
    }
}

@Composable
fun SwipeImageSlider() {
    val images = listOf(
        R.drawable.sample1,
        R.drawable.sample2,
        R.drawable.sample3
    )

    var currentIndex by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp)
            .clickable {
                // По нажатию меняем картинку
                currentIndex = (currentIndex + 1) % images.size
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = images[currentIndex]),
            contentDescription = "Слайд",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Необязательно: индикатор текущего слайда
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(images.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .background(
                            color = if (index == currentIndex) Color.White else Color.Gray,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun ProductList() {
    val products = listOf(
        Product(1L, "Серый пластик", "Описание: прочный, лёгкий материал", R.drawable.product1, 1000, 10, null),
        Product(2L, "Антрацит пластик", "Описание: современный тёмный оттенок", R.drawable.product2, 1200, 5, null)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        products.forEach { product ->
            ProductCard(product)
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD2C2B5))
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = product.image),
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 8.dp),
                contentScale = ContentScale.Crop
            )
            Column {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(product.description, fontSize = 14.sp, color = Color.Black)
            }
        }
    }
}

@Composable
fun ProductsScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    currentUserName: String = "",
    onLoginRequired: () -> Unit = {}
) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    // Добавляем состояние для выбранного товара
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    // Для отзывов
    data class Review(val name: String, val text: String, val stars: Int, val date: String)
    var userReview by remember { mutableStateOf("") }
    var userStars by remember { mutableStateOf(0) }
    var showReviewSent by remember { mutableStateOf(false) }
    var showReviewLoginRequired by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Функция загрузки товаров
    fun loadProducts(page: Int = 0) {
        coroutineScope.launch {
            try {
                isLoading = true
                val response = RetrofitClient.instance.getProducts(
                    page = page,
                    size = 10,
                    sort = listOf("name,asc")
                )

                val newProducts = response.content.map { apiProduct ->
                    Product(
                        id = apiProduct.id,
                        name = apiProduct.name,
                        description = apiProduct.description,
                        price = apiProduct.price.toInt(),
                        stockQuantity = apiProduct.stockQuantity,
                        height = apiProduct.height,
                        width = apiProduct.width,
                        color = apiProduct.color,
                        averageRating = apiProduct.averageRating,
                        image = android.R.drawable.ic_menu_gallery
                    )
                }

                if (page == 0) {
                    products = newProducts
                } else {
                    products = products + newProducts
                }

                hasMore = !response.last
                currentPage = page
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = when {
                    e is java.net.ConnectException -> "Не удалось подключиться к серверу"
                    e is java.net.SocketTimeoutException -> "Таймаут подключения"
                    e is retrofit2.HttpException -> {
                        when (e.code()) {
                            404 -> "Товары не найдены"
                            500 -> "Ошибка сервера (500). Попробуйте позже"
                            else -> "Ошибка загрузки: ${e.code()}"
                        }
                    }
                    e.message?.contains("Unable to resolve host") == true -> "Проблемы с интернет-соединением"
                    else -> "Ошибка загрузки товаров: ${e.message}"
                }
                println("Products loading error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Загрузка товаров при первом открытии
    LaunchedEffect(Unit) {
        loadProducts(0)
    }

    // Функция для загрузки следующей страницы
    fun loadNextPage() {
        if (!isLoading && hasMore) {
            loadProducts(currentPage + 1)
        }
    }

    // Если выбран товар - показываем детальный экран
    if (selectedProduct != null) {
        ProductDetailScreen(
            product = selectedProduct!!,
            modifier = modifier,
            onBackClick = { selectedProduct = null },
            isLoggedIn = isLoggedIn,
            onLoginRequired = onLoginRequired
        )
    } else {
        // Показываем основной экран с товарами
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(CreamColor)
        ) {
            // Заголовок
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Товары", color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            // Загрузка или ошибка
            if (isLoading && products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFC8744F))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Загрузка товаров...", color = Color.Gray)
                    }
                }
            } else if (errorMessage != null && products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.orders),
                            contentDescription = "Ошибка",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { loadProducts(0) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F))
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            } else {
                // ОБЩИЙ ПРОКРУЧИВАЕМЫЙ КОНТЕНТ
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Сетка товаров
                    items(products.chunked(2)) { rowProducts ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Первый товар в ряду
                            ProductGridCard(
                                product = rowProducts[0],
                                modifier = Modifier.weight(1f),
                                isLoggedIn = isLoggedIn,
                                onLoginRequired = onLoginRequired,
                                onProductClick = { product ->
                                    selectedProduct = product
                                }
                            )

                            // Второй товар в ряду (если есть)
                            if (rowProducts.size > 1) {
                                ProductGridCard(
                                    product = rowProducts[1],
                                    modifier = Modifier.weight(1f),
                                    isLoggedIn = isLoggedIn,
                                    onLoginRequired = onLoginRequired,
                                    onProductClick = { product ->
                                        selectedProduct = product
                                    }
                                )
                            } else {
                                // Пустое место, если товаров нечетное количество
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Индикатор загрузки следующей страницы
                    if (hasMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color(0xFFC8744F))
                                } else {
                                    // Автоматически загружаем следующую страницу при прокрутке
                                    LaunchedEffect(Unit) {
                                        loadNextPage()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалоги
    if (showReviewSent) {
        SuccessDialog(
            title = "Спасибо за отзыв!",
            message = "Ваш отзыв успешно отправлен.",
            onDismiss = {
                showReviewSent = false
                userReview = ""
                userStars = 0
            }
        )
    }

    if (showReviewLoginRequired) {
        AlertDialog(
            onDismissRequest = { showReviewLoginRequired = false },
            title = {
                Text(
                    text = "Требуется авторизация",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Для оставления отзыва необходимо войти в систему",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReviewLoginRequired = false
                        onLoginRequired()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Войти", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showReviewLoginRequired = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Отмена", color = Color.White)
                }
            }
        )
    }
}
// Менеджер для работы с корзиной
object CartManager {
    private var cartItems = mutableStateListOf<CartItemResponse>()
    var cartTotal by mutableStateOf(0)
    var cartItemCount by mutableStateOf(0)

    fun updateFromResponse(cartResponse: CartResponse?) {
        cartItems.clear()
        cartResponse?.items?.let {
            cartItems.addAll(it)
            cartTotal = cartResponse.grandTotal
            cartItemCount = it.sumOf { item -> item.quantity }
        } ?: run {
            cartTotal = 0
            cartItemCount = 0
        }
    }

    fun getItems(): List<CartItemResponse> = cartItems.toList()

    fun clear() {
        cartItems.clear()
        cartTotal = 0
        cartItemCount = 0
    }

    // Новая функция для добавления элемента (для симуляции)
    fun addItem(productId: Long, productName: String, price: Int, quantity: Int) {
        // В реальном приложении это будет делать API
        // Здесь симулируем добавление
        val existingItem = cartItems.find { it.productId == productId }
        if (existingItem != null) {
            // Если товар уже есть, увеличиваем количество
            val index = cartItems.indexOf(existingItem)
            cartItems[index] = existingItem.copy(
                quantity = existingItem.quantity + quantity,
                totalPrice = existingItem.pricePerItem * (existingItem.quantity + quantity)
            )
        } else {
            // Добавляем новый товар
            cartItems.add(
                CartItemResponse(
                    id = System.currentTimeMillis(), // Временный ID
                    productId = productId,
                    productName = productName,
                    quantity = quantity,
                    pricePerItem = price,
                    totalPrice = price * quantity
                )
            )
        }
        recalculateTotals()
    }

    private fun recalculateTotals() {
        cartTotal = cartItems.sumOf { it.totalPrice }
        cartItemCount = cartItems.sumOf { it.quantity }
    }
}

fun extractSizeFromName(name: String): Pair<Int?, Int?>? {
    // Ищем паттерн типа "20x150" в названии
    val pattern = Regex("""(\d+)[xX×](\d+)""")
    val match = pattern.find(name)

    return if (match != null && match.groupValues.size == 3) {
        val width = match.groupValues[1].toIntOrNull()
        val height = match.groupValues[2].toIntOrNull()
        if (width != null && height != null) Pair(width, height) else null
    } else {
        null
    }
}

fun getCleanProductName(name: String): String {
    // Удаляем размеры из названия
    val pattern = Regex("""\s*\d+[xX×]\d+\s*""")
    return pattern.replace(name, "").trim()
}

@Composable
fun ProductGridCard(
    product: Product,
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    onLoginRequired: () -> Unit = {},
    onProductClick: (Product) -> Unit = {} // Добавляем обработчик клика на карточку
) {
    var showQuantityDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var productImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showLoginRequiredDialog by remember { mutableStateOf(false) }

    val cleanName = getCleanProductName(product.name)
    val sizeFromName = extractSizeFromName(product.name)

    // Используем размеры из полей product или из названия
    val displayHeight = product.height ?: sizeFromName?.second
    val displayWidth = product.width ?: sizeFromName?.first

    // Функция добавления в корзину
    fun addToCart(quantity: Int) {
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val response = RetrofitClient.instance.addToCart(
                    CartItemRequest(product.id, quantity)
                )
                CartManager.updateFromResponse(response)
            } catch (e: Exception) {
                errorMessage = "Ошибка добавления в корзину: ${e.message}"
                println("Add to cart error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(product.id) {
        if (!isLoadingImage) {
            isLoadingImage = true
            try {
                val response = RetrofitClient.instance.getProductImages(product.id)
                val imageBitmap = ImageUtils.base64ToImageBitmap(response.image)
                productImage = imageBitmap
            } catch (e: Exception) {
                println("Error loading product image: ${e.message}")
                println("Full error: ${e.stackTraceToString()}")
            } finally {
                isLoadingImage = false
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clickable {
                onProductClick(product) // Обрабатываем клик на всю карточку
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4C6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Изображение
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            ) {
                if (productImage != null) {
                    Image(
                        bitmap = productImage!!,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = product.image),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Название товара
            Text(
                text = cleanName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Размеры (если есть)
            if (displayHeight != null && displayWidth != null) {
                Text(
                    text = "${displayWidth}×${displayHeight} см",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Цена
            Text(
                text = "${product.price} ₽",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFC8744F),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Информация о наличии
            if (product.stockQuantity > 0) {
                Text(
                    text = "✓ В наличии",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "✗ Нет в наличии",
                    fontSize = 12.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            // Показать ошибку, если есть
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Диалоги
    if (showLoginRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showLoginRequiredDialog = false },
            title = {
                Text(
                    text = "Требуется авторизация",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Для добавления товара в корзину необходимо войти в систему",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginRequiredDialog = false
                        onLoginRequired()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F))
                ) {
                    Text("Войти", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLoginRequiredDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Отмена", color = Color.White)
                }
            }
        )
    }

    if (showQuantityDialog) {
        QuantityDialog(
            productName = product.name,
            maxQuantity = product.stockQuantity,
            onDismiss = { showQuantityDialog = false },
            onConfirm = { quantity ->
                addToCart(quantity)
                showQuantityDialog = false
            }
        )
    }
}

@Composable
fun ProductDetailScreen(
    product: Product,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    isLoggedIn: Boolean = false,
    onLoginRequired: () -> Unit = {},
    onAddToCart: (Long, Int) -> Unit = { _, _ -> }
) {
    var showQuantityDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var productImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showLoginRequiredDialog by remember { mutableStateOf(false) }

    // Состояния для отзывов
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoadingReviews by remember { mutableStateOf(false) }
    var reviewError by remember { mutableStateOf<String?>(null) }
    var currentReviewPage by remember { mutableStateOf(0) }
    var hasMoreReviews by remember { mutableStateOf(true) }

    // Состояния для написания отзыва
    var showReviewDialog by remember { mutableStateOf(false) }
    var userReviewText by remember { mutableStateOf("") }
    var userReviewRating by remember { mutableStateOf(5) }
    var isSubmittingReview by remember { mutableStateOf(false) }
    var showReviewSuccess by remember { mutableStateOf(false) }

    // Функция добавления в корзину
    fun addToCart(quantity: Int) {
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val response = RetrofitClient.instance.addToCart(
                    CartItemRequest(product.id, quantity)
                )
                CartManager.updateFromResponse(response)
            } catch (e: Exception) {
                errorMessage = "Ошибка добавления в корзину: ${e.message}"
                println("Add to cart error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Функция загрузки отзывов
    fun loadReviews(page: Int = 0) {
        coroutineScope.launch {
            try {
                isLoadingReviews = true
                val response = RetrofitClient.instance.getProductReviews(
                    productId = product.id,
                    page = page,
                    size = 5
                )

                if (page == 0) {
                    reviews = response.content
                } else {
                    reviews = reviews + response.content
                }

                hasMoreReviews = !response.last
                currentReviewPage = page
                reviewError = null
            } catch (e: Exception) {
                reviewError = "Ошибка загрузки отзывов: ${e.message}"
                println("Error loading reviews: ${e.message}")
            } finally {
                isLoadingReviews = false
            }
        }
    }

    // Загружаем изображение продукта
    LaunchedEffect(product.id) {
        if (!isLoadingImage) {
            isLoadingImage = true
            try {
                val response = RetrofitClient.instance.getProductImages(product.id)
                val imageBitmap = ImageUtils.base64ToImageBitmap(response.image)
                productImage = imageBitmap
            } catch (e: Exception) {
                println("Error loading product image: ${e.message}")
            } finally {
                isLoadingImage = false
            }
        }

        // Загружаем отзывы
        loadReviews(0)
    }

    // Используем Box для правильного позиционирования
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Кнопка назад и заголовок
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Товар",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Используем LazyColumn для всего контента вместо Column с verticalScroll
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Изображение товара
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(horizontal = 16.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                    ) {
                        if (productImage != null) {
                            Image(
                                bitmap = productImage!!,
                                contentDescription = product.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = product.image),
                                contentDescription = product.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Название товара
                    Text(
                        text = product.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Цена
                    Text(
                        text = "${product.price} ₽",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC8744F),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Параметры
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Параметры",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Собираем параметры для отображения
                        val parameters = mutableListOf<Pair<String, String?>>()

                        if (product.width != null) {
                            parameters.add("Ширина" to "${product.width} см")
                        }
                        if (product.height != null) {
                            parameters.add("Высота" to "${product.height} см")
                        }
                        if (!product.color.isNullOrEmpty()) {
                            parameters.add("Цвет" to product.color)
                        }

                        // Отображаем параметры по 2 в ряд
                        parameters.chunked(2).forEach { rowParams ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ParameterItem(
                                    title = rowParams[0].first,
                                    value = rowParams[0].second ?: "",
                                    modifier = Modifier.weight(1f)
                                )

                                if (rowParams.size > 1) {
                                    ParameterItem(
                                        title = rowParams[1].first,
                                        value = rowParams[1].second ?: "",
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Описание
                if (product.description.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "Описание",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = product.description,
                                fontSize = 16.sp,
                                color = Color.Gray,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Информация о наличии
                item {
                    Text(
                        text = if (product.stockQuantity > 0) "✓ В наличии" else "✗ Нет в наличии",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (product.stockQuantity > 0) Color(0xFF4CAF50) else Color.Red,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Кнопка "В корзину"
                    Button(
                        onClick = {
                            if (product.stockQuantity > 0) {
                                if (isLoggedIn) {
                                    showQuantityDialog = true
                                } else {
                                    showLoginRequiredDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (product.stockQuantity > 0) Color(0xFFC8744F) else Color.Gray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        enabled = product.stockQuantity > 0 && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                if (product.stockQuantity > 0) "Добавить в корзину" else "Нет в наличии",
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Блок отзывов
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Отзывы",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            // Кнопка для написания отзыва
                            if (isLoggedIn) {
                                Button(
                                    onClick = { showReviewDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFC8744F).copy(alpha = 0.1f),
                                        contentColor = Color(0xFFC8744F)
                                    ),
                                    elevation = null
                                ) {
                                    Text("Написать отзыв")
                                }
                            } else {
                                TextButton(
                                    onClick = onLoginRequired
                                ) {
                                    Text("Войдите, чтобы оставить отзыв")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Состояние загрузки отзывов
                if (isLoadingReviews && reviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFC8744F),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else if (reviewError != null && reviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = reviewError!!,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else if (reviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Пока нет отзывов. Будьте первым!",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    // Список отзывов
                    items(reviews) { review ->
                        ReviewItem(
                            review = review,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Кнопка "Показать еще" если есть еще отзывы
                    if (hasMoreReviews) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingReviews) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFC8744F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Button(
                                        onClick = { loadReviews(currentReviewPage + 1) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFC8744F).copy(alpha = 0.1f),
                                            contentColor = Color(0xFFC8744F)
                                        ),
                                        elevation = null
                                    ) {
                                        Text("Показать еще")
                                    }
                                }
                            }
                        }
                    }
                }

                // Дополнительный отступ в конце
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Диалоги
    if (showLoginRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showLoginRequiredDialog = false },
            title = {
                Text(
                    text = "Требуется авторизация",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Для добавления товара в корзину необходимо войти в систему",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginRequiredDialog = false
                        onLoginRequired()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F))
                ) {
                    Text("Войти", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLoginRequiredDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Отмена", color = Color.White)
                }
            }
        )
    }

    if (showQuantityDialog) {
        QuantityDialog(
            productName = product.name,
            maxQuantity = product.stockQuantity,
            onDismiss = { showQuantityDialog = false },
            onConfirm = { quantity ->
                addToCart(quantity)
                showQuantityDialog = false
            }
        )
    }

    // Диалог написания отзыва
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmittingReview) showReviewDialog = false },
            title = {
                Text(
                    text = "Написать отзыв",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    // Рейтинг звездами
                    Text(
                        text = "Ваша оценка:",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Оценка $i",
                                tint = if (i <= userReviewRating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { userReviewRating = i }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Текст отзыва
                    OutlinedTextField(
                        value = userReviewText,
                        onValueChange = { userReviewText = it },
                        label = { Text("Ваш отзыв") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5,
                        minLines = 3,
                        isError = userReviewText.isBlank() && isSubmittingReview
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Лямбда для отправки отзыва
                        if (userReviewText.isNotBlank() && userReviewRating > 0) {
                            isSubmittingReview = true
                            coroutineScope.launch {
                                try {
                                    RetrofitClient.instance.createReview(
                                        CreateReviewRequest(
                                            productId = product.id,
                                            text = userReviewText,
                                            rating = userReviewRating
                                        )
                                    )

                                    // Обновляем список отзывов
                                    loadReviews(0)
                                    showReviewDialog = false
                                    userReviewText = ""
                                    userReviewRating = 5
                                    showReviewSuccess = true
                                } catch (e: Exception) {
                                    reviewError = "Ошибка отправки отзыва: ${e.message}"
                                    println("Error submitting review: ${e.message}")
                                } finally {
                                    isSubmittingReview = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F)),
                    enabled = userReviewText.isNotBlank() && userReviewRating > 0 && !isSubmittingReview
                ) {
                    if (isSubmittingReview) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Отправить")
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = { showReviewDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    enabled = !isSubmittingReview
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог успешной отправки отзыва
    if (showReviewSuccess) {
        AlertDialog(
            onDismissRequest = { showReviewSuccess = false },
            title = {
                Text(
                    text = "Спасибо!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text("Ваш отзыв успешно отправлен")
            },
            confirmButton = {
                Button(
                    onClick = { showReviewSuccess = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F))
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ReviewItem(
    review: Review,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Заголовок с именем и датой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = review.username,  // Теперь это строка
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Text(
                    text = formatReviewDate(review.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Рейтинг звездами
            Row {
                repeat(review.rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Звезда",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                }
                repeat(5 - review.rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Пустая звезда",
                        tint = Color(0xFFE0E0E0),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Текст отзыва
            Text(
                text = review.text,
                color = Color.Black,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}

fun formatReviewDate(dateString: String): String {
    return try {
        // Пробуем разные форматы дат
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        )

        var date: java.util.Date? = null
        for (format in formats) {
            try {
                date = format.parse(dateString)
                break
            } catch (e: Exception) {
                continue
            }
        }

        if (date != null) {
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

// Компонент для отображения одного параметра
@Composable
fun ParameterItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Серое название параметра
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Значение черным цветом
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun QuantityDialog(
    productName: String,
    maxQuantity: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите количество") },
        text = {
            Column {
                Text("Товар: $productName", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        enabled = quantity > 1
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Уменьшить")
                    }

                    Text(
                        text = "$quantity",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { if (quantity < maxQuantity) quantity++ },
                        enabled = quantity < maxQuantity
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Увеличить")
                    }
                }
                Text(
                    "Доступно: $maxQuantity шт.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(quantity) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F))
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Отмена")
            }
        }
    )
}
@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    onBackClick: () -> Unit = {},
    onLoginRequired: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var shippingAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val cartItems = CartManager.getItems()
    val cartTotal = CartManager.cartTotal



    // Функция загрузки корзины
    fun loadCart() {
        if (!isLoggedIn) return

        coroutineScope.launch {
            try {
                isLoading = true
                val cartResponse = RetrofitClient.instance.getCart()
                CartManager.updateFromResponse(cartResponse)
            } catch (e: Exception) {
                println("Cart loading error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    // Загружаем корзину при открытии экрана
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            loadCart()
        }
    }
    // Функция создания заказа
    fun createOrder(address: String, phone: String) {
        coroutineScope.launch {
            try {
                isLoading = true
                val orderResponse = RetrofitClient.instance.createOrderFromCart(
                    CreateOrderRequest(address, phone)
                )

                // Обработка успешного оформления заказа
                CartManager.clear()

                // Если есть confirmationUrl, можно открыть в браузере
                if (orderResponse.payment.confirmationUrl.isNotEmpty()) {
                    // Открыть URL для оплаты
                }

            } catch (e: Exception) {
                println("Order creation error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamColor)
    ) {
        // Заголовок с кнопкой назад
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Корзина",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFC8744F))
            }
        } else if (!isLoggedIn) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.cart),
                        contentDescription = "Корзина",
                        modifier = Modifier.size(120.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Для просмотра корзины",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Text(
                        "необходимо авторизоваться",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                }
            }
        } else if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.orders),
                        contentDescription = "Корзина пуста",
                        modifier = Modifier.size(120.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Корзина пуста",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(cartItems) { item ->
                    CartItemCard(item)
                }
            }

            // Итого и кнопка оформления
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Итого:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${cartTotal} ₽", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCheckoutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F))
                    ) {
                        Text("Оформить заказ")
                    }
                }
            }
        }
    }

    // Диалог оформления заказа
    if (showCheckoutDialog) {
        CheckoutDialog(
            onDismiss = { showCheckoutDialog = false },
            onConfirm = { address, phone ->
                // Логика оформления заказа
                createOrder(address, phone)
                showCheckoutDialog = false
            }
        )
    }
}

@Composable
fun CartItemCard(item: CartItemResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.Bold)
                Text("${item.pricePerItem} ₽ × ${item.quantity} шт.")
                Text("Итого: ${item.totalPrice} ₽", color = Color(0xFFC8744F))
            }
            IconButton(onClick = {
                // TODO: Реализовать удаление из корзины
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Оформление заказа") },
        text = {
            Column {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Адрес доставки") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Номер телефона") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(address, phone) },
                enabled = address.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F))
            ) {
                Text("Оформить")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Отмена")
            }
        }
    )
}
fun isValidEmail(email: String): Boolean {
    val emailRegex = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
    )
    return emailRegex.matcher(email).matches()
}

fun isValidName(name: String): Boolean {
    val nameRegex = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ\\s-]+\$")
    return nameRegex.matcher(name).matches() && name.length >= 2
}

fun isValidUsername(username: String): Boolean {
    val usernameRegex = Pattern.compile("^[a-zA-Z0-9_]+\$")
    return usernameRegex.matcher(username).matches() && username.length >= 3
}

fun isValidPhone(phone: String): Boolean {
    val phoneRegex = Pattern.compile("^[+0-9\\s-()]+\$")
    return phoneRegex.matcher(phone).matches() && phone.length >= 5
}

//@Composable
//fun ParameterBlock(title: String, value: String, backgroundColor: Color, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onClick() },
//        elevation = CardDefaults.cardElevation(2.dp),
//        colors = CardDefaults.cardColors(containerColor = backgroundColor)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(backgroundColor) // ← ДОБАВЬТЕ ЭТУ СТРОКУ
//                .padding(12.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(title, color = Color.Black)
//            Box(
//                modifier = Modifier
//                    .background(backgroundColor) // ← ДОБАВЬТЕ ЭТУ СТРОКУ
//                    .padding(horizontal = 8.dp, vertical = 4.dp)
//            ) {
//                Text(value, color = Color.Black)
//            }
//        }
//    }
//}
//
//@Composable
//fun ColorChoiceDialog(
//    title: String,
//    colors: List<Pair<String, Color>>,
//    onDismiss: () -> Unit,
//    onColorSelected: (String) -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
//        text = {
//            Column {
//                colors.forEach { (colorName, colorValue) ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { onColorSelected(colorName) }
//                            .padding(vertical = 12.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        // Цветной квадратик
//                        Box(
//                            modifier = Modifier
//                                .size(40.dp)
//                                .background(colorValue, RoundedCornerShape(8.dp))
//                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
//                        )
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Text(
//                            text = colorName,
//                            fontSize = 16.sp,
//                            modifier = Modifier.weight(1f)
//                        )
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = onDismiss,
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
//            ) {
//                Text("Отмена")
//            }
//        }
//    )
//}
//
//@Composable
//fun ChoiceDialog(title: String, options: List<Pair<Any,String>>, onDismiss: () -> Unit, onSelect: (Any) -> Unit) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(title, fontWeight = FontWeight.Bold) },
//        text = {
//            Column {
//                options.forEach { (value,label) ->
//                    Text(
//                        label,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { onSelect(value) }
//                            .padding(8.dp)
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = onDismiss,
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
//            ) {
//                Text("Отмена")
//            }
//        }
//    )
//}
//
//@Composable
//fun SliderDialog(title: String, current: Float, min: Float, max: Float, step: Float, onConfirm: (Float) -> Unit) {
//    var sliderValue by remember { mutableStateOf(current) }
//    AlertDialog(
//        onDismissRequest = { onConfirm(current) },
//        title = { Text(title, fontWeight = FontWeight.Bold) },
//        text = {
//            Column {
//                Text("${sliderValue.toInt()} см")
//                Slider(value = sliderValue, onValueChange = { sliderValue = it }, valueRange = min..max, steps = ((max-min)/step-1).toInt())
//            }
//        },
//        confirmButton = {
//            Button(onClick = { onConfirm(sliderValue) }) {
//                Text("OK")
//            }
//        }
//    )
//}

@Composable
fun SuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text("OK", color = Color.White)
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .wrapContentHeight()
    )
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
    onLoginSuccess: (username: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFE4C6))
    ) {
        BackButton(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color(0xFFF6E8D7), RoundedCornerShape(16.dp))
                .padding(24.dp)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Авторизация",
                fontSize = 24.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Имя пользователя") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                isError = errorMessage != null
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = errorMessage != null
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val response = RetrofitClient.instance.login(
                                    LoginRequest(username, password)
                                )

                                // Проверяем, что это не сообщение об ошибке
                                if (response.contains("Bad credentials", ignoreCase = true) ||
                                    response.contains("error", ignoreCase = true) ||
                                    response.contains("403", ignoreCase = true) ||
                                    response.contains("forbidden", ignoreCase = true) ||
                                    response.contains("unauthorized", ignoreCase = true) ||
                                    response.length < 20 // JWT токен обычно длинный (>20 символов)
                                ) {
                                    errorMessage = "Неверный логин или пароль"
                                    isLoading = false
                                    return@launch
                                }

                                TokenManager.token = response
                                isLoading = false
                                onLoginSuccess(username)
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = when {
                                    e is java.net.ConnectException -> "Не удалось подключиться к серверу"
                                    e is java.net.SocketTimeoutException -> "Таймаут подключения"
                                    e is retrofit2.HttpException -> {
                                        when (e.code()) {
                                            403, 401 -> "Неверный логин или пароль"
                                            500 -> "Ошибка сервера (500). Попробуйте позже"
                                            400 -> "Неверные данные"
                                            else -> "Ошибка авторизации"
                                        }
                                    }
                                    e.message?.contains("Unable to resolve host") == true -> "Проблемы с интернет-соединением"
                                    else -> "Неверный логин или пароль"
                                }
                                println("Login error: ${e.message}")
                            }
                        }
                    } else {
                        errorMessage = "Заполните все поля"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Войти", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Нет аккаунта? Зарегистрироваться",
                color = Color.DarkGray,
                modifier = Modifier.clickable { onRegisterClick() },
                textAlign = TextAlign.Center
            )
        }
    }
}

// Обновите RegisterScreen (уберите проверку существующих пользователей)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Валидационные состояния
    var usernameError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun validateAll(): Boolean {
        var isValid = true

        // Валидация имени пользователя
        usernameError = when {
            username.isBlank() -> "Обязательное поле"
            !isValidUsername(username) -> "Только буквы, цифры и подчеркивание"
            username.length < 3 -> "Минимум 3 символа"
            else -> null
        }
        if (usernameError != null) isValid = false

        // Валидация имени
        firstNameError = when {
            firstName.isBlank() -> "Обязательное поле"
            !isValidName(firstName) -> "Только буквы и дефисы"
            firstName.length < 2 -> "Минимум 2 символа"
            else -> null
        }
        if (firstNameError != null) isValid = false

        // Валидация фамилии
        lastNameError = when {
            lastName.isBlank() -> "Обязательное поле"
            !isValidName(lastName) -> "Только буквы и дефисы"
            lastName.length < 2 -> "Минимум 2 символа"
            else -> null
        }
        if (lastNameError != null) isValid = false

        // Валидация email
        emailError = when {
            email.isBlank() -> "Обязательное поле"
            !isValidEmail(email) -> "Неверный формат email"
            else -> null
        }
        if (emailError != null) isValid = false

        // Валидация телефона
        phoneError = when {
            phoneNumber.isBlank() -> "Обязательное поле"
            !isValidPhone(phoneNumber) -> "Только цифры и символы +-()"
            phoneNumber.length < 5 -> "Минимум 5 символов"
            else -> null
        }
        if (phoneError != null) isValid = false

        // Валидация пароля
        passwordError = when {
            password.isBlank() -> "Обязательное поле"
            password.length < 6 -> "Минимум 6 символов"
            else -> null
        }
        if (passwordError != null) isValid = false

        return isValid
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFE4C6))
    ) {
        BackButton(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color(0xFFF6E8D7), RoundedCornerShape(16.dp))
                .padding(24.dp)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Регистрация",
                fontSize = 24.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Поле имени пользователя
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = null
                },
                label = { Text("Имя пользователя") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                isError = usernameError != null,
                supportingText = {
                    usernameError?.let { error ->
                        Text(text = error, color = Color.Red)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Поле имени
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    firstNameError = null
                },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                isError = firstNameError != null,
                supportingText = {
                    firstNameError?.let { error ->
                        Text(text = error, color = Color.Red)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Поле фамилии
            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    lastNameError = null
                },
                label = { Text("Фамилия") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                isError = lastNameError != null,
                supportingText = {
                    lastNameError?.let { error ->
                        Text(text = error, color = Color.Red)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Поле email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = emailError != null,
                supportingText = {
                    emailError?.let { error ->
                        Text(text = error, color = Color.Red)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Поле телефона
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    phoneError = null
                },
                label = { Text("Номер телефона") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                isError = phoneError != null,
                supportingText = {
                    phoneError?.let { error ->
                        Text(text = error, color = Color.Red)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Поле пароля
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = passwordError != null,
                supportingText = {
                    passwordError?.let { error ->
                        Text(text = error, color = Color.Red)
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (validateAll()) {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val token = RetrofitClient.instance.register(
                                    RegisterRequest(
                                        username = username,
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        phoneNumber = phoneNumber,
                                        password = password
                                    )
                                )
                                TokenManager.token = token
                                isLoading = false
                                onRegisterSuccess()
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = when {
                                    e is java.net.ConnectException -> "Не удалось подключиться к серверу"
                                    e is java.net.SocketTimeoutException -> "Таймаут подключения"
                                    e is retrofit2.HttpException -> {
                                        when (e.code()) {
                                            403 -> "Доступ запрещен. Проверьте данные или обратитесь к администратору"
                                            500 -> "Ошибка сервера (500). Попробуйте позже"
                                            400 -> "Неверные данные"
                                            else -> "Ошибка HTTP: ${e.code()}"
                                        }
                                    }
                                    else -> "Ошибка: ${e.message ?: "Неизвестная ошибка"}"
                                }
                                println("Registration error: ${e.stackTraceToString()}")
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Создать аккаунт", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Уже есть аккаунт? Войти",
                color = Color.DarkGray,
                modifier = Modifier.clickable { onLoginClick() },
                textAlign = TextAlign.Center
            )
        }
    }
}


data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val image: Int,
    val price: Int,
    val stockQuantity: Int,
    val height: Int? = null, // Добавьте это поле
    val width: Int? = null,  // Добавьте это поле
    val color: String? = null, // Добавляем цвет
    val averageRating: Double? = null // Добавляем рейтинг
)