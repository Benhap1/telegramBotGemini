package com.test.telegramBot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Ben4inBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final GeminiApiClient geminiApiClient;

    public Ben4inBot(@Value("${telegram.bot.token}") String botToken,
                     @Value("${telegram.bot.username}") String botUsername,
                     GeminiApiClient geminiApiClient) {
        super(botToken);
        this.botUsername = botUsername;
        this.geminiApiClient = geminiApiClient;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().trim();
            long chatId = update.getMessage().getChatId();

            if (!messageText.toLowerCase().startsWith("bot ")) {
                return;
            }

            String userRequest = messageText.substring(4).trim();
            String response = geminiApiClient.getResponse(userRequest);

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

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}

