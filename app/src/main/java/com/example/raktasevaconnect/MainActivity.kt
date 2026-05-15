package com.example.raktasevaconnect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.LocalTextStyle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.annotation.SuppressLint
import androidx.core.content.ContextCompat

import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import coil.compose.AsyncImage

import com.example.raktasevaconnect.ui.theme.RaktaSevaConnectTheme

import com.google.android.gms.location.LocationServices

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth

import com.google.firebase.firestore.ktx.firestore

import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth

    // LOCATION PERMISSION

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {

                val fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(this)

                if (
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    try {

                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->

                                if (location != null) {

                                    val latitude = location.latitude
                                    val longitude = location.longitude

                                    println("LATITUDE: $latitude")
                                    println("LONGITUDE: $longitude")
                                }
                            }

                    } catch (e: SecurityException) {

                        e.printStackTrace()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        enableEdgeToEdge()

        requestPermissionLauncher.launch(
            Manifest.permission.ACCESS_FINE_LOCATION
        )


        setContent {

            RaktaSevaConnectTheme {

                AppNavigation()
            }
        }
    }
}

// ---------------- MODEL ----------------

data class BloodRequest(

    val name: String = "",
    val blood: String = "",
    val hospital: String = "",
    val location: String = "",
    val urgency: String = "",
    val time: String = "",

    val contact: String = "",
    val units: String = "",
    val notes: String = "",

    val requestId: String = "",

    val status: String = "ACTIVE",

    val acceptedDonors: List<String> = emptyList(),

    val acceptedDonorPhones: List<String> = emptyList(),

    val acceptedDonorIds: List<String> = emptyList()
)
data class UserData(

    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val bloodGroup: String = "",
    val profileImage: String = ""
)

// ---------------- NAVIGATION ----------------

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,

        startDestination =
            if (Firebase.auth.currentUser != null)
                "home"
            else
                "login"
    ) {

        // LOGIN SCREEN

        composable("login") {
            LoginScreen(navController)
        }

        // REGISTER SCREEN

        composable("register") {
            DonorForm(navController)
        }

        // HOME SCREEN

        composable("home") {
            HomeScreen(navController)
        }

        // REQUEST SCREEN

        composable("request") {
            RequestScreen(navController)
        }

        // REQUESTS LIST

        composable("requests_list") {
            RequestsListScreen()
        }

        // PROFILE SCREEN

        composable("profile") {
            ProfileScreen(navController)
        }
    }
}
// ---------------- DONOR FORM ----------------

@Composable
fun DonorForm(navController: NavController) {

    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var blood by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val bloodGroups =
        listOf(
            "A+", "A-",
            "B+", "B-",
            "O+", "O-",
            "AB+", "AB-"
        )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Rakta-Seva Connect",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Register as a Blood Donor",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(30.dp))

            // NAME

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },

                label = {
                    Text("Full Name")
                },

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // PHONE

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                },

                label = {
                    Text("Phone Number")
                },

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // EMAIL

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                },

                label = {
                    Text("Email")
                },

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // PASSWORD

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },

                label = {
                    Text("Password")
                },

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // BLOOD GROUP

            Text(
                text = "Select Blood Group",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            bloodGroups.chunked(2).forEach { row ->

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    row.forEach { group ->

                        val isSelected =
                            blood == group

                        FilterChip(
                            selected = isSelected,

                            onClick = {
                                blood = group
                            },

                            label = {

                                Row {

                                    Icon(
                                        imageVector =
                                            Icons.Default.Favorite,

                                        contentDescription = null,

                                        tint = Color.Red
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.width(4.dp)
                                    )

                                    Text(group)
                                }
                            },

                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor =
                                        Color(0xFF6A4FB3)
                                )
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // REGISTER BUTTON

            Button(
                onClick = {

                    if (
                        name.isBlank() ||
                        phone.isBlank() ||
                        blood.isBlank() ||
                        email.isBlank() ||
                        password.isBlank()
                    ) {

                        Toast.makeText(
                            context,
                            "Fill all fields!",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        isLoading = true

                        Firebase.auth
                            .createUserWithEmailAndPassword(
                                email,
                                password
                            )

                            .addOnCompleteListener { task ->

                                isLoading = false

                                if (task.isSuccessful) {

                                    val userId =
                                        Firebase.auth.currentUser?.uid

                                    val user = UserData(
                                        name = name,
                                        phone = phone,
                                        bloodGroup = blood,
                                        email = email
                                    )

                                    if (userId != null) {

                                        Firebase.firestore
                                            .collection("users")
                                            .document(userId)
                                            .set(user)
                                    }

                                    Toast.makeText(
                                        context,
                                        "Account Created Successfully 🎉",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.navigate("home") {

                                        popUpTo("register") {
                                            inclusive = true
                                        }
                                    }

                                } else {

                                    Toast.makeText(
                                        context,
                                        task.exception?.message
                                            ?: "Signup Failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),

                shape = RoundedCornerShape(18.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF6A4FB3)
                )
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text = "Register",
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // LOGIN BUTTON

            TextButton(
                onClick = {
                    navController.navigate("login")
                }
            ) {

                Text(
                    text = "Already have an account? Login"
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
// ---------------- LOGIN SCREEN ----------------

@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Welcome Back 👋",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Login to continue",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },

            label = {
                Text("Email")
            },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },

            label = {
                Text("Password")
            },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {

                if (
                    email.isBlank() ||
                    password.isBlank()
                ) {

                    Toast.makeText(
                        context,
                        "Fill all fields",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    isLoading = true

                    Firebase.auth
                        .signInWithEmailAndPassword(
                            email,
                            password
                        )

                        .addOnCompleteListener { task ->

                            isLoading = false

                            if (task.isSuccessful) {

                                Toast.makeText(
                                    context,
                                    "Login Successful 🎉",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.navigate("home") {

                                    popUpTo("login") {
                                        inclusive = true
                                    }
                                }

                            } else {

                                Toast.makeText(
                                    context,
                                    task.exception?.message
                                        ?: "Login Failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),

            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor =
                    Color(0xFF6A4FB3)
            )
        ) {

            if (isLoading) {

                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )

            } else {

                Text(
                    text = "Login",
                    fontSize = 16.sp
                )
            }
        }


        Spacer(modifier = Modifier.height(20.dp))

        TextButton(
            onClick = {
                navController.navigate("register")
            }
        ) {

            Text(
                text = "New User? Register Here"
            )
        }
    }
}
/// ---------------- HOME SCREEN ----------------

@Composable
fun HomeScreen(navController: NavController) {

    @SuppressLint("MissingPermission")
    fun saveUserLocation() {

        val context = navController.context

        val fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(
                    context
                )

        if (

            ContextCompat.checkSelfPermission(

                context,

                Manifest.permission.ACCESS_FINE_LOCATION

            ) == PackageManager.PERMISSION_GRANTED

        ) {

            fusedLocationClient
                .lastLocation

                .addOnSuccessListener { location ->

                    if (location != null) {

                        val userId =
                            Firebase.auth.currentUser?.uid

                        if (userId != null) {

                            Firebase.firestore
                                .collection("users")
                                .document(userId)

                                .update(

                                    mapOf(

                                        "latitude" to location.latitude,

                                        "longitude" to location.longitude
                                    )
                                )
                        }
                    }
                }
        }
    }

    // SAVE FCM TOKEN + LOCATION

    LaunchedEffect(Unit) {

        val userId =
            Firebase.auth.currentUser?.uid

        if (userId != null) {

            Firebase.messaging.token

                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val token = task.result

                        Firebase.firestore
                            .collection("users")
                            .document(userId)

                            .update(
                                "fcmToken",
                                token
                            )
                    }
                }
        }

        // SAVE USER LOCATION

        saveUserLocation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),

        verticalArrangement = Arrangement.Center
    ) {

        // TITLE

        Text(
            text = "🚨 Welcome to\nRakta-Seva",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "You are now registered as a donor 👍",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(45.dp))

        // REQUEST BLOOD BUTTON

        Button(
            onClick = {
                navController.navigate("request")
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6A4FB3)
            ),

            shape = RoundedCornerShape(30.dp)

        ) {

            Text(
                text = "Request Blood 🚑",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // VIEW REQUESTS BUTTON

        OutlinedButton(
            onClick = {
                navController.navigate("requests_list")
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),

            shape = RoundedCornerShape(30.dp)

        ) {

            Text(
                text = "View Current Requests 🩸",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // PROFILE BUTTON

        OutlinedButton(
            onClick = {
                navController.navigate("profile")
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),

            shape = RoundedCornerShape(30.dp)

        ) {

            Text(
                text = "My Profile 👤",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // LOGOUT BUTTON

        Button(
            onClick = {

                Firebase.auth.signOut()

                navController.navigate("login") {

                    popUpTo("home") {
                        inclusive = true
                    }
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),

            shape = RoundedCornerShape(30.dp)

        ) {

            Text(
                text = "Logout",
                fontSize = 16.sp
            )
        }
    }
}
// ---------------- REQUEST SCREEN ----------------

@Composable
fun RequestScreen(navController: NavController) {

    val context = LocalContext.current

    var patientName by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }
    var blood by remember { mutableStateOf("") }

    var contact by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var urgency by remember {
        mutableStateOf("URGENT")
    }

    var units by remember {
        mutableStateOf("1 Unit")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp)
    ) {

        item {

            Text(
                text = "🚨 Emergency Blood Request",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fill the details carefully",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {

                    OutlinedTextField(
                        value = patientName,
                        onValueChange = {
                            patientName = it
                        },

                        label = {
                            Text("Patient Name")
                        },

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = hospital,
                        onValueChange = {
                            hospital = it
                        },

                        label = {
                            Text("Hospital Name")
                        },

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = blood,
                        onValueChange = {
                            blood = it
                        },

                        label = {
                            Text("Blood Group")
                        },

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = contact,
                        onValueChange = {
                            contact = it
                        },

                        label = {
                            Text("Contact Number")
                        },

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = location,
                        onValueChange = {
                            location = it
                        },

                        label = {
                            Text("Location")
                        },

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = {
                            notes = it
                        },

                        label = {
                            Text("Additional Notes")
                        },

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {

                            if (
                                patientName.isBlank() ||
                                hospital.isBlank() ||
                                blood.isBlank() ||
                                contact.isBlank() ||
                                location.isBlank()
                            ) {

                                Toast.makeText(
                                    context,
                                    "Fill all fields",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {

                                isLoading = true

                                val requestId =
                                    Firebase.firestore
                                        .collection("blood_requests")
                                        .document()
                                        .id

                                val request =
                                    BloodRequest(

                                        name = patientName,

                                        blood = blood,

                                        hospital = hospital,

                                        location = location,

                                        urgency = urgency,

                                        time = "Immediate",

                                        contact = contact,

                                        units = units,

                                        notes = notes,

                                        requestId = requestId,

                                        status = "ACTIVE"
                                    )

                                Firebase.firestore
                                    .collection("blood_requests")
                                    .document(requestId)
                                    .set(request)

                                    .addOnSuccessListener {

                                        isLoading = false

                                        Toast.makeText(
                                            context,
                                            "Request Submitted 🚑",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        navController.navigate(
                                            "requests_list"
                                        )
                                    }
                            }
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),

                        shape = RoundedCornerShape(18.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF6A4FB3)
                        )
                    ) {

                        if (isLoading) {

                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )

                        } else {

                            Text(
                                text = "Submit Request",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
// ---------------- REQUESTS LIST SCREEN ----------------

@Composable
fun RequestsListScreen() {

    var requests by remember {
        mutableStateOf(listOf<BloodRequest>())
    }

    // FIRESTORE REALTIME LISTENER

    LaunchedEffect(Unit) {

        Firebase.firestore
            .collection("blood_requests")

            .addSnapshotListener { value, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val requestList =
                    mutableListOf<BloodRequest>()

                value?.documents?.forEach { document ->

                    val request =
                        document.toObject(
                            BloodRequest::class.java
                        )

                    if (request != null) {

                        requestList.add(request)
                    }
                }

                requests = requestList
            }
    }

    // UI

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // TOP HEADER

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6A4FB3))
                .padding(18.dp)
        ) {

            Text(
                text = "Emergency Blood Requests",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // EMPTY STATE

        if (requests.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "No Blood Requests Yet 🚑",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }

        } else {

            // REQUESTS LIST

            LazyColumn {

                items(requests) { request ->

                    RequestCard(request)
                }
            }
        }
    }
}

// ---------------- REQUEST CARD ----------------

@Composable
fun RequestCard(request: BloodRequest) {

    val context = LocalContext.current

    var expanded by remember {
        mutableStateOf(false)
    }

    val urgencyColor = when (request.urgency) {

        "URGENT" -> Color.Red
        "MODERATE" -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),

        shape = RoundedCornerShape(22.dp),

        elevation = CardDefaults.cardElevation(6.dp),

        onClick = {
            expanded = !expanded
        }
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Color.Red,
                            shape = RoundedCornerShape(50.dp)
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = request.blood,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        request.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(request.hospital)
                }

                Text(
                    request.urgency,
                    color = urgencyColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(request.location)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {

                    val gmmIntentUri = Uri.parse(
                        "geo:0,0?q=${request.location}"
                    )

                    val mapIntent = Intent(
                        Intent.ACTION_VIEW,
                        gmmIntentUri
                    )

                    mapIntent.setPackage(
                        "com.google.android.apps.maps"
                    )

                    context.startActivity(mapIntent)
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),

                shape = RoundedCornerShape(14.dp)
            ) {

                Text("Open in Google Maps 📍")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("🕒 Needed by ${request.time}")

            if (expanded) {

                Spacer(modifier = Modifier.height(18.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "📞 Contact Number",
                    fontWeight = FontWeight.Bold
                )

                Text(request.contact)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🩸 Units Required",
                    fontWeight = FontWeight.Bold
                )

                Text(request.units)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "📍 Exact Location",
                    fontWeight = FontWeight.Bold
                )

                Text(request.location)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "📝 Additional Notes",
                    fontWeight = FontWeight.Bold
                )

                Text(request.notes)

                Spacer(modifier = Modifier.height(20.dp))

                // ACCEPT DONOR BUTTON

                Button(
                    onClick = {

                        val userId =
                            Firebase.auth.currentUser?.uid

                        if (userId != null) {

                            Firebase.firestore
                                .collection("users")
                                .document(userId)
                                .get()

                                .addOnSuccessListener { userDoc ->

                                    val donorName =
                                        userDoc.getString("name")
                                            ?: "Unknown"

                                    val donorPhone =
                                        userDoc.getString("phone")
                                            ?: "No Phone"

                                    Firebase.firestore
                                        .collection("blood_requests")
                                        .document(request.requestId)

                                        .update(

                                            mapOf(

                                                "acceptedDonors" to
                                                        request.acceptedDonors + donorName,

                                                "acceptedDonorPhones" to
                                                        request.acceptedDonorPhones + donorPhone,

                                                "acceptedDonorIds" to
                                                        request.acceptedDonorIds + userId
                                            )
                                        )

                                    Toast.makeText(
                                        context,
                                        "You accepted this request ❤️",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A4FB3)
                    ),

                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text("I Can Donate ❤️")
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ACCEPTED DONORS

                Text(
                    text = "Accepted Donors",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (request.acceptedDonors.isEmpty()) {

                    Text(
                        text = "No donors yet",
                        color = Color.Gray
                    )

                } else {

                    request.acceptedDonors.forEachIndexed { index, donor ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),

                            shape = RoundedCornerShape(14.dp)
                        ) {

                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {

                                Text(
                                    text = donor,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text =
                                        request.acceptedDonorPhones[index]
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}// ---------------- PROFILE SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

    var userData by remember {
        mutableStateOf(UserData())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    // MY REQUESTS

    var myRequests by remember {
        mutableStateOf<List<BloodRequest>>(emptyList())
    }

    // PROFILE IMAGE

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // IMAGE PICKER

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // FETCH USER DATA

    LaunchedEffect(Unit) {

        val userId =
            Firebase.auth.currentUser?.uid

        if (userId != null) {

            // FETCH PROFILE

            Firebase.firestore
                .collection("users")
                .document(userId)
                .get()

                .addOnSuccessListener { document ->

                    val user =
                        document.toObject(
                            UserData::class.java
                        )

                    if (user != null) {

                        userData = user
                    }

                    isLoading = false
                }

                .addOnFailureListener {

                    isLoading = false
                }

            // FETCH USER REQUESTS

            Firebase.firestore
                .collection("blood_requests")
                .whereEqualTo(
                    "userId",
                    userId
                )
                .get()

                .addOnSuccessListener { result ->

                    val requests =
                        result.documents.mapNotNull { document ->

                            document.toObject(
                                BloodRequest::class.java
                            )?.copy(

                                requestId = document.id
                            )
                        }

                    myRequests = requests
                }
        }
    }

    // LOADING UI

    if (isLoading) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            CircularProgressIndicator()
        }

    } else {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(20.dp)
        ) {

            item {

                // TOP BAR

                TopAppBar(

                    title = {
                        Text("My Profile")
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {

                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )


                Spacer(modifier = Modifier.height(20.dp))

                // PROFILE IMAGE

                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {

                    Card(
                        modifier = Modifier
                            .size(130.dp)
                            .clickable {

                                launcher.launch("image/*")
                            },

                        shape = RoundedCornerShape(100.dp)
                    ) {

                        if (
                            imageUri != null ||
                            userData.profileImage.isNotEmpty()
                        ) {

                            AsyncImage(

                                model =
                                    imageUri
                                        ?: userData.profileImage,

                                contentDescription = null,

                                modifier = Modifier.fillMaxSize(),

                                contentScale = ContentScale.Crop
                            )

                        } else {

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Color(0xFF6A4FB3)
                                    ),

                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text =
                                        userData.name
                                            .take(2)
                                            .uppercase(),

                                    color = Color.White,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = {

                            launcher.launch("image/*")
                        },

                        modifier = Modifier.size(42.dp),

                        containerColor = Color(0xFF6A4FB3)
                    ) {

                        Text("✏️")
                    }
                }

// SAVE PROFILE PHOTO BUTTON

                Button(

                    onClick = {

                        val userId =
                            Firebase.auth.currentUser?.uid

                        if (
                            userId != null &&
                            imageUri != null
                        ) {

                            Firebase.firestore
                                .collection("users")
                                .document(userId)

                                .update(
                                    "profileImage",
                                    imageUri.toString()
                                )

                            Toast.makeText(
                                navController.context,
                                "Profile photo saved ✅",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },

                    modifier = Modifier.padding(top = 12.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A4FB3)
                    )
                ) {

                    Text("Save Profile Photo")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // NAME

                Text(
                    text = userData.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // BLOOD GROUP

                Text(
                    text =
                        "${userData.bloodGroup} Blood Donor",

                    color = Color.Gray,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PHONE

                OutlinedTextField(
                    value = userData.phone,

                    onValueChange = { },

                    label = {
                        Text(
                            "Phone Number",
                            color = Color.DarkGray
                        )
                    },

                    textStyle = LocalTextStyle.current.copy(
                        color = Color.Black
                    ),

                    modifier = Modifier.fillMaxWidth(),

                    enabled = false,

                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(

                        disabledTextColor = Color.Black,

                        disabledBorderColor = Color.Gray,

                        disabledLabelColor = Color.DarkGray
                    )
                )

                // EMAIL

                OutlinedTextField(
                    value = userData.email,

                    onValueChange = { },

                    label = {
                        Text(
                            "Email",
                            color = Color.DarkGray
                        )
                    },

                    textStyle = LocalTextStyle.current.copy(
                        color = Color.Black
                    ),

                    modifier = Modifier.fillMaxWidth(),

                    enabled = false,

                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(

                        disabledTextColor = Color.Black,

                        disabledBorderColor = Color.Gray,

                        disabledLabelColor = Color.DarkGray
                    )
                )
                // DONATION CARD

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "🩸 Last Donation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "No Donations Yet"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "✅ Eligible to Donate",

                            color = Color(0xFF4CAF50),

                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ACCOUNT INFO

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "📋 Account Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Name: ${userData.name}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Blood Group: ${userData.bloodGroup}"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Phone: ${userData.phone}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Email: ${userData.email}")
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // MY REQUESTS

                Text(
                    text = "📋 My Requests",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                myRequests.forEach { request ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),

                        shape = RoundedCornerShape(20.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Text(
                                text =
                                    "${request.blood} • ${request.hospital}",

                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text =
                                    "Status: ${request.status}"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row {

                                // DELETE BUTTON

                                Button(
                                    onClick = {

                                        Firebase.firestore
                                            .collection("blood_requests")
                                            .document(request.requestId)
                                            .delete()

                                        myRequests =
                                            myRequests.filter {

                                                it.requestId != request.requestId
                                            }
                                    },

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red
                                    )
                                ) {

                                    Text("Delete")
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // DONOR FOUND BUTTON

                                Button(
                                    onClick = {

                                        Firebase.firestore
                                            .collection("blood_requests")
                                            .document(request.requestId)
                                            .update(
                                                "status",
                                                "DONOR FOUND"
                                            )

                                        myRequests =
                                            myRequests.map {

                                                if (it.requestId == request.requestId)
                                                    it.copy(
                                                        status = "DONOR FOUND"
                                                    )
                                                else it
                                            }
                                    }
                                ) {

                                    Text("Donor Found")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}