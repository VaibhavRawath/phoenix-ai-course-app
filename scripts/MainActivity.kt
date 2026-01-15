package com.example.sayit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.sayit.ui.theme.SayItTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SayItTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar =
        currentRoute != "chat" &&
                currentRoute != "signin" &&
                currentRoute?.startsWith("learning") != true &&
                currentRoute != "modules"

    val showFab = currentRoute != "chat" && currentRoute != "signin"

    val currentUser = Firebase.auth.currentUser
    val startDestination = if (currentUser != null) "home" else "signin"

    Scaffold(
        containerColor = Color(0xFF050510),
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(navController)
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate("chat") },
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(10.dp, CircleShape, spotColor = NeonCyan)
                            .background(
                                Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                                CircleShape
                            )
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("signin") {
                SignInScreen {
                    navController.navigate("home") {
                        popUpTo("signin") { inclusive = true }
                    }
                }
            }

            composable("home") { HomeScreen(navController) }

            composable("modules") { ModulesScreen(navController) }

            composable(
                route = "learning/{moduleId}",
                arguments = listOf(navArgument("moduleId") { type = NavType.IntType })
            ) { backStackEntry ->
                val moduleId = backStackEntry.arguments?.getInt("moduleId") ?: 1
                LearningScreen(navController, moduleId)
            }

            composable("chat") { ChatScreen() }

            composable("premium") { PremiumScreen() }

            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050510), Color(0xFF101025))
                )
            )
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PhoenixLogo(size = 120.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "SYSTEM ONLINE",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Demo App",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { navController.navigate("modules") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(12.dp), spotColor = NeonPurple)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFD500F9), Color(0xFF651FFF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            "START LEARNING",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050510)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "PREMIUM",
                color = NeonPurple,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text("Upgrade for exclusive features", color = Color.Gray)
        }
    }
}

@Composable
fun SettingsScreen() {
    val user = Firebase.auth.currentUser

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050510)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "SETTINGS",
                color = NeonCyan,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(32.dp))

            if (user?.photoUrl != null) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .padding(16.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = user?.email ?: "No email logged in",
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = user?.displayName ?: "User",
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { Firebase.auth.signOut() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Sign Out", color = Color.White)
            }
        }
    }
}
