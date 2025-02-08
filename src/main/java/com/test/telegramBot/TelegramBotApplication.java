package com.test.telegramBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class TelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramBotApplication.class, args);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    @DependsOn("telegramBotsApi")
    public Ben4inBot registerBot(@Value("${telegram.bot.token}") String botToken,
                                 @Value("${telegram.bot.username}") String botUsername,
                                 TelegramBotsApi botsApi) throws TelegramApiException {
        Ben4inBot bot = new Ben4inBot(botToken, botUsername);
        botsApi.registerBot(bot);
        return bot;
    }
}


