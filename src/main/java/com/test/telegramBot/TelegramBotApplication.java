package com.test.telegramBot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@SpringBootApplication
@EnableScheduling
public class TelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramBotApplication.class, args);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @DependsOn("telegramBotsApi")
    public Ben4inBot registerBot(@Value("${telegram.bot.token}") String botToken,
                                 @Value("${telegram.bot.username}") String botUsername,
                                 TelegramBotsApi botsApi,
                                 GeminiApiClient geminiApiClient) throws TelegramApiException {
        Ben4inBot bot = new Ben4inBot(botToken, botUsername, geminiApiClient);
        botsApi.registerBot(bot);
        return bot;
    }


    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public GeminiApiClient geminiApiClient(@Value("${geminiApiKey}") String apiKey,
                                           WebClient.Builder webClientBuilder,
                                           ObjectMapper objectMapper) {
        return new GeminiApiClient(apiKey, webClientBuilder, objectMapper);
    }

    @Bean
    public HolidaysParser holidaysParser(Ben4inBot bot) {
        return new HolidaysParser(bot);
    }

}