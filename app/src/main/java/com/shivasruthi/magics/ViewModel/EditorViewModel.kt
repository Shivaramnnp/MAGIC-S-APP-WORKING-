package com.shivasruthi.magics.viewmodel

import androidx.lifecycle.ViewModel
import com.shivasruthi.magics.data.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditorViewModel : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions = _questions.asStateFlow()

    fun initializeQuestions(initialQuestions: List<Question>) {
        _questions.value = initialQuestions
    }

    fun onQuestionTextChanged(questionIndex: Int, newText: String) {
        _questions.value = _questions.value.mapIndexed { index, question ->
            if (index == questionIndex) {
                question.copy(questionText = newText)
            } else {
                question
            }
        }
    }

    fun onOptionTextChanged(questionIndex: Int, optionIndex: Int, newText: String) {
        _questions.value = _questions.value.mapIndexed { qIndex, question ->
            if (qIndex == questionIndex) {
                val newOptions = question.options.toMutableList()
                if(optionIndex < newOptions.size) {
                    newOptions[optionIndex] = newText
                }
                question.copy(options = newOptions)
            } else {
                question
            }
        }
    }

    fun onCorrectAnswerChanged(questionIndex: Int, newCorrectIndex: Int) {
        _questions.value = _questions.value.mapIndexed { index, question ->
            if (index == questionIndex) {
                question.copy(correctAnswerIndex = newCorrectIndex)
            } else {
                question
            }
        }
    }

    // --- THE FIX: New function to add a blank option ---
    fun addOption(questionIndex: Int) {
        _questions.value = _questions.value.mapIndexed { index, question ->
            if (index == questionIndex) {
                val newOptions = question.options.toMutableList()
                newOptions.add("")
                question.copy(options = newOptions)
            } else {
                question
            }
        }
    }
}