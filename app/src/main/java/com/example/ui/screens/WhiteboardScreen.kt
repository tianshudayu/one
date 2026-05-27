package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BoardViewModel
import com.example.ui.theme.*

@Composable
fun WhiteboardScreen(
    viewModel: BoardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val view = LocalView.current

    val inputText by viewModel.inputText.collectAsStateWithLifecycle()

    var showControls by remember { mutableStateOf(false) }
    var backgroundColor by remember { mutableStateOf(Color.White) }
    
    val textColor = when (backgroundColor) {
        Color.White -> Color.Black
        Color.Black -> Color.White
        else -> Color.White // For red and blue, white looks best
    }

    // Handle Immersive Mode and Orientation
    DisposableEffect(activity) {
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        val window = activity?.window
        if (window != null) {
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    BackHandler {
        onNavigateBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        AutoFitText(
            text = inputText,
            color = textColor,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        )

        // Controls Panel
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ColorDot(Color.White) { backgroundColor = Color.White }
                    ColorDot(Color.Black) { backgroundColor = Color.Black }
                    ColorDot(BlueBackground) { backgroundColor = BlueBackground }
                    ColorDot(RedBackground) { backgroundColor = RedBackground }
                }
            }
        }
    }
}

@Composable
fun ColorDot(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape)
            .padding(2.dp)
            .background(Color.Transparent, CircleShape) // For border effect if needed
            .clickable(onClick = onClick)
    )
}

@Composable
fun AutoFitText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val maxWidthPx = if (constraints.hasBoundedWidth) constraints.maxWidth else 10000
        val maxHeightPx = if (constraints.hasBoundedHeight) constraints.maxHeight else 10000

        val fontToUse = remember(text, maxWidthPx, maxHeightPx) {
            var low = 10
            var high = 300
            var bestFit = 10

            val annotatedString = androidx.compose.ui.text.AnnotatedString(text)

            // Binary search to find the maximum possible font size
            while (low <= high) {
                val mid = (low + high) / 2
                val result = textMeasurer.measure(
                    text = annotatedString,
                    style = TextStyle(
                        fontSize = mid.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = (mid * 1.3f).sp
                    ),
                    constraints = androidx.compose.ui.unit.Constraints(
                        maxWidth = maxWidthPx
                    ),
                    softWrap = true
                )

                if (result.size.height <= maxHeightPx && !result.hasVisualOverflow) {
                    bestFit = mid
                    low = mid + 1 // Try larger
                } else {
                    high = mid - 1 // Too large, try smaller
                }
            }
            (bestFit - 2).coerceAtLeast(10) // Slight buffer to ensure no clipping
        }

        Text(
            text = text,
            color = color,
            fontSize = fontToUse.sp,
            lineHeight = (fontToUse * 1.3f).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
