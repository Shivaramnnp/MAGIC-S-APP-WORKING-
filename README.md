# MagicS – Smart Study Assistant 📚✨

MagicS is a powerful Android application designed to revolutionize the way students study.  
Using the power of **Generative AI**, MagicS automatically converts your PDFs, notes, or textbooks into interactive **multiple-choice questions (MCQs)** — generating a personalized practice exam in seconds.

MagicS transforms passive reading into an effective and engaging learning experience by helping you:
- Understand concepts quickly  
- Practice instantly  
- Stay motivated while studying  
- Improve retention through active recall  

---

## ✨ Features

### 📄 PDF → Quiz Conversion  
Select any PDF document from your device, and MagicS extracts text & images to generate quizzes instantly.

### 🤖 AI-Powered Question Generation  
A generative AI model analyzes the extracted content and creates relevant MCQs based on the subject matter.

### 🎧 Engaging Processing Experience  
While the AI processes your file, MagicS displays animated motivational quotes to keep you focused.

### 📊 Instant Summary & Practice  
Once processing is complete, users can immediately review and practice the automatically generated questions.

### 🎨 Modern & Clean UI  
Built using **Jetpack Compose** for a sleek, responsive, and intuitive user experience.

---

## 🚀 How It Works

1. **Select a PDF:** Choose any PDF from your device.
2. **AI Processing:** MagicS analyzes the PDF using OCR and text extraction.
3. **Question Generation:** Extracted content is passed to an AI model which creates MCQs.
4. **Start Practicing:** Users are taken to a summary screen where they can begin practicing right away.

---

## 🛠️ Technology Stack

MagicS uses modern Android development tools and best engineering practices.

### 🎨 UI  
- Jetpack Compose  
- Smooth transitions and animations  
- Custom themes and responsive layout  

### 🏗 Architecture  
- MVVM (Model-View-ViewModel)  
- ViewModel + UIState separation  
- Modular and maintainable codebase  

### ⚙️ Asynchronous Programming  
- Kotlin Coroutines for non-blocking background work  
- Ensures a fluid user experience  

### 🧭 Navigation  
- Navigation Compose for managing screen flow  

### 📄 PDF Processing  
- Integrated OCR and PDF extraction libraries  

### 🤖 Generative AI  
- Multimodal AI model for analyzing context and generating MCQs  

---

## 🖥️ Screens

### **Processing Screen (`ProcessingScreen.kt`)**

This is the heart of the user experience — where the magic happens.

#### 🔧 Functionality:
- Accepts selected PDF URI  
- Initiates processing flow in `ProcessingViewModel`  
- Displays a sequence of motivational quotes  
- Navigates to quiz/summary screen on success  
- Shows a detailed error message on failure  

#### 🔄 State Management:
Uses `ProcessingUiState` with the following states:
- **Loading**  
- **Success**  
- **Error**

UI reacts dynamically to each state.

---

## 🔧 How to Build

To run MagicS on your device, follow these steps:

### ✔ Requirements
- Android Studio (latest recommended)
- API key for your selected Generative AI provider (e.g., Google AI Studio)

### ✔ Steps
1. Clone the repository:
   ```bash
   git clone https://your-repository-url/MagicS.git
   ```
2. Open the project in **Android Studio**.  
3. Add your AI API key inside:
   ```
   secrets.defaults.properties
   ```
4. Sync, build, and run the project.

---

## 🔮 Future Roadmap

Upcoming features planned for MagicS:

- [ ] Support for more document formats (.docx, images)  
- [ ] User accounts + quiz history tracking  
- [ ] Custom quiz settings (difficulty, # of questions, etc.)  
- [ ] Flashcard generation  
- [ ] Cloud sync of quizzes across devices  
- [ ] Real-time analytics dashboard  
- [ ] Improved adaptive AI learning system  

---

## 💬 Motivational Quote Engine  
MagicS includes a custom set of motivational quotes that display during PDF processing to keep users inspired and focused.

---

## 📌 Contributing
Contributions are welcome! Feel free to submit issues or open pull requests to improve MagicS.

---

## 🧑‍💻 Authors  
MagicS is actively developed by passionate Android developers dedicated to improving learning through technology.

---

