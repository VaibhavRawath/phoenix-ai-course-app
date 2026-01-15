package com.example.sayit

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import coil.compose.AsyncImage
import android.app.Activity
import android.content.Intent

// --- COLORS ---
val NeonCyan = Color(0xFF00E5FF)
val NeonPurple = Color(0xFFD500F9)
val DarkGlass = Color(0xFF1A1A2E).copy(alpha = 0.7f)
val TechGreen = Color(0xFF00FF9D)
val SurfaceDark = Color(0xFF13131F)
val CodeBg = Color(0xFF1E1E28)
val FireOrange = Color(0xFFFF5722)
val GoldYellow = Color(0xFFFFC107)
val DeepPurple = Color(0xFF6200EA)

// --- SYNTAX COLORS ---
val SyntaxKeyword = Color(0xFFFF79C6)       // Pink/Magenta
val SyntaxString = Color(0xFFF1FA8C)        // Light Yellow
val SyntaxComment = Color(0xFF6272A4)       // Blueish Grey
val SyntaxNumber = Color(0xFFBD93F9)        // Purple
val SyntaxType = Color(0xFF8BE9FD)          // Cyan
val SyntaxPlain = Color(0xFFF8F8F2)         // Off White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val sessions by viewModel.sessions.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    // LARGE PROMPT POOL
    val allPrompts = remember {
        listOf(
            "Plan a 3-day trip to Tokyo",
            "Draft a professional email",
            "Explain Quantum Physics",
            "Suggest a healthy dinner recipe",
            "Write a Python script for Fibonacci",
            "How do I center a div in CSS?",
            "Tell me a joke",
            "Summarize the history of Rome",
            "Give me a workout routine",
            "Write a poem about the ocean",
            "Explain blockchain to a 5-year-old",
            "Best sci-fi movies to watch",
            "How to make a perfect coffee",
            "Debugging tips for Android",
            "What is the meaning of life?",
            "Generate a random character name",
            "Explain the theory of relativity",
            "Tips for public speaking",
            "How does a car engine work?",
            "Write song lyrics about AI",
            "What are the benefits of yoga?",
            "How to start a garden",
            "Explain Machine Learning",
            "Write a short horror story",
            "Best books to read this year"
        )
    }

    // AUTO-CYCLING PROMPT STATE
    var currentPromptIndex by remember { mutableIntStateOf(0) }

    // User's first name for greeting
    val user = Firebase.auth.currentUser
    val userName = user?.displayName?.split(" ")?.firstOrNull() ?: "User"

    LaunchedEffect(Unit) {
        while(true) {
            delay(4000) // Slower cycle: 4 seconds
            currentPromptIndex = (currentPromptIndex + 2) % allPrompts.size
        }
    }

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size)
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Chat?", color = Color.White) },
            text = { Text("Are you sure you want to delete this session? This action cannot be undone.", color = Color.LightGray) },
            containerColor = Color(0xFF1A1A2E),
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { viewModel.deleteSession(it) }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0F0F1A),
                drawerContentColor = Color.White,
                modifier = Modifier.width(300.dp).border(0.dp, Color.Transparent) // Clean edge
            ) {
                Spacer(Modifier.height(30.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 20.dp)) {
                    PhoenixLogo(size = 24.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "ACCESS LOGS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.height(10.dp))

                NavigationDrawerItem(
                    label = { Text("NEW CHAT", fontWeight = FontWeight.Bold) },
                    selected = false,
                    icon = { Icon(Icons.Default.Add, null, tint = NeonCyan) },
                    onClick = { viewModel.startNewChat(); scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp).border(1.dp, NeonCyan.copy(alpha=0.3f), RoundedCornerShape(8.dp)),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color(0xFF1A1A2E), unselectedTextColor = NeonCyan)
                )

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(0.1f))

                LazyColumn(Modifier.weight(1f)) {
                    items(sessions) { session ->
                        NavigationDrawerItem(
                            label = { Text(session.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                            selected = currentSessionId == session.id,
                            icon = { Icon(Icons.Default.ChatBubbleOutline, null, tint = Color.Gray) },
                            badge = {
                                IconButton(onClick = { showDeleteDialog = session.id }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = { viewModel.loadSession(session.id); scope.launch { drawerState.close() } },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Color(0xFF1A1A2E),
                                unselectedTextColor = Color.LightGray,
                                selectedTextColor = NeonCyan
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(0.1f))
                Spacer(Modifier.height(10.dp))
                NavigationDrawerItem(
                    label = { Text("SIGN OUT", fontWeight = FontWeight.Bold) },
                    selected = false,
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color(0xFFFF5252)) },
                    onClick = {
                        Firebase.auth.signOut()
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedTextColor = Color(0xFFFF5252)
                    )
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // TRANSPARENT GLASS TOP BAR
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PhoenixLogo(size = 32.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("PHOENIX",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            // The Flexing Badge
                            Surface(
                                color = NeonPurple.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, NeonPurple.copy(alpha=0.5f))
                            ) {
                                Text("BETA",
                                    color = NeonPurple,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF050510).copy(alpha = 0.9f))
                )
            },
            contentWindowInsets = WindowInsets.systemBars
        ) { paddingValues ->
            Box(Modifier.fillMaxSize().background(Color(0xFF050510))) {
                // Background Grid Effect (Subtle)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        if (messages.isEmpty()) {
                            item {
                                Box(Modifier.fillParentMaxSize(), Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        PhoenixLogo(size = 80.dp)
                                        Spacer(Modifier.height(16.dp))
                                        Text("PHOENIX", style = MaterialTheme.typography.displayLarge, color = Color.White, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(8.dp))
                                        
                                        // MODIFIED HERE: Added User Name with Card Style
                                        Surface(
                                            color = NeonPurple.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, NeonPurple.copy(alpha=0.3f)),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                // User Profile Picture
                                                if (user?.photoUrl != null) {
                                                    AsyncImage(
                                                        model = user.photoUrl,
                                                        contentDescription = "User Profile",
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .border(1.dp, NeonPurple, CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "User Profile",
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonPurple.copy(alpha = 0.2f))
                                                            .padding(4.dp),
                                                        tint = NeonPurple
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }

                                                Text(
                                                    text = "Hello $userName,",
                                                    color = NeonPurple,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text("Try asking about:", color = Color.Gray)
                                        
                                        Spacer(Modifier.height(24.dp))

                                        // AUTOMATED SWIPING PROMPT CAROUSEL (MULTIPLE PROMPTS)
                                        AnimatedContent(
                                            targetState = currentPromptIndex,
                                            transitionSpec = {
                                                (fadeIn(animationSpec = tween(1500)) + slideInVertically(animationSpec = tween(1500)) { height -> height } + scaleIn(animationSpec = tween(1500))).togetherWith(
                                                    fadeOut(animationSpec = tween(1500)) + slideOutVertically(animationSpec = tween(1500)) { height -> -height } + scaleOut(animationSpec = tween(1500))
                                                )
                                            }, label = "prompt_swap"
                                        ) { targetIndex ->
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                // Prompt 1
                                                PromptChip(viewModel, allPrompts[targetIndex % allPrompts.size])
                                                Spacer(Modifier.height(12.dp))
                                                // Prompt 2
                                                PromptChip(viewModel, allPrompts[(targetIndex + 1) % allPrompts.size])
                                                Spacer(Modifier.height(12.dp))
                                                // Prompt 3
                                                PromptChip(viewModel, allPrompts[(targetIndex + 2) % allPrompts.size])
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            items(messages) { message -> ChatBubble(message) }
                        }
                        if (isLoading) item { TypingIndicator() }
                    }

                    // INPUT AREA
                    Surface(
                        color = Color(0xFF0F0F1A),
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 10.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .navigationBarsPadding(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier.weight(1f).padding(end = 10.dp),
                                shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF1A1A2E),
                                    unfocusedContainerColor = Color(0xFF1A1A2E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.LightGray,
                                    cursorColor = NeonCyan,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                placeholder = { Text("Ask anything...", color = Color.Gray, fontFamily = FontFamily.Monospace) },
                                maxLines = 4
                            )

                            // NEON SEND BUTTON
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CutCornerShape(12.dp))
                                    .background(Brush.linearGradient(listOf(NeonCyan, Color(0xFF00B0FF))))
                                    .clickable { viewModel.sendMessage(inputText); inputText = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromptChip(viewModel: ChatViewModel, text: String) {
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .clickable { viewModel.sendMessage(text) },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1A2E),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Text(text, color = Color.LightGray, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), fontSize = 16.sp)
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val user = Firebase.auth.currentUser

    // UI Feedback States
    var isCopied by remember { mutableStateOf(false) }
    var likeState by remember { mutableStateOf<Boolean?>(null) } // true = like, false = dislike, null = none

    if (isUser) {
        // USER BUBBLE: Neon Purple Gradient, sleek rounded
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.Top) {
                 Column(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                        .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF6200EA), NeonPurple)))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // USER PROFILE PICTURE
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "User Profile",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, NeonPurple, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Profile",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    } else {
        // AI BUBBLE: Premium Modern UI
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(32.dp)
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
                    .background(Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                PhoenixLogo(size = 18.dp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name & Flex Badge
                Row(
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "PHOENIX",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = TechGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, TechGreen.copy(alpha=0.3f))
                    ) {
                        Text("ONLINE",
                            color = TechGreen,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Bubble
                Box(
                    modifier = Modifier
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp), spotColor = Color(0x40000000))
                        .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                        .background(SurfaceDark)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                        .padding(20.dp)
                ) {
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        FormattedAiText(message.content)
                    }
                }

                // Actions Footer
                Row(
                    modifier = Modifier.padding(top = 8.dp, start = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // COPY BUTTON
                    IconButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message.content))
                            isCopied = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if(isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            "Copy",
                            tint = if(isCopied) TechGreen else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // LIKE BUTTON
                    IconButton(
                        onClick = {
                            likeState = true
                            Toast.makeText(context, "Feedback sent!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            "Like",
                            tint = if(likeState == true) NeonCyan else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // DISLIKE BUTTON
                    IconButton(
                        onClick = {
                            likeState = false
                            Toast.makeText(context, "Feedback sent!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ThumbDown,
                            "Dislike",
                            tint = if(likeState == false) Color(0xFFFF5252) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- MODERN FORMATTER ---
@Composable
fun FormattedAiText(text: String) {
    val blocks = remember(text) { parseBlocks(text) }

    Column {
        blocks.forEach { block ->
            when (block) {
                is UiBlock.Code -> {
                    CodeBlock(block.language, block.content)
                }
                is UiBlock.Text -> {
                    RenderTextBlock(block.content)
                }
            }
        }
    }
}

@Composable
fun RenderTextBlock(text: String) {
    val lines = text.split("\n")
    lines.forEach { line ->
        when {
            // Header
            line.startsWith("#") -> {
                Text(
                    text = line.replace("#", "").trim(),
                    color = TechGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )
            }
            // Bullet
            line.trim().startsWith("- ") || line.trim().startsWith("* ") -> {
                Row(modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                    Text("•", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                    Text(parseBold(line.trim().substring(1).trim()), color = Color(0xFFE0E0E0), fontSize = 15.sp, fontFamily = FontFamily.SansSerif, lineHeight = 26.sp)
                }
            }
            // Number
            line.trim().matches(Regex("\\d+\\..*")) -> {
                val numberPart = line.trim().takeWhile { it != '.' }
                val textPart = line.trim().dropWhile { it != '.' }.drop(1).trim()
                Row(modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                    Text("$numberPart.", color = NeonPurple, fontSize = 15.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                    Text(parseBold(textPart), color = Color(0xFFE0E0E0), fontSize = 15.sp, fontFamily = FontFamily.SansSerif, lineHeight = 26.sp)
                }
            }
            // Normal Text
            else -> {
                if (line.isNotBlank()) {
                    Text(
                        text = parseBold(line),
                        color = Color(0xFFEEEEEE),
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlock(language: String, content: String) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) } // State to track copied status
    val highlightedText = remember(content, language) { SyntaxHighlighter.highlight(content, language) }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        shape = RoundedCornerShape(8.dp),
        color = CodeBg,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B2B3B))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (language.isNotBlank()) language.uppercase() else "CODE",
                    color = Color(0xFF8F93A2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Copy Button with State Change
                Row(
                    modifier = Modifier
                        .clickable {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(content))
                            isCopied = true
                        }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = if (isCopied) TechGreen else Color(0xFF8F93A2),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "Copied!" else "Copy",
                        color = if (isCopied) TechGreen else Color(0xFF8F93A2),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Content
            Box(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = highlightedText,
                    color = SyntaxPlain,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
fun parseBold(text: String): androidx.compose.ui.text.AnnotatedString {
    val parts = text.split("**")
    return buildAnnotatedString {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Color.White)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            modifier = Modifier.width(100.dp).height(2.dp),
            color = NeonCyan,
            trackColor = Color.Transparent
        )
        Spacer(Modifier.width(8.dp))
        Text("PROCESSING...", color = NeonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun PhoenixLogo(size: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()

        // --- COLORS FROM IMAGE ---
        val wingDarkRed = Color(0xFF7D2E30)   // Dark maroon tips
        val wingMainRed = Color(0xFFA83639)   // Main wing red
        val bodyYellow = Color(0xFFFFEE58)    // Bright yellow body
        val bodyShadow = Color(0xFFFFCA28)    // Darker yellow shading
        val chestOrange = Color(0xFFFF7043)   // Chest tuft
        val eyeColor = Color(0xFF6D2124)      // Dark red eyes

        // 1. WINGS (Background)
        val wingsPath = Path().apply {
            moveTo(w * 0.5f, h * 0.6f) // Center body anchor

            // Left Wing
            cubicTo(w * 0.4f, h * 0.5f, w * 0.1f, h * 0.3f, w * 0.05f, h * 0.15f) // Top tip
            cubicTo(w * 0.05f, h * 0.35f, w * 0.0f, h * 0.5f, w * 0.15f, h * 0.65f) // Jagged edge 1
            cubicTo(w * 0.2f, h * 0.7f, w * 0.3f, h * 0.75f, w * 0.45f, h * 0.75f) // Return to body

            // Right Wing (Mirror)
            moveTo(w * 0.5f, h * 0.6f)
            cubicTo(w * 0.6f, h * 0.5f, w * 0.9f, h * 0.3f, w * 0.95f, h * 0.15f) // Top tip
            cubicTo(w * 0.95f, h * 0.35f, w * 1.0f, h * 0.5f, w * 0.85f, h * 0.65f) // Jagged edge 1
            cubicTo(w * 0.8f, h * 0.7f, w * 0.7f, h * 0.75f, w * 0.55f, h * 0.75f) // Return to body
        }
        drawPath(wingsPath, wingMainRed)

        // Add darker accents to wing tips
        val wingTipsPath = Path().apply {
            moveTo(w * 0.05f, h * 0.15f)
            quadraticBezierTo(w * 0.15f, h * 0.25f, w * 0.2f, h * 0.4f)
            lineTo(w * 0.1f, h * 0.3f)
            close()

            moveTo(w * 0.95f, h * 0.15f)
            quadraticBezierTo(w * 0.85f, h * 0.25f, w * 0.8f, h * 0.4f)
            lineTo(w * 0.9f, h * 0.3f)
            close()
        }
        drawPath(wingTipsPath, wingDarkRed)

        // 2. TAIL FLAMES
        val tailPath = Path().apply {
            moveTo(w * 0.5f, h * 0.7f)
            quadraticBezierTo(w * 0.4f, h * 0.85f, w * 0.45f, h * 0.95f) // Left tip
            lineTo(w * 0.5f, h * 0.85f) // Center notch
            lineTo(w * 0.55f, h * 0.95f) // Right tip
            quadraticBezierTo(w * 0.6f, h * 0.85f, w * 0.5f, h * 0.7f)
            close()
        }
        drawPath(tailPath, wingDarkRed)

        // 3. BODY (Yellow Fluff)
        drawCircle(
            color = bodyYellow,
            radius = w * 0.14f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f)
        )

        // 4. EARS
        val earsPath = Path().apply {
            // Left Ear
            moveTo(w * 0.42f, h * 0.4f)
            lineTo(w * 0.38f, h * 0.28f)
            lineTo(w * 0.48f, h * 0.38f)
            // Right Ear
            moveTo(w * 0.58f, h * 0.4f)
            lineTo(w * 0.62f, h * 0.28f)
            lineTo(w * 0.52f, h * 0.38f)
        }
        drawPath(earsPath, bodyYellow)

        // 5. CHEST TUFT (Orange)
        val chestPath = Path().apply {
            moveTo(w * 0.42f, h * 0.55f)
            quadraticBezierTo(w * 0.5f, h * 0.65f, w * 0.58f, h * 0.55f)
            lineTo(w * 0.5f, h * 0.75f) // Point down
            close()
        }
        drawPath(chestPath, chestOrange)

        // 6. FACE DETAILS
        // Eyes
        drawOval(
            color = eyeColor,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.48f),
            size = androidx.compose.ui.geometry.Size(w * 0.05f, h * 0.07f)
        )
        drawOval(
            color = eyeColor,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.53f, h * 0.48f),
            size = androidx.compose.ui.geometry.Size(w * 0.05f, h * 0.07f)
        )

        // Tiny Nose
        drawCircle(
            color = chestOrange,
            radius = w * 0.015f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.56f)
        )

        // Whiskers (Optional subtle lines)
        drawLine(
            color = bodyShadow,
            start = androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.55f),
            end = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.53f),
            strokeWidth = 2f
        )
        drawLine(
            color = bodyShadow,
            start = androidx.compose.ui.geometry.Offset(w * 0.6f, h * 0.55f),
            end = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.53f),
            strokeWidth = 2f
        )
    }
}

// --- MARKDOWN PARSER ---
sealed class UiBlock {
    data class Text(val content: String) : UiBlock()
    data class Code(val language: String, val content: String) : UiBlock()
}

fun parseBlocks(text: String): List<UiBlock> {
    val blocks = mutableListOf<UiBlock>()
    val lines = text.split("\n")
    var inCode = false
    var codeBuffer = StringBuilder()
    var textBuffer = StringBuilder()
    var currentLang = ""

    fun flushText() {
        if (textBuffer.isNotEmpty()) {
            blocks.add(UiBlock.Text(textBuffer.toString()))
            textBuffer.clear()
        }
    }

    fun flushCode() {
        if (codeBuffer.isNotEmpty()) {
            blocks.add(UiBlock.Code(currentLang, codeBuffer.toString().trimEnd()))
            codeBuffer.clear()
        }
    }

    for (line in lines) {
        if (line.trim().startsWith("```")) {
            if (inCode) {
                // End code block
                inCode = false
                flushCode()
            } else {
                // Start code block
                flushText()
                inCode = true
                currentLang = line.trim().removePrefix("```").trim()
            }
        } else {
            if (inCode) {
                codeBuffer.append(line).append("\n")
            } else {
                textBuffer.append(line).append("\n")
            }
        }
    }
    flushText()
    if (inCode) flushCode()

    return blocks
}

// --- SYNTAX HIGHLIGHTER ---
object SyntaxHighlighter {
    // Basic Keywords for common languages
    private val KEYWORDS = mapOf(
        "kotlin" to listOf("val", "var", "fun", "class", "object", "interface", "return", "if", "else", "when", "for", "while", "package", "import", "true", "false", "null", "super", "this"),
        "java" to listOf("public", "private", "protected", "class", "interface", "extends", "implements", "return", "if", "else", "for", "while", "new", "static", "void", "int", "boolean", "true", "false", "null"),
        "python" to listOf("def", "class", "return", "if", "else", "elif", "for", "while", "import", "from", "as", "try", "except", "print", "True", "False", "None"),
        "javascript" to listOf("const", "let", "var", "function", "return", "if", "else", "for", "while", "class", "import", "export", "true", "false", "null", "undefined"),
        "default" to listOf("fun", "def", "function", "class", "return", "if", "else", "for", "while", "true", "false", "null")
    )

    fun highlight(code: String, language: String): androidx.compose.ui.text.AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Normalize language key
            val langKey = language.lowercase().trim()
            val keywords = KEYWORDS[langKey] ?: KEYWORDS["default"]!!

            // Helper to apply style to all regex matches
            fun applyStyle(pattern: String, style: SpanStyle) {
                val matcher = Pattern.compile(pattern).matcher(code)
                while (matcher.find()) {
                    addStyle(style, matcher.start(), matcher.end())
                }
            }

            // 1. Numbers (Purple)
            applyStyle("\\b\\d+\\b", SpanStyle(color = SyntaxNumber))

            // 2. Types (Cyan) - Capitalized words often types
            applyStyle("\\b[A-Z][a-zA-Z0-9]*\\b", SpanStyle(color = SyntaxType))

            // 3. Keywords (Pink)
            keywords.forEach { keyword ->
                applyStyle("\\b$keyword\\b", SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold))
            }

            // 4. Strings (Yellow) - applied later to override keywords inside strings
            applyStyle("\".*?\"", SpanStyle(color = SyntaxString))
            applyStyle("'.*?'", SpanStyle(color = SyntaxString))

            // 5. Comments (Grey) - applied last to override everything
            if (langKey == "python") {
                applyStyle("#.*", SpanStyle(color = SyntaxComment))
            } else {
                applyStyle("//.*", SpanStyle(color = SyntaxComment))
            }
        }
    }
}