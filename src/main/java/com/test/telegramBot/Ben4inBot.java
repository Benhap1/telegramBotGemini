package com.test.telegramBot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;

@Component
public class Ben4inBot extends TelegramLongPollingBot {

    private final String botUsername;

    public Ben4inBot(@Value("${telegram.bot.token}") String botToken,
                     @Value("${telegram.bot.username}") String botUsername) {
        super(botToken);
        this.botUsername = botUsername;
    }

    @Value("${geminiApiKey}")
    private String geminiApiKey;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().trim();
            long chatId = update.getMessage().getChatId();

            if (!messageText.toLowerCase().startsWith("bot ")) {
                return;
            }

            String userRequest = messageText.substring(4).trim();
            String response = getGeminiResponse(userRequest);

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setParseMode("MarkdownV2");
            message.setText(escapeMarkdown(response));

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private String escapeMarkdown(String text) {
        return text.replaceAll("([*_\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }

    private String getGeminiResponse(String prompt) {
        int retries = 3;
        int delay = 2000;

        for (int i = 0; i < retries; i++) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;
                HttpPost httpPost = new HttpPost(apiUrl);
                httpPost.setHeader("Content-Type", "application/json");

                String json = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + prompt + "\" }] }] }";
                httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    int statusCode = response.getCode();

                    if (statusCode == 429) {
                        System.out.println("Превышен лимит запросов. Ждем " + delay + " мс...");
                        Thread.sleep(delay);
                        delay = Math.min(delay * 2, 10000); // Ограничение максимальной задержки до 10 сек
                        continue;
                    }
                    if (statusCode != 200) {
                        throw new IOException("Ошибка API: " + statusCode);
                    }

                    String responseBody = EntityUtils.toString(response.getEntity());
                    JsonNode jsonNode = new ObjectMapper().readTree(responseBody);

                    return jsonNode.at("/candidates/0/content/parts/0/text").asText();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Восстанавливаем прерванное состояние потока
                return "Ошибка запроса: выполнение прервано.";
            }
        }
        return "Ошибка обработки запроса. Попробуйте позже.";
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}


