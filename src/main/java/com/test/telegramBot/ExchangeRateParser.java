package com.test.telegramBot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ExchangeRateParser {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateParser.class);
    private static final String EXCHANGE_RATE_URL = "https://www.cbr-xml-daily.ru/daily_utf8.xml";
    private final Ben4inBot bot;

    public ExchangeRateParser(Ben4inBot bot) {
        this.bot = bot;
    }

    public String getExchangeRates() {
        try {
            logger.info("Подключение к сайту: {}", EXCHANGE_RATE_URL);
            Document document = Jsoup.connect(EXCHANGE_RATE_URL).get();

            Element usdElement = document.selectFirst("Valute:has(CharCode:contains(USD)) Value");
            Element eurElement = document.selectFirst("Valute:has(CharCode:contains(EUR)) Value");

            if (usdElement != null && eurElement != null) {
                String usdRate = usdElement.text();
                String eurRate = eurElement.text();
                return String.format("\uD83D\uDCB0 Курс валют ЦБ:\nUSD: %s ₽\nEUR: %s ₽", usdRate, eurRate);
            }
        } catch (IOException e) {
            logger.error("Ошибка при парсинге курса валют", e);
            return "Ошибка загрузки данных о курсе валют.";
        }
        return "Данные о курсе валют не найдены.";
    }

    @Scheduled(fixedRate = 120000) // Запуск каждые 2 минуты
    public void sendExchangeRatesToChat() {
        long chatId = 362122858;
        logger.info("Запуск отправки курса валют в чат: {}", chatId);

        String exchangeRates = getExchangeRates();
        if (!exchangeRates.isEmpty()) {
            logger.info("Отправка курса валют в чат: {}", exchangeRates);
            bot.sendMessage(chatId, exchangeRates);
        }
    }
}

