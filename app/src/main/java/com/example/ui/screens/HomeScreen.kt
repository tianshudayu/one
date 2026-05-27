package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BoardViewModel

val PremiumBlack = Color(0xFF121212)
val PremiumDarkGray = Color(0xFF2C2C2C)
val PremiumGray = Color(0xFF8E8E93)
val PremiumLightGray = Color(0xFFE5E5EA)
val PremiumBg = Color(0xFFF2F2F7)

@Composable
fun HomeScreen(
    viewModel: BoardViewModel,
    onNavigateToWhiteboard: () -> Unit,
    onNavigateToTeleprompter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { }
    }

    val title = if (isEnglish) "One Board" else "一块白板"
    val subtitle = "One Board — Teleprompter"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PremiumBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars.add(WindowInsets(top = 48.dp, bottom = 32.dp))),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = PremiumBlack,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 32.sp
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = subtitle.uppercase(),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    color = PremiumGray,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                // Input TextField Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color.Black.copy(alpha = 0.05f),
                            ambientColor = Color.Black.copy(alpha = 0.02f)
                        )
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(28.dp)
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = viewModel::updateInputText,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("input_text_field"),
                        textStyle = TextStyle(
                            fontSize = 17.sp,
                            color = PremiumDarkGray,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(PremiumBlack),
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text(
                                    text = if (isEnglish) "Enter your text here..." else "欢迎使用。\n\n在此输入您要展示或朗读的内容。\n下方的「醒目白板」模式会全屏显示文本，适合接机或粉丝应援。\n「提词器」模式提供舒适的文字滚动体验，助力播讲或视频录制。",
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        color = PremiumGray.copy(alpha = 0.8f),
                                        lineHeight = 26.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Word Count Pill
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(PremiumBg, RoundedCornerShape(50))
                            .border(1.dp, PremiumLightGray, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingDot()
                        
                        Text(
                            text = "${inputText.length} WORDS",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = PremiumGray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().height(112.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Whiteboard Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), spotColor = PremiumGray)
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .border(1.dp, PremiumLightGray, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(onClick = onNavigateToWhiteboard)
                            .testTag("whiteboard_button")
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MODE 01",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PremiumGray,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = if (isEnglish) "Whiteboard" else "醒目白板",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PremiumBlack
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PremiumBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CropLandscape,
                                contentDescription = "Whiteboard",
                                tint = PremiumDarkGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Teleprompter Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), spotColor = PremiumGray)
                            .background(PremiumBlack, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(onClick = onNavigateToTeleprompter)
                            .testTag("teleprompter_button")
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MODE 02",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PremiumGray,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = if (isEnglish) "Teleprompter" else "提词器",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Notes,
                                contentDescription = "Teleprompter",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Language Switcher
                Row(
                    modifier = Modifier
                        .background(PremiumLightGray.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (!isEnglish) Color.White else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { if (isEnglish) viewModel.toggleLanguage() }
                            )
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "中文",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isEnglish) PremiumBlack else PremiumGray
                            ),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isEnglish) Color.White else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { if (!isEnglish) viewModel.toggleLanguage() }
                            )
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "English",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEnglish) PremiumBlack else PremiumGray
                            ),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Decorative Footer Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MINIMALIST",
                        style = TextStyle(fontSize = 10.sp, color = PremiumGray, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
                    )
                    Box(modifier = Modifier.size(4.dp).background(PremiumLightGray, CircleShape))
                    Text(
                        text = "IMMERSIVE",
                        style = TextStyle(fontSize = 10.sp, color = PremiumGray, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
                    )
                    Box(modifier = Modifier.size(4.dp).background(PremiumLightGray, CircleShape))
                    Text(
                        text = "PRECISE",
                        style = TextStyle(fontSize = 10.sp, color = PremiumGray, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
                    )
                }
            }
        }
    }
}

@Composable
fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .graphicsLayer { this.alpha = alpha }
            .background(PremiumDarkGray, CircleShape)
    )
}

