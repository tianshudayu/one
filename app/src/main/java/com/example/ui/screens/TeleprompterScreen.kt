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
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BoardViewModel
import kotlinx.coroutines.isActive
import kotlin.math.abs

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun TeleprompterScreen(
    viewModel: BoardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val view = LocalView.current

    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val speed by viewModel.teleprompterSpeed.collectAsStateWithLifecycle()
    val fontSize by viewModel.teleprompterFontSize.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    var isPlaying by remember { mutableStateOf(false) }
    var isMirrored by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()

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

    // Process lines using TextMeasurer for perfect line-by-line wrapping
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current

    val lines = remember(inputText, fontSize, configuration.screenWidthDp) {
        val measuredLines = mutableListOf<String>()
        val textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (fontSize * 1.4f).sp
        )
        // Leave some padding horizontally and account for maximum scale (1.4f)
        val maxWidthPx = with(density) { 
            ((configuration.screenWidthDp.dp - 64.dp) / 1.4f).toPx().toInt().coerceAtLeast(1) 
        }

        inputText.split("\n").forEach { paragraph ->
            if (paragraph.trim().isBlank()) {
                measuredLines.add(" ")
            } else {
                val layoutResult = textMeasurer.measure(
                    text = androidx.compose.ui.text.AnnotatedString(paragraph),
                    style = textStyle,
                    softWrap = true,
                    constraints = androidx.compose.ui.unit.Constraints(maxWidth = maxWidthPx)
                )
                for (i in 0 until layoutResult.lineCount) {
                    val start = layoutResult.getLineStart(i)
                    val end = layoutResult.getLineEnd(i)
                    measuredLines.add(paragraph.substring(start, end))
                }
            }
        }
        measuredLines
    }

    // Scroll Logic
    LaunchedEffect(isPlaying, speed) {
        if (isPlaying) {
            listState.scroll {
                var previousTime = withFrameNanos { it }
                while (isActive) {
                    val currentTime = withFrameNanos { it }
                    val deltaMs = (currentTime - previousTime) / 1_000_000f
                    previousTime = currentTime
                    
                    // Speed calculation: 1-25 range.
                    val pixelsPerMs = (speed * 0.05f) 
                    val scrollAmount = pixelsPerMs * deltaMs
                    
                    scrollBy(scrollAmount)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!showControls) {
                    isPlaying = !isPlaying
                } else {
                    showControls = false
                }
            }
    ) {
        val halfScreenHeight = configuration.screenHeightDp.dp / 2

        var containerHeight by remember { mutableFloatStateOf(0f) }

        // Main Text List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerHeight = it.height.toFloat() }
                .graphicsLayer {
                    rotationY = if (isMirrored) 180f else 0f
                }
        ) {
            item { Spacer(modifier = Modifier.height(halfScreenHeight)) }
            items(lines.size) { textIndex ->
                val line = lines[textIndex]
                val listIndex = textIndex + 1 // Shifted by Spacer
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((fontSize * 1.6f).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = line,
                        color = Color.White,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.3f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                @Suppress("UNUSED_VARIABLE")
                                val scrollTrigger = listState.firstVisibleItemScrollOffset
                                
                                val layoutInfo = listState.layoutInfo
                                
                                if (containerHeight > 0f) {
                                    val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == listIndex }
                                    if (itemInfo != null) {
                                        val viewportCenter = containerHeight / 2f
                                        val itemCenter = itemInfo.offset + (itemInfo.size / 2f)
                                        val distance = kotlin.math.abs(viewportCenter - itemCenter)
                                        
                                        // Active radius covers half the screen (so texts smoothly fade over screen height)
                                        val activeRadius = containerHeight * 0.45f
                                        val progress = (distance / activeRadius).coerceIn(0f, 1f)
                                        
                                        val curve = kotlin.math.cos(progress * kotlin.math.PI * 0.5).toFloat()
                                        alpha = androidx.compose.ui.util.lerp(0.2f, 1f, curve)
                                        val s = androidx.compose.ui.util.lerp(0.7f, 1.4f, curve)
                                        scaleX = s
                                        scaleY = s
                                    } else {
                                        alpha = 0.2f
                                        scaleX = 0.7f
                                        scaleY = 0.7f
                                    }
                                } else {
                                    alpha = 0.2f
                                    scaleX = 0.7f
                                    scaleY = 0.7f
                                }
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                            }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(halfScreenHeight)) }
        }

        // Focus Indicator Lines (Optional but good for UX)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(fontSize.dp * 1.5f)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)).align(Alignment.TopCenter))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)).align(Alignment.BottomCenter))
        }

        // Float button to show controls when hidden
        AnimatedVisibility(
            visible = !showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.clickable { showControls = true }
            ) {
                Text(
                    text = if (isEnglish) "Controls" else "控制板",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp, 8.dp)
                )
            }
        }

        // Bottom Controls Panel
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Speed
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (isEnglish) "Speed: ${speed.toInt()}" else "速度: ${speed.toInt()}", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = speed,
                            onValueChange = { viewModel.updateSpeed(it) },
                            valueRange = 1f..3f,
                            steps = 1,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Center: Actions
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        
                        FloatingActionButton(
                            onClick = { isPlaying = !isPlaying },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                        }
                        
                        IconButton(onClick = { isMirrored = !isMirrored }) {
                            Icon(Icons.Default.Flip, contentDescription = "Mirror")
                        }
                    }

                    // Right: Font Size
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (isEnglish) "Text Size: ${fontSize.toInt()}" else "字号: ${fontSize.toInt()}", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = fontSize,
                            onValueChange = { viewModel.updateFontSize(it) },
                            valueRange = 24f..120f,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
