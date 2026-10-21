package com.example.textled

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.example.textled.ui.theme.TextLedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TextLedTheme {
                TextLedScreen()
            }
        }
    }
}


@Composable
fun TextLedScreen() {
    var inputText by rememberSaveable { mutableStateOf("") }
    var textHistory by rememberSaveable(
        stateSaver = textHistorySaver
    ) {
        mutableStateOf(emptyList<TextHistoryItem>())
    }
    val colorSaver = Saver<Color, Long>(
        save = { it.value.toLong() },
        restore = { Color(it.toULong()) }
    )
    var textColor by rememberSaveable(stateSaver = colorSaver) { mutableStateOf(Color.Green) }
    var backgroundColor by rememberSaveable(stateSaver = colorSaver) { mutableStateOf(Color.Black) }
    var selectedPalette by remember { mutableStateOf<String?>(null) }
    var isStarted by rememberSaveable { mutableStateOf(false) }
    var originalOrientation by rememberSaveable { mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }
    val activity = LocalActivity.current

    fun addToHistory(text: String) {
        if (text.isBlank()) return
        val existing = textHistory.find { it.text == text }
        val updatedHistory = listOf(
            TextHistoryItem(
                text = text,
                pinned = existing?.pinned ?: false
            )
        ) + textHistory.filterNot { it.text == text }
        val sortedHistory = updatedHistory.filter { it.pinned } +
                updatedHistory.filterNot { it.pinned }
        val limitedHistory = sortedHistory.toMutableList()
        while (limitedHistory.size > 5) {
            val oldestUnpinned = limitedHistory.indexOfLast {
                !it.pinned
            }
            if (oldestUnpinned == -1) break
            limitedHistory.removeAt(oldestUnpinned)
        }
        textHistory = limitedHistory
    }

    fun togglePin(text: String) {
        val item = textHistory.find { it.text == text } ?: return
        if (!item.pinned && textHistory.count { it.pinned } >= 3) {
            return
        }
        textHistory = textHistory.map {
                if (it.text == text) {
                    it.copy(pinned = !it.pinned)
                } else {
                    it
                }
            }
            .let { updated ->
                updated.filter { it.pinned } +
                        updated.filterNot { it.pinned }
            }
    }

    BackHandler(enabled = isStarted) {
        isStarted = false
        activity?.requestedOrientation = originalOrientation
    }

    if (isStarted) {
        LedDisplay(
            text = inputText,
            textColor = textColor,
            backgroundColor = backgroundColor
        )
        return
    }

    val colors = listOf(
        Color.White,
        Color.Black,
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow,
        Color.Cyan,
        Color.Magenta,
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFF00BCD4),
        Color(0xFF808080)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Text LED",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Preview",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp),
                color = backgroundColor
            ) {
                if (inputText.isNotEmpty()) {
                    LedDisplay(
                        text = inputText.ifEmpty { "Your text will appear here" },
                        textColor = if (inputText.isEmpty()) Color.Gray else textColor,
                        backgroundColor = backgroundColor
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Your text will appear here",
                            color = Color.Gray,
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Enter your text",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Type something...",
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )
            }
            if (textHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Recent texts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    textHistory.forEach { item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.text,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            inputText = item.text
                                        },
                                    maxLines = 2,
                                    fontSize = 14.sp
                                )
                                Button(
                                    onClick = {
                                        togglePin(item.text)
                                    },
                                    enabled = item.pinned ||
                                            textHistory.count { it.pinned } < 3
                                ) {
                                    Text(
                                        if (item.pinned) "Unpin" else "Pin"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Color:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedPalette =
                                if (selectedPalette == "text") null
                                else "text"
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    textColor,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    Color.Gray,
                                    RoundedCornerShape(6.dp)
                                )
                        )
                        Text("Text", fontSize = 14.sp)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedPalette =
                                if (selectedPalette == "background") null
                                else "background"
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    backgroundColor,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    Color.Gray,
                                    RoundedCornerShape(6.dp)
                                )
                        )
                        Text("Background", fontSize = 14.sp)
                    }
                }
            }
            if (selectedPalette != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (selectedPalette == "text") {
                        "Text color"
                    } else {
                        "Background color"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.chunked(6).forEach { colorRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            colorRow.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .background(
                                            color,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            Color.Gray,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (selectedPalette == "text") {
                                                textColor = color
                                            } else {
                                                backgroundColor = color
                                            }
                                            selectedPalette = null
                                        }
                                )
                            }
                            repeat(6 - colorRow.size) {
                                Spacer(
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    addToHistory(inputText)
                    selectedPalette = null
                    originalOrientation = activity?.requestedOrientation?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    isStarted = true
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = inputText.isNotBlank()
            ) {
                Text(
                    text = "Start",
                    fontSize = 18.sp
                )
            }
        }
    }
}

private data class TextHistoryItem(
    val text: String,
    val pinned: Boolean = false
)

private val textHistorySaver = listSaver<List<TextHistoryItem>, String>(
    save = { history ->
        history.map {
            "${if (it.pinned) "1" else "0"}|${it.text}"
        }
    },
    restore = { saved ->
        saved.map { entry ->
            val separator = entry.indexOf('|')
            TextHistoryItem(
                text = entry.substring(separator + 1),
                pinned = entry.substring(0, separator) == "1"
            )
        }
    }
)

@Composable
fun LedDisplay(
    text: String,
    textColor: Color,
    backgroundColor: Color
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clipToBounds()
    ) {
        val scrollFontSize = (maxHeight.value * 0.65f).coerceAtMost(300f).sp
        val textWidthPx = textMeasurer.measure(
            text = AnnotatedString(text),
            style = TextStyle(fontSize = scrollFontSize),
            maxLines = 1,
            softWrap = false
        ).size.width.toFloat()
        val gapPx = with(density) { 48.dp.toPx() }
        val cycleDistance = textWidthPx + gapPx
        val transition = rememberInfiniteTransition(
            label = "ledMarquee"
        )
        val animatedOffset = transition.animateFloat(
            initialValue = 0f,
            targetValue = cycleDistance,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (
                            cycleDistance / density.density * 25
                            ).toInt().coerceIn(2500, 20000),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "ledScrollOffset"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .requiredWidth(
                        with(density) {
                            (textWidthPx * 3f + gapPx * 3f).toDp()
                        }
                    )
                    .offset {
                        IntOffset(
                            x = -animatedOffset.value.toInt(),
                            y = 0
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Text(
                        text = text,
                        color = textColor,
                        fontSize = scrollFontSize,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.requiredWidth(
                            with(density) {
                                textWidthPx.toDp()
                            }
                        )
                    )
                    Spacer(
                        modifier = Modifier.width(
                            with(density) { gapPx.toDp() }
                        )
                    )
                }
            }
        }
    }
}