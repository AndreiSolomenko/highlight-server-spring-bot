package com.onrender.highlightserverspring;

import net.sourceforge.tess4j.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OCRController {

    private static final String TELEGRAM_BOT_TOKEN = "6895841707:AAEWTxVbJUUMeVCAPBwXWf9UxhHfvhLhOx0";
    private static final String TELEGRAM_CHAT_ID = "875602491";

    @PostMapping("/api/process-image")
    public Map<String, String> processImage(@RequestParam("image") MultipartFile image, @RequestParam("language") String language) {
        try {
            Map<String, String> response = new HashMap<>();

            if (image == null || image.isEmpty()) {
//                 throw new IllegalArgumentException("No image loaded.");
                System.err.println("Error: No image loaded.");

                response.put("Error:", "No image loaded.");
                return response;
            }
            if (language == null || language.trim().isEmpty()) {
//                 throw new IllegalArgumentException("The language is not specified.");
                System.err.println("Error: The language is not specified.");

                response.put("Error:", "The language is not specified.");
                return response;
            }

//            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
//            ITesseract instance = new Tesseract();
//
//            instance.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
//            instance.setLanguage(language);

//            String recognizedText = instance.doOCR(bufferedImage);

            String recognizedText = "HELLO WORLD!!!";

            response.put("text", recognizedText);


            sendTelegramImageAndText(image, language, recognizedText);


            return response;

        } catch (Exception e) {
            throw new RuntimeException("OCR error", e);
        }
    }


    private void sendTelegramImageAndText(MultipartFile image, String language, String recognizedText) {
        try {
            // 1. Відправка зображення
            String urlPhoto = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendPhoto";

            HttpRequest.BodyPublisher body = buildMultipartBody(image, TELEGRAM_CHAT_ID, language, recognizedText);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlPhoto))
                    .header("Content-Type", "multipart/form-data; boundary=---011000010111000001101001")
                    .POST(body)
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            System.err.println("Помилка при надсиланні в Telegram: " + e.getMessage());
        }
    }

    private HttpRequest.BodyPublisher buildMultipartBody(MultipartFile image, String chatId, String language, String recognizedText) throws IOException {
        String boundary = "---011000010111000001101001";
        String CRLF = "\r\n";

        StringBuilder metadataPart = new StringBuilder();
        metadataPart.append("--").append(boundary).append(CRLF);
        metadataPart.append("Content-Disposition: form-data; name=\"chat_id\"").append(CRLF).append(CRLF);
        metadataPart.append(chatId).append(CRLF);

        metadataPart.append("--").append(boundary).append(CRLF);
        metadataPart.append("Content-Disposition: form-data; name=\"caption\"").append(CRLF).append(CRLF);
        metadataPart.append("Запит:\n").append(language).append("\n\nВідповідь:\n").append(recognizedText).append(CRLF);

        metadataPart.append("--").append(boundary).append(CRLF);
        metadataPart.append("Content-Disposition: form-data; name=\"photo\"; filename=\"image.jpg\"").append(CRLF);
        metadataPart.append("Content-Type: image/jpeg").append(CRLF).append(CRLF);

        byte[] imageBytes = image.getBytes();
        byte[] metaBytes = metadataPart.toString().getBytes();
        byte[] endBytes = (CRLF + "--" + boundary + "--" + CRLF).getBytes();

        return HttpRequest.BodyPublishers.ofByteArrays(
                java.util.List.of(metaBytes, imageBytes, endBytes)
        );
    }


    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "is working";
    }


    @GetMapping("/")
    public String root() {
        return "ok";
    }
}
