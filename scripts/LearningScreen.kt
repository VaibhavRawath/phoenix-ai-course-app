package com.example.sayit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

// Content Data Structure
data class LearningContent(
    val title: String,
    val subtitle: String,
    val descriptionTitle: String,
    val description: String,
    val codeSnippet: String,
    val output: List<String>,
    val proTip: String
)

private fun getModuleContent(moduleId: Int): LearningContent {
    return when (moduleId) {
        1 -> LearningContent(
            title = "Python Basics",
            subtitle = "Syntax • Variables • Data Types",
            descriptionTitle = "Variables & Types",
            description = "Variables are containers for storing data values. Python has no command for declaring a variable. A variable is created the moment you first assign a value to it.",
            codeSnippet = """
                x = 5
                y = "Hello, World!"
                print(x)
                print(y)
            """.trimIndent(),
            output = listOf("$ > python main.py", "5", "Hello, World!"),
            proTip = "Variable names are case-sensitive."
        )
        2 -> LearningContent(
            title = "Control Flow",
            subtitle = "If • Loops • Logic",
            descriptionTitle = "Conditional Logic",
            description = "Python supports the usual logical conditions from mathematics. These conditions can be used in several ways, most commonly in 'if' statements and loops.",
            codeSnippet = """
                a = 33
                b = 200
                if b > a:
                  print("b is greater than a")
            """.trimIndent(),
            output = listOf("$ > python main.py", "b is greater than a"),
            proTip = "Indentation is crucial in Python. It defines the scope of loops and conditionals."
        )
        3 -> LearningContent(
            title = "Functions",
            subtitle = "Reusable Logic",
            descriptionTitle = "Defining Functions",
            description = "A function is a block of code which only runs when it is called. You can pass data, known as parameters, into a function.",
            codeSnippet = """
                def my_function(fname):
                  print(fname + " Refsnes")

                my_function("Emil")
                my_function("Tobias")
            """.trimIndent(),
            output = listOf("$ > python main.py", "Emil Refsnes", "Tobias Refsnes"),
            proTip = "Use the 'return' keyword to let a function return a value."
        )
        4 -> LearningContent(
            title = "Data Structures",
            subtitle = "Lists • Tuples • Dicts",
            descriptionTitle = "Lists in Python",
            description = "Lists are used to store multiple items in a single variable. Lists are one of 4 built-in data types in Python used to store collections of data.",
            codeSnippet = """
                thislist = ["apple", "banana", "cherry"]
                print(thislist)
                print(len(thislist))
            """.trimIndent(),
            output = listOf("$ > python main.py", "['apple', 'banana', 'cherry']", "3"),
            proTip = "List items are ordered, changeable, and allow duplicate values."
        )
        5 -> LearningContent(
            title = "OOP",
            subtitle = "Classes • Objects",
            descriptionTitle = "Classes and Objects",
            description = "Python is an object oriented programming language. Almost everything in Python is an object, with its properties and methods.",
            codeSnippet = """
                class Person:
                  def __init__(self, name, age):
                    self.name = name
                    self.age = age

                p1 = Person("John", 36)
                print(p1.name)
            """.trimIndent(),
            output = listOf("$ > python main.py", "John"),
            proTip = "The __init__() function is called automatically every time the class is being used to create a new object."
        )
        // Default fallback
        else -> LearningContent(
            title = "Advanced Python",
            subtitle = "Decorators • Generators",
            descriptionTitle = "Decorators",
            description = "Decorators are a very powerful and useful tool in Python since it allows programmers to modify the behaviour of a function or class.",
            codeSnippet = """
                def my_decorator(func):
                    def wrapper():
                        print("Something is happening before the function is called.")
                        func()
                        print("Something is happening after the function is called.")
                    return wrapper

                @my_decorator
                def say_whee():
                    print("Whee!")
            """.trimIndent(),
            output = listOf("$ > python main.py", "Something is happening before...", "Whee!", "Something is happening after..."),
            proTip = "Decorators wrap a function, modifying its behavior."
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(navController: NavController, moduleId: Int = 1) {
    val scrollState = rememberScrollState()
    val content = getModuleContent(moduleId)

    Scaffold(
        containerColor = Color(0xFF050510),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "ACADEMY",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF050510).copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // HERO SECTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6200EA), Color(0xFFD500F9))
                        )
                    )
            ) {
                // Decor circles
                Box(
                    modifier = Modifier
                        .offset(x = (-20).dp, y = (-20).dp)
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.1f))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = 20.dp)
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.1f))
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Surface(
                        color = Color.White.copy(0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "MODULE $moduleId",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        content.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        content.subtitle,
                        color = Color.White.copy(0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CHAPTER TITLE
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    content.descriptionTitle.uppercase(),
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // INFO CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F)),
                border = BorderStroke(1.dp, Color.White.copy(0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        content.descriptionTitle,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        content.description,
                        color = Color(0xFFB0B0C0),
                        lineHeight = 24.sp,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CODE EXAMPLE HEADER
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, null, tint = NeonPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "EXAMPLE SNIPPET",
                    color = NeonPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CODE BLOCK
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E28))
                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
            ) {
                Column {
                    // Window Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2B2B3B))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                        
                        Spacer(Modifier.weight(1f))
                        Text("main.py", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    
                    // Code Content
                    Text(
                        text = content.codeSnippet,
                        color = Color(0xFFF8F8F2),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // OUTPUT TERMINAL
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, null, tint = TechGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "RUNTIME OUTPUT",
                    color = TechGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    content.output.forEach { line ->
                        val color = if(line.startsWith("$")) Color.Gray else TechGreen
                        Text(line, color = color, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // PRO TIP
            Surface(
                color = Color(0xFF101025),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().shadow(8.dp, spotColor = NeonPurple)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NeonPurple.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lightbulb, null, tint = NeonPurple)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "PRO TIP",
                            color = NeonPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            content.proTip,
                            color = Color(0xFFCCCCCC),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // COMPLETE BUTTON
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E28)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.1f))
            ) {
                Text("MARK AS COMPLETE", color = Color.White)
            }
        }
    }
}
