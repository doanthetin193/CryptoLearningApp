# Hướng dẫn cấu hình API Keys

## Các bước setup nhanh
1. Copy file `local.properties.sample` thành `local.properties`
2. Thêm API keys của bạn:
   ```properties
   GEMINI_API_KEY=AIzaSyDbrQFONxMSK0hJ7a2gTuF4xC6vaUHnuLc
   NEWS_API_KEY=wCnvSkqSEBt+cZjlmHXLlC9fpNqEtVUAQo59aMT3IpI=
   ```
3. Build project

## Lấy API Keys
- **Gemini AI**: [Google AI Studio](https://makersuite.google.com/app/apikey)
- **News API**: [CoinTelegraph API](https://documenter.getpostman.com/view/5734027/RzZ6Hzr3)

## Lưu ý
- File `local.properties` đã được gitignore (an toàn)
- Không bao giờ commit API keys lên repo
