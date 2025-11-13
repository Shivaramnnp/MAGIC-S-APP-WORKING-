package com.shivasruthi.magics.data

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val questions: List<Question>
)

@Serializable
data class BoundingBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

@Serializable
data class Question(
    val questionNumber: Int,
    val pageNumber: Int,
    val questionText: String?,
    var questionImage: String? = null,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val boundingBox: BoundingBox? = null,
    val contains_latex: Boolean = false,
    val is_diagram: Boolean = false
)