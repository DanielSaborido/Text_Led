package com.example.textled

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
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

    BackHandler(enabled = isStarted) {
        isStarted = false
        activity?.requestedOrientation = originalOrientation
    }

    if (isStarted) {
        LedDisplay(
            text = inputText,
            textColor = textColor,
            backgroundColor = backgroundColor
        ) {
            isStarted = false
            activity?.requestedOrientation = originalOrientation
        }
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = inputText.ifEmpty {
                            "Your text will appear here"
                        },
                        color = if (inputText.isEmpty()) {
                            Color.Gray
                        } else {
                            textColor
                        },
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
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
                    selectedPalette = null
                    originalOrientation = activity?.requestedOrientation?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    isStarted = true
                    activity?.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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

@Composable
fun LedDisplay(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onBack: () -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val displayText = text
        val availableWidthPx = with(density) {
            (maxWidth - 16.dp).toPx()
        }

        val fittedFontSize = remember(
            displayText,
            availableWidthPx,
            textColor
        ) {
            var low = 1f
            var high = 300f

            repeat(18) {
                val mid = (low + high) / 2f

                val measured = textMeasurer.measure(
                    text = AnnotatedString(displayText),
                    style = TextStyle(fontSize = mid.sp),
                    maxLines = 1,
                    softWrap = false
                )

                if (measured.size.width <= availableWidthPx) {
                    low = mid
                } else {
                    high = mid
                }
            }

            low
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayText,
                color = textColor,
                fontSize = fittedFontSize.sp,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}