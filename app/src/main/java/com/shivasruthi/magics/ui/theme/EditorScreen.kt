package com.shivasruthi.magics.ui.theme

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.shivasruthi.magics.data.Question
import com.shivasruthi.magics.viewmodel.EditorViewModel
import com.shivasruthi.magics.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

private fun needsEditing(question: Question): Boolean {
    return question.correctAnswerIndex == -1 || question.options.any { it.isBlank() }
}

@Composable
fun EditorScreen(
    navController: NavController,
    viewModel: EditorViewModel,
    sharedViewModel: SharedViewModel
) {
    val questionsFromShared by sharedViewModel.questions.collectAsState()
    val editorQuestions by viewModel.questions.collectAsState()

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(questionsFromShared) {
        if (questionsFromShared.isNotEmpty()) {
            viewModel.initializeQuestions(questionsFromShared)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            itemsIndexed(editorQuestions) { index, question ->
                QuestionEditCard(
                    questionNumber = index + 1,
                    question = question,
                    onQuestionChange = { newText ->
                        viewModel.onQuestionTextChanged(index, newText)
                    },
                    onOptionChange = { optionIndex, newText ->
                        viewModel.onOptionTextChanged(index, optionIndex, newText)
                    },
                    onCorrectAnswerChange = { newCorrectIndex ->
                        viewModel.onCorrectAnswerChanged(index, newCorrectIndex)
                    },
                    onAddOption = {
                        viewModel.addOption(index)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        val currentIndex = lazyListState.firstVisibleItemIndex
                        val nextIndex = editorQuestions.drop(currentIndex + 1).indexOfFirst { needsEditing(it) }
                        if (nextIndex != -1) {
                            lazyListState.animateScrollToItem(index = currentIndex + 1 + nextIndex)
                        }
                    }
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Jump to Next Edit")
            }
            Spacer(modifier = Modifier.height(16.dp))
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        val currentIndex = lazyListState.firstVisibleItemIndex
                        val prevIndex = editorQuestions.take(currentIndex).indexOfLast { needsEditing(it) }
                        if (prevIndex != -1) {
                            lazyListState.animateScrollToItem(index = prevIndex)
                        }
                    }
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Jump to Previous Edit")
            }
        }
    }
}

@Composable
fun QuestionEditCard(
    questionNumber: Int,
    question: Question,
    onQuestionChange: (String) -> Unit,
    onOptionChange: (Int, String) -> Unit,
    onCorrectAnswerChange: (Int) -> Unit,
    onAddOption: () -> Unit
) {
    val cardBorderColor = when {
        question.correctAnswerIndex == -1 -> MaterialTheme.colorScheme.error
        question.options.any { it.isBlank() } -> Color(0xFFFFC107) // Amber/Yellow
        else -> Color(0xFF00C853) // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.5.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question $questionNumber",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Page ${question.pageNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))


            if (question.questionImage != null) {
                AsyncImage(
                    model = Uri.parse(question.questionImage),
                    contentDescription = "Question Content Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.FillWidth
                )
            }

            if (question.is_diagram) {
                if (question.contains_latex) {
                    LatexView(text = question.questionText ?: "Diagram Question")
                } else {
                    Text(text = question.questionText ?: "Diagram Question", modifier = Modifier.padding(bottom = 8.dp))
                }
            } else {
                if (question.contains_latex) {
                    LatexView(text = question.questionText ?: "")
                } else {
                    OutlinedTextField(
                        value = question.questionText ?: "",
                        onValueChange = onQuestionChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Question Text") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Options:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            question.options.forEachIndexed { index, optionText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (index == question.correctAnswerIndex),
                            onClick = { onCorrectAnswerChange(index) }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (index == question.correctAnswerIndex),
                        onClick = { onCorrectAnswerChange(index) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    if (question.contains_latex) {
                        LatexView(text = optionText, modifier = Modifier.weight(1f))
                    } else {
                        OutlinedTextField(
                            value = optionText,
                            onValueChange = { newText -> onOptionChange(index, newText) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (question.options.size < 6) {
                TextButton(
                    onClick = onAddOption,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Option", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Option")
                }
            }
        }
    }
}