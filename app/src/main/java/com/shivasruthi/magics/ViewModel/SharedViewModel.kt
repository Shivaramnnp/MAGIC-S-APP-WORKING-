package com.shivasruthi.magics.viewmodel

import androidx.lifecycle.ViewModel
import com.shivasruthi.magics.data.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions = _questions.asStateFlow()

    fun setQuestions(newQuestions: List<Question>) {
        _questions.value = newQuestions
    }
}