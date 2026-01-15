package com.example.sayit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// -------------------- DATA --------------------

data class Module(
    val id: Int,
    val title: String,
    val subtitle: String,
    val isLocked: Boolean
)

private val modules = listOf(
    Module(1, "Python Basics", "Syntax • Variables • Data Types", false),
    Module(2, "Control Flow", "If • Loops • Logic", false),
    Module(3, "Functions", "Reusable Logic", false),
    Module(4, "Data Structures", "Lists • Tuples • Dicts", false),
    Module(5, "OOP", "Classes • Objects", false),
    Module(6, "File Handling", "Read • Write • Files", false),
    Module(7, "Error Handling", "Try • Except", false),
    Module(8, "Modules & Packages", "Imports • Pip", false),
    Module(9, "Advanced Python", "Decorators • Generators", false),
    Module(10, "Final Assessment", "Certification Exam", false)
)

// -------------------- UI --------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(navController: NavController) {

    Scaffold(
        containerColor = Color(0xFF050510),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "LEARNING PATH",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF050510)
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Master Python from zero to pro.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
            }

            items(modules) { module ->
                ModuleCard(module = module) {
                    if (!module.isLocked) {
                        navController.navigate("learning/${module.id}")
                    }
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// -------------------- MODULE CARD --------------------

@Composable
fun ModuleCard(
    module: Module,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(
        if (module.isLocked)
            listOf(Color(0xFF1A1A2E), Color(0xFF101025))
        else
            listOf(NeonPurple, NeonCyan)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(10.dp, RoundedCornerShape(18.dp))
            .background(gradient, RoundedCornerShape(18.dp))
            .clickable(enabled = !module.isLocked) { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Text(
                module.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                module.subtitle,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
        }

        Icon(
            imageVector = if (module.isLocked) Icons.Default.Lock else Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(28.dp)
        )
    }
}
