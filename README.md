# 📱 CryptoLearningApp

> **Ứng dụng học tập Cryptocurrency với AI Chat và Tin tức thời gian thực**

![Android](https://img.shields.io/badge/Android-API%2024+-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-orange.svg)
![AI Powered](https://img.shields.io/badge/AI-Gemini%20Powered-purple.svg)

## 🌟 Tính năng chính

### 📚 **Learning Module**
- Các bài học có cấu trúc về Cryptocurrency
- Quiz tương tác với scoring system
- Progress tracking cho từng user
- Onboarding flow cho người mới

### 🤖 **AI Chat Assistant**
- Integration với **Google Gemini AI**
- Chat contextual về crypto bằng tiếng Việt  
- Hỗ trợ learning với giải thích dễ hiểu
- Session management và history

### 📰 **Crypto News**
- Tin tức thời gian thực từ **CoinTelegraph API**
- Cập nhật thị trường crypto
- UI modern với pull-to-refresh

### 👤 **Profile Management**
- User profiles với gender, birth year
- Theme switching (Light/Dark)
- Settings và preferences

## 🏗️ Kiến trúc

- **Architecture**: MVVM + Clean Architecture
- **UI**: Jetpack Compose + Material 3 Design
- **DI**: Hilt (Dagger)
- **Navigation**: Navigation Compose
- **Network**: Retrofit + OkHttp
- **State Management**: StateFlow + ViewModel

## 🚀 Quick Start

### 📋 Yêu cầu
- Android Studio (latest)
- JDK 8+
- Android SDK (API 24+)

### ⚡ Setup nhanh (cho team members)
```bash
git clone https://github.com/[your-username]/CryptoLearningApp.git
cd CryptoLearningApp
cp local.properties.sample local.properties
# Thêm API keys vào local.properties
./gradlew assembleDebug
```

👉 **Chi tiết setup**: Xem [TEAM_SETUP_GUIDE.md](TEAM_SETUP_GUIDE.md)

### 🔑 API Keys cần thiết
- **Gemini AI**: https://aistudio.google.com/app/apikey
- **News API**: https://newsapi.org/register

👉 **Hướng dẫn chi tiết**: Xem [API_KEYS_README.md](API_KEYS_README.md)

## 📁 Cấu trúc Project

```
app/src/main/java/com/example/cryptolearningapp/
├── 🚀 CryptoLearningApplication.kt    # Application entry point
├── 📱 MainActivity.kt                 # Main activity
├── 📊 data/                          # Data layer
│   ├── 🌐 api/                       # API interfaces (Gemini, News)
│   ├── 📋 model/                     # Data models & entities
│   ├── 🗄️ local/                     # Room database
│   └── 🔄 repository/                # Data repositories
├── 🎯 domain/                        # Business logic layer
│   ├── 📝 usecase/                   # Use cases
│   └── 📊 model/                     # Domain models
├── 🎨 ui/                            # Presentation layer
│   ├── 📺 screen/                    # Compose screens
│   ├── 🧩 components/                # Reusable UI components
│   ├── 🎭 theme/                     # App theming
│   └── 🔄 viewmodel/                 # ViewModels
├── 💉 di/                            # Dependency injection
└── 🛠️ utils/                         # Utilities & helpers
```

## 🛠️ Build Commands

```bash
# Development build
./gradlew assembleDebug

# Release build  
./gradlew assembleRelease

# Run tests
./gradlew testDebugUnitTest

# Generate BuildConfig (nếu IDE báo lỗi)
./gradlew generateDebugBuildConfig
```

👉 **Troubleshooting**: Xem [BUILD_GUIDE.md](BUILD_GUIDE.md)

## 🎯 Learning Path

### 🏛️ **Theo Kiến trúc (Recommended cho Developers)**
1. **Entry Point**: `MainActivity.kt` + NavGraph
2. **DI Setup**: Hilt modules (Network, Repository, App)
3. **Data Flow**: API → Repository → UseCase → ViewModel → UI
4. **Navigation**: Compose navigation với bottom navigation
5. **State Management**: StateFlow patterns

### 👤 **Theo User Journey (Recommended cho UX)**
1. **Onboarding**: Profile creation → Setup preferences
2. **Home Screen**: Navigation overview → Feature discovery
3. **Learning Path**: Lessons → Quiz → Progress tracking
4. **AI Chat**: Chat interface → Gemini integration
5. **News Feed**: Real-time updates → Article reading

## 🔒 Bảo mật

- ✅ API keys được lưu trong `local.properties` (gitignored)
- ✅ BuildConfig pattern cho environment variables
- ✅ Không hardcode sensitive data trong source code
- ✅ Template files cho team collaboration

## 🤝 Contributing

1. Fork repository
2. Tạo feature branch: `git checkout -b feature/amazing-feature`
3. Follow setup guide: [TEAM_SETUP_GUIDE.md](TEAM_SETUP_GUIDE.md)
4. Commit changes: `git commit -m 'Add amazing feature'`
5. Push branch: `git push origin feature/amazing-feature`
6. Open Pull Request

### 📏 Code Standards
- Kotlin coding conventions
- MVVM architecture patterns
- Compose best practices
- Hilt DI guidelines

## 📸 Screenshots

| Home | Learning | AI Chat | News |
|------|----------|---------|------|
| 🏠 | 📚 | 🤖 | 📰 |

## 🔗 Links hữu ích

- [Android Developer Guide](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Hilt Dependency Injection](https://dagger.dev/hilt/)
- [Google Gemini AI](https://deepmind.google/technologies/gemini/)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **[Your Name]** - *Initial work* - [@your-username](https://github.com/your-username)

## 🙏 Acknowledgments

- Google Gemini AI cho AI capabilities
- CoinTelegraph cho crypto news API
- Android Jetpack team cho modern Android development

---

**⭐ Nếu project hữu ích, hãy star repo này! ⭐**
