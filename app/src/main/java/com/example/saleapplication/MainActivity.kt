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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

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


// API Service interface - измените возвращаемые типы
interface ApiService {
    @POST("api/v1/users/register")
    suspend fun register(@Body request: RegisterRequest): String

    @POST("api/v1/users/login")
    suspend fun login(@Body request: LoginRequest): String
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
            val request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
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
    var showLogoutDialog by remember { mutableStateOf(false) } // Добавьте это состояние

    // Состояние для имени пользователя
    var currentUserName by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            if (currentScreen == "main" || currentScreen == "constructor" || currentScreen == "orders") {
                NavigationBar(
                    containerColor = Color(0xE8B5982B).copy(alpha = 0.17f)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "constructor",
                        onClick = {
                            currentScreen = "constructor"
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.pencil),
                                contentDescription = "Конструктор"
                            )
                        },
                        label = { Text("Конструктор") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "main",
                        onClick = {

                            currentScreen = "main"
                        },
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
                        selected = currentScreen == "orders" || currentScreen == "login",
                        onClick = {
                            if (isLoggedIn) {
                                currentScreen = "orders"
                            } else {
                                currentScreen = "login"
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = if (isLoggedIn) R.drawable.orders else R.drawable.out),
                                contentDescription = if (isLoggedIn) "Мои заказы" else "Вход"
                            )
                        },
                        label = { Text(if (isLoggedIn) "Заказы" else "Вход") }
                    )
                    if (isLoggedIn) {
                        NavigationBarItem(
                            selected = false,
                            onClick = {
                                showLogoutDialog = true // Показываем диалог вместо выхода
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
                onRegisterSuccess = {
                    currentScreen = "login"
                }
            )
            "constructor" -> ConstructorScreen(
                modifier = Modifier.padding(paddingValues),
                isLoggedIn = isLoggedIn,
                currentUserName = currentUserName,
                onLoginRequired = { currentScreen = "login" }
            )
            "orders" -> OrdersScreen(Modifier.padding(paddingValues))
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
fun OrdersScreen(modifier: Modifier = Modifier) {
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
        Spacer(modifier = Modifier.height(32.dp))

        // Пока пустой экран
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.orders),
                    contentDescription = "Нет заказов",
                    modifier = Modifier.size(120.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "У вас пока нет заказов",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Создайте свой первый заказ в конструкторе",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
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
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) {
                        currentIndex = if (currentIndex > 0) currentIndex - 1 else images.lastIndex
                    } else if (dragAmount < -50) {
                        currentIndex = if (currentIndex < images.lastIndex) currentIndex + 1 else 0
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = images[currentIndex]),
            contentDescription = "Слайд",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun ProductList() {
    val products = listOf(
        Product("Серый пластик", "Описание: прочный, лёгкий материал", R.drawable.product1),
        Product("Антрацит пластик", "Описание: современный тёмный оттенок", R.drawable.product2)
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
            .padding(8.dp)
            .fillMaxWidth(),
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
fun ConstructorScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    currentUserName: String = "",
    onLoginRequired: () -> Unit = {}
) {
    var scale by remember { mutableStateOf(1f) }
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var selectedMaterial by remember { mutableStateOf<String?>(null) }
    var width by remember { mutableStateOf(100f) }
    var height by remember { mutableStateOf(150f) }
    var showOrderSuccessDialog by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }

    // Цвета с их реальными значениями
    val colors = listOf(
        "Зеленый" to Color(0xFF4CAF50),
        "Красный" to Color(0xFFF44336),
        "Синий" to Color(0xFF2196F3),
        "Коричневый" to Color(0xFF795548)
    )
    val materials = listOf("Пластик", "Алюминий", "Дерево", "Ткань")

    // Диалоги выбора
    var showColorDialog by remember { mutableStateOf(false) }
    var showMaterialDialog by remember { mutableStateOf(false) }
    var showWidthDialog by remember { mutableStateOf(false) }
    var showHeightDialog by remember { mutableStateOf(false) }

    // Для отзывов
    data class Review(val name: String, val text: String, val stars: Int, val date: String)
    var reviews by remember { mutableStateOf(
        mutableListOf(
            Review("Иван", "Отличная штора, всем доволен!", 5, "08.11.2025"),
            Review("Мария", "Цвет совпал с описанием, рекомендую.", 4, "07.11.2025"),
            Review("Алексей", "Быстрая доставка и хорошее качество.", 5, "06.11.2025")
        )
    ) }
    var userReview by remember { mutableStateOf("") }
    var userStars by remember { mutableStateOf(0) }
    var showReviewSent by remember { mutableStateOf(false) }
    var showReviewLoginRequired by remember { mutableStateOf(false) }

    // Цвет с прозрачностью 66%
    val customColor = Color(0xFFE8B598)

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Конструктор", color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Создайте свою умную штору", fontSize = 16.sp, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.sample1),
            contentDescription = "Штора",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    clip = false
                ),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { scale = (scale + 0.1f).coerceAtMost(2f) },
                colors = ButtonDefaults.buttonColors(containerColor = customColor)
            ) { Text("+") }
            Button(
                onClick = { scale = (scale - 0.1f).coerceAtLeast(0.5f) },
                colors = ButtonDefaults.buttonColors(containerColor = customColor)
            ) { Text("-") }
        }

        Spacer(modifier = Modifier.height(24.dp))
        // --- Параметры через диалоги ---
        ParameterBlock("Цвет", selectedColor ?: "Не выбран", customColor) {
            showColorDialog = true
        }
        ParameterBlock("Материал", selectedMaterial ?: "Не выбран", customColor) {
            showMaterialDialog = true
        }
        ParameterBlock("Ширина", "${width.toInt()} см", customColor) {
            showWidthDialog = true
        }
        ParameterBlock("Длина", "${height.toInt()} см", customColor) {
            showHeightDialog = true
        }
        ParameterBlock("Дополнительно", "Не выбрано", customColor) {
            /* можно расширить в будущем */
        }

        Spacer(modifier = Modifier.height(32.dp))
        val totalPrice = remember(width, height, selectedMaterial) {
            (width * height / 1000 + when (selectedMaterial) {
                "Алюминий" -> 200
                "Дерево" -> 300
                "Ткань" -> 150
                else -> 100
            }).toInt()
        }
        Text("Итоговая стоимость: $totalPrice ₽", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (isLoggedIn) {
                    showOrderSuccessDialog = true
                } else {
                    showLoginRequiredDialog = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Оформить заказ", color = Color.White, fontSize = 18.sp)
        }

        // --- Блок отзывов ---
        Spacer(modifier = Modifier.height(32.dp))
        Text("Отзывы", fontSize = 22.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        reviews.forEach { review ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color(0x2BE8B598), RoundedCornerShape(8.dp))
                    .border(2.dp, Color(0xFFE8B598), RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(review.name, color = Color.Black, fontWeight = FontWeight.Bold)
                    Row {
                        repeat(review.stars) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        repeat(5 - review.stars) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFE0E0E0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(review.date, fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(review.text, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Оставить отзыв", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        if (!isLoggedIn) {
            Text(
                text = "Для оставления отзыва необходимо войти в систему",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = { showReviewLoginRequired = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8744F))
            ) {
                Text("Войти для отзыва", color = Color.White)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Рейтинг",
                        tint = if (i <= userStars) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { userStars = i }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box {
                OutlinedTextField(
                    value = userReview,
                    onValueChange = { userReview = it },
                    placeholder = { Text("Напишите ваш отзыв") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Button(
                    onClick = {
                        if (userReview.isNotBlank() && userStars > 0) {
                            reviews.add(0, Review(currentUserName, userReview, userStars, "08.11.2025"))
                            showReviewSent = true
                            userReview = ""
                            userStars = 0
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8B598))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Отправить",
                        tint = Color(0xFFC8744F)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Диалог требования авторизации для отзыва
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

    // Диалог требования авторизации для заказа
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
                    text = "Для оформления заказа необходимо войти в систему",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginRequiredDialog = false
                        onLoginRequired()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
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
    // --- Диалоги выбора ---
    if (showColorDialog) {
        ColorChoiceDialog(
            title = "Выберите цвет",
            colors = colors,
            onDismiss = { showColorDialog = false },
            onColorSelected = { colorName ->
                selectedColor = colorName
                showColorDialog = false
            }
        )
    }

    // Добавьте этот диалог в блок диалогов (рядом с другими диалогами)
    if (showOrderSuccessDialog) {
        SuccessDialog(
            title = "Заказ оформлен!",
            message = "Ваш заказ успешно оформлен. С вами свяжутся для уточнения деталей.",
            onDismiss = {
                showOrderSuccessDialog = false
            }
        )
    }

    if (showMaterialDialog) {
        ChoiceDialog(
            title = "Выберите материал",
            onDismiss = { showMaterialDialog = false },
            options = materials.map { it to it }
        ) { selection ->
            selectedMaterial = selection as String
            showMaterialDialog = false
        }
    }
    if (showWidthDialog) {
        SliderDialog("Выберите ширину", width, 50f, 300f, 10f) { newValue ->
            width = newValue
            showWidthDialog = false
        }
    }
    if (showHeightDialog) {
        SliderDialog("Выберите длину", height, 50f, 300f, 10f) { newValue ->
            height = newValue
            showHeightDialog = false
        }
    }
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

@Composable
fun ParameterBlock(title: String, value: String, backgroundColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor) // ← ДОБАВЬТЕ ЭТУ СТРОКУ
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color.Black)
            Box(
                modifier = Modifier
                    .background(backgroundColor) // ← ДОБАВЬТЕ ЭТУ СТРОКУ
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(value, color = Color.Black)
            }
        }
    }
}

@Composable
fun ColorChoiceDialog(
    title: String,
    colors: List<Pair<String, Color>>,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                colors.forEach { (colorName, colorValue) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onColorSelected(colorName) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Цветной квадратик
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(colorValue, RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = colorName,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
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
fun ChoiceDialog(title: String, options: List<Pair<Any,String>>, onDismiss: () -> Unit, onSelect: (Any) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { (value,label) ->
                    Text(
                        label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                            .padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
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
fun SliderDialog(title: String, current: Float, min: Float, max: Float, step: Float, onConfirm: (Float) -> Unit) {
    var sliderValue by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = { onConfirm(current) },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("${sliderValue.toInt()} см")
                Slider(value = sliderValue, onValueChange = { sliderValue = it }, valueRange = min..max, steps = ((max-min)/step-1).toInt())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(sliderValue) }) {
                Text("OK")
            }
        }
    )
}

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


data class Product(val name: String, val description: String, val image: Int)