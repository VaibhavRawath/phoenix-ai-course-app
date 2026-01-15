package com.example.sayit

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- DATABASE TABLES ---
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "messages",
    foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

fun MessageEntity.toUiModel(): ChatMessage = ChatMessage(role, content)

// --- DAO ---
@Dao
interface ChatDao {
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<MessageEntity>>

    @Insert
    suspend fun insertMessage(message: MessageEntity)
}

// --- DATABASE SETUP ---
@Database(entities = [SessionEntity::class, MessageEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "sayit_db_v2")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}

// --- PHOENIX API (INTERNAL) ---
data class ChatMessage(val role: String, val content: String)
data class PhoenixRequest(val model: String = "mistral-tiny", val messages: List<ChatMessage>)
data class PhoenixResponse(val choices: List<PhoenixChoice>)
data class PhoenixChoice(val message: ChatMessage)

interface PhoenixApi {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun generateResponse(
        @retrofit2.http.Header("Authorization") auth: String,
        @Body request: PhoenixRequest
    ): PhoenixResponse
}

object PhoenixNetwork {
    val service: PhoenixApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mistral.ai/") // Backend provider
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PhoenixApi::class.java)
    }
}

// --- VIEWMODEL ---
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).chatDao()

    val sessions = dao.getAllSessions().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId = _currentSessionId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = _currentSessionId.flatMapLatest { sessId ->
        if (sessId == null) flowOf(emptyList())
        else dao.getMessagesForSession(sessId).map { list -> list.map { it.toUiModel() } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun startNewChat() { _currentSessionId.value = null }

    fun loadSession(sessionId: Long) { _currentSessionId.value = sessionId }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            dao.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) _currentSessionId.value = null
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            var sessId = _currentSessionId.value
            if (sessId == null) {
                val title = userText.take(30) + "..."
                sessId = dao.insertSession(SessionEntity(title = title))
                _currentSessionId.value = sessId
            }

            dao.insertMessage(MessageEntity(sessionId = sessId, role = "user", content = userText))
            _isLoading.value = true

            try {
                // Get history
                val history = dao.getMessagesForSession(sessId).first().map { it.toUiModel() }.filter { it.content.isNotBlank() }.toMutableList()
                
                // Get User Name from Firebase
                val user = Firebase.auth.currentUser
                val userName = user?.displayName ?: "User"

                // Inject System Prompt with Current Date/Time
                val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                // Updated system prompt: instruct AI to be aware of date/time but NOT to mention it unless asked
                // Also includes user's name
                val systemPrompt = ChatMessage("system", "You are Phoenix, a helpful AI assistant. You are speaking with $userName. Today is $currentDate and the time is $currentTime. Do not mention the date or time in your responses unless explicitly asked by the user.")
                
                // Prepend system prompt to history sent to API
                val apiMessages = listOf(systemPrompt) + history

                val response = PhoenixNetwork.service.generateResponse(
                    auth = "api key here",
                    request = PhoenixRequest(messages = apiMessages)
                )
                dao.insertMessage(MessageEntity(sessionId = sessId, role = "assistant", content = response.choices.first().message.content))
            } catch (e: Exception) {
                // Mask the error message to hide provider details from user
                val errorMsg = if (e.message?.contains("mistral", ignoreCase = true) == true) "Connection interrupted" else e.message
                dao.insertMessage(MessageEntity(sessionId = sessId, role = "assistant", content = "System Error: $errorMsg"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
