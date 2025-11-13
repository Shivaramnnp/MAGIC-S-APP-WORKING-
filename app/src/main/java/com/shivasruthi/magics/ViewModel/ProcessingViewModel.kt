package com.shivasruthi.magics.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.content
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.shivasruthi.magics.BuildConfig
import com.shivasruthi.magics.data.BoundingBox
import com.shivasruthi.magics.data.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.UnknownHostException

private data class PageData(val pageNumber: Int, val ocrText: String, val bitmap: Bitmap)

sealed interface ProcessingUiState {
    object Loading : ProcessingUiState
    data class Success(val questions: List<Question>) : ProcessingUiState
    data class Error(val message: String) : ProcessingUiState
}

class ProcessingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ProcessingUiState>(ProcessingUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val jsonParser = Json { isLenient = true; ignoreUnknownKeys = true }

    fun startPdfProcessing(pfd: ParcelFileDescriptor, context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = ProcessingUiState.Loading
            if (!isNetworkAvailable(context)) {
                _uiState.value = ProcessingUiState.Error("No internet connection. Please connect and try again.")
                return@launch
            }
            try {
                val allQuestions = processPdfInBatches(pfd, context)
                if (allQuestions.isEmpty()) {
                    _uiState.value = ProcessingUiState.Error("The AI could not find any questions in the document.")
                } else {
                    _uiState.value = ProcessingUiState.Success(allQuestions.sortedWith(compareBy({ it.pageNumber }, { it.questionNumber })))
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is UnknownHostException -> "Could not connect to the server. Please check your internet connection."
                    is ServerException -> "The AI model is currently overloaded. Please try again in a few moments."
                    is com.google.ai.client.generativeai.type.ResponseStoppedException -> "The AI stopped processing. Reason: ${e.response.promptFeedback?.blockReason?.name}"
                    else -> e.message ?: "An unexpected error occurred."
                }
                _uiState.value = ProcessingUiState.Error(errorMessage)
                e.printStackTrace()
            }
        }
    }

    private suspend fun processPdfInBatches(pfd: ParcelFileDescriptor, context: Context): List<Question> {
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val pdfRenderer = PdfRenderer(pfd)
        val allQuestions = mutableListOf<Question>()
        val generativeModel = GenerativeModel("gemini-2.5-flash", BuildConfig.GEMINI_API_KEY)

        Log.d("BatchProcessor", "Step 1: Performing OCR on all ${pdfRenderer.pageCount} pages...")
        val allPagesData = (0 until pdfRenderer.pageCount).map { i ->
            val page = pdfRenderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val ocrResult = textRecognizer.process(InputImage.fromBitmap(bitmap, 0)).await()
            page.close()
            PageData(pageNumber = i + 1, ocrText = ocrResult.text, bitmap = bitmap)
        }
        Log.d("BatchProcessor", "OCR complete.")

        val batchSize = 15
        val pageBatches = allPagesData.chunked(batchSize)
        Log.d("BatchProcessor", "Created ${pageBatches.size} batches of up to $batchSize pages each.")

        for ((batchIndex, batch) in pageBatches.withIndex()) {
            Log.i("BatchProcessor", "Processing Batch ${batchIndex + 1}/${pageBatches.size}...")

            val batchWithContext = mutableListOf<PageData>()
            if (batchIndex > 0) {
                val previousBatch = pageBatches[batchIndex - 1]
                batchWithContext.add(previousBatch.last())
            }
            batchWithContext.addAll(batch)

            val inputForModel = mutableListOf<Content>()
            inputForModel.add(content { text(createMultimodalBatchPrompt()) })

            val bitmapMap = mutableMapOf<Int, Bitmap>()
            for (pageData in batchWithContext) {
                inputForModel.add(content { image(pageData.bitmap) })
                inputForModel.add(content { text("--- OCR TEXT FOR PAGE ${pageData.pageNumber} ---\n${pageData.ocrText}") })
                bitmapMap[pageData.pageNumber] = pageData.bitmap
            }

            val response = generateContentWithRetry(generativeModel, inputForModel)

            if (response != null) {
                val questionsFromBatch = parseAndHeal(response, bitmapMap, context)
                allQuestions.addAll(questionsFromBatch)
                Log.i("BatchProcessor", "Batch ${batchIndex + 1} successful. Found ${questionsFromBatch.size} questions.")
            } else {
                Log.e("BatchProcessor", "Batch ${batchIndex + 1} failed after multiple retries.")
            }
        }

        allPagesData.forEach { it.bitmap.recycle() }
        pdfRenderer.close()
        pfd.close()

        return allQuestions.distinctBy { it.questionText?.trim() }
    }

    private suspend fun generateContentWithRetry(
        model: GenerativeModel,
        prompt: List<Content>,
        maxRetries: Int = 3
    ): GenerateContentResponse? {
        var currentRetry = 0
        var waitTime = 2000L
        while (currentRetry < maxRetries) {
            try {
                return model.generateContent(*prompt.toTypedArray())
            } catch (e: ServerException) {
                currentRetry++
                Log.w("BatchProcessor_RETRY", "Model overloaded. Retry attempt $currentRetry of $maxRetries. Waiting ${waitTime}ms.")
                if (currentRetry >= maxRetries) {
                    Log.e("BatchProcessor_RETRY", "Batch failed after max retries.")
                    return null
                }
                delay(waitTime)
                waitTime *= 2
            } catch (e: Exception) {
                Log.e("BatchProcessor_RETRY", "A non-retryable error occurred: ${e.message}")
                throw e
            }
        }
        return null
    }

    private fun createMultimodalBatchPrompt(): String {
        val dollar = '$'
        return """
        You are an expert AI system that analyzes a batch of test paper pages and extracts all questions into a single, structured JSON object.

        **GOLDEN RULE FOR JSON:** Your output MUST be a single, perfectly valid JSON object.
        - A double quote character (") inside a JSON string value MUST be escaped with a backslash (\").
        - A backslash character (\) inside a JSON string value MUST be escaped with another backslash (\\).

        **CRITICAL RULES:**
        1.  **Find Correct Answer:** The correct answer is marked with a green checkmark icon (✓). Use this visual clue from the IMAGE to set the 0-based `correctAnswerIndex`. If no checkmark is visible, use -1.
        2.  **Handle Math for MathJax:** Convert ALL mathematical notation into valid LaTeX. Use single dollar signs `${dollar}` for inline math and double dollar signs `${dollar}${dollar}` for display math.
        3.  **Handle Missing Options:** You MUST provide exactly four options. If you find fewer, generate plausible but incorrect options to fill the remaining slots.
        4.  **Diagrams vs. Text:**
            - A question is a "diagram" ONLY if it contains a circuit, graph, or chart.
            - If it is a diagram, set `"is_diagram": true` and provide a `boundingBox`.
            - If it is NOT a diagram, set `"is_diagram": false` and DO NOT provide a `boundingBox`.
        5.  **Page Number:** For every question, you MUST correctly report its `pageNumber`.
        6.  **No Nulls:** You MUST NOT use `null` values. Use an empty string "" for missing text.

        **JSON OUTPUT SPECIFICATION:**
        - "questionNumber": (Integer)
        - "pageNumber": (Integer)
        - "questionText": (String)
        - "options": (Array of 4 Strings)
        - "correctAnswerIndex": (Integer)
        - "contains_latex": (Boolean)
        - "is_diagram": (Boolean)
        - "boundingBox": (Object, optional) - ONLY if `is_diagram` is true.

        Now, process the entire sequence of images and text provided.
        """.trimIndent()
    }

    private fun parseAndHeal(response: GenerateContentResponse, bitmapMap: Map<Int, Bitmap>, context: Context): List<Question> {
        val responseText = response.text ?: return emptyList()
        Log.d("BatchProcessor", "Raw AI Response: $responseText")
        val finalQuestions = mutableListOf<Question>()

        try {
            // --- THE DEFINITIVE FIX: Pre-sanitize the string to fix invalid LaTeX escapes ---
            // This regex finds any single backslash followed by letters (like \frac)
            // that is NOT already escaped (i.e., not preceded by another backslash).
            val regex = Regex("(?<!\\\\)\\\\([a-zA-Z]+)")
            val sanitizedText = cleanedJson(responseText).replace(regex) { matchResult ->
                "\\\\${matchResult.groupValues[1]}" // Replaces '\frac' with '\\frac'
            }

            val jsonElement = jsonParser.parseToJsonElement(sanitizedText)
            val questionsArray = jsonElement.jsonObject["questions"]?.jsonArray ?: return emptyList()

            for (questionElement in questionsArray) {
                try {
                    val questionJson = questionElement.jsonObject

                    val optionsArray = questionJson["options"]?.jsonArray?.map {
                        if (it is JsonNull) "" else it.jsonPrimitive.content
                    } ?: listOf("", "", "", "")

                    val question = Question(
                        questionNumber = questionJson["questionNumber"]?.jsonPrimitive?.int ?: 0,
                        pageNumber = questionJson["pageNumber"]?.jsonPrimitive?.int ?: 0,
                        questionText = questionJson["questionText"]?.jsonPrimitive?.content,
                        options = optionsArray,
                        correctAnswerIndex = questionJson["correctAnswerIndex"]?.jsonPrimitive?.int ?: -1,
                        contains_latex = questionJson["contains_latex"]?.jsonPrimitive?.boolean ?: false,
                        is_diagram = questionJson["is_diagram"]?.jsonPrimitive?.boolean ?: false,
                        boundingBox = questionJson["boundingBox"]?.jsonObject?.let {
                            BoundingBox(
                                x = it["x"]?.jsonPrimitive?.int ?: 0,
                                y = it["y"]?.jsonPrimitive?.int ?: 0,
                                width = it["width"]?.jsonPrimitive?.int ?: 0,
                                height = it["height"]?.jsonPrimitive?.int ?: 0
                            )
                        }
                    )

                    if (question.is_diagram && question.boundingBox != null) {
                        val sourceBitmap = bitmapMap[question.pageNumber]
                        if (sourceBitmap != null) {
                            val box = question.boundingBox
                            if (box.x >= 0 && box.y >= 0 && box.width > 0 && box.height > 0 && box.x + box.width <= sourceBitmap.width && box.y + box.height <= sourceBitmap.height) {
                                val croppedBitmap = Bitmap.createBitmap(sourceBitmap, box.x, box.y, box.width, box.height)
                                val imageUri = saveBitmapToCache(croppedBitmap, "q_img_${System.currentTimeMillis()}.png", context)
                                question.questionImage = imageUri?.toString()
                            }
                        }
                    }
                    finalQuestions.add(question)
                } catch (e: Exception) {
                    Log.e("BatchProcessor", "Skipping one malformed question object. Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("BatchProcessor", "Major JSON parsing failed: ${e.message}\nJSON Input: $responseText")
            return emptyList()
        }
        return finalQuestions
    }

    private fun cleanedJson(text: String) = text.replace("```json", "").replace("```", "").trim()

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private fun saveBitmapToCache(bitmap: Bitmap, fileName: String, context: Context): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "images")
            cacheDir.mkdirs()
            val file = File(cacheDir, fileName)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}