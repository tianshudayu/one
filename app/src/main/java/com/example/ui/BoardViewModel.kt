package com.example.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BoardViewModel : ViewModel() {
    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    private val _teleprompterSpeed = MutableStateFlow(2f) // 1-3
    val teleprompterSpeed = _teleprompterSpeed.asStateFlow()

    fun updateSpeed(speed: Float) {
        _teleprompterSpeed.value = speed
    }

    private val _teleprompterFontSize = MutableStateFlow(48f) // 24-120
    val teleprompterFontSize = _teleprompterFontSize.asStateFlow()

    fun updateFontSize(size: Float) {
        _teleprompterFontSize.value = size
    }

    private val _isEnglish = MutableStateFlow(false)
    val isEnglish = _isEnglish.asStateFlow()

    fun toggleLanguage() {
        _isEnglish.value = !_isEnglish.value
    }
}
