package com.test.telegramBot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Component
public class HolidaysParser {
    private static final Logger logger = LoggerFactory.getLogger(HolidaysParser.class);
    private static final String HOLIDAYS_URL = "https://www.calend.ru/day/";
    private final Ben4inBot bot;

    public HolidaysParser(Ben4inBot bot) {
        this.bot = bot;
    }

    public List<String> getHolidays() {
        List<String> holidays = new ArrayList<>();
        try {
            logger.info("Подключение к сайту: {}", HOLIDAYS_URL);
            Document document = Jsoup.connect(HOLIDAYS_URL).get();

            LocalDate today = LocalDate.now();
            String todayId = "div_" + today.getYear() + "-" + today.getMonthValue() + "-" + today.getDayOfMonth();

            holidays.add("\ud83c\udf89 Праздники сегодня:");
            Elements todayHolidays = document.select("div[id='" + todayId + "'] p a[href^='/holidays/']");
            for (Element holiday : todayHolidays) {
                String title = holiday.text();
                String link = holiday.absUrl("href");
                holidays.add(title + " - " + link);
            }
        } catch (IOException e) {
            logger.error("Ошибка при парсинге страницы", e);
            holidays.add("Ошибка загрузки данных.");
        }
        return holidays;
    }

    public List<String> getHistory() {
        List<String> history = new ArrayList<>();
        try {
            logger.info("Подключение к сайту: {}", HOLIDAYS_URL);
            Document document = Jsoup.connect(HOLIDAYS_URL).get();

            history.add("\ud83d\udcdc Хроника дня в истории:");
            Elements historySections = document.select(".caption.days_section:has(.title a[href^='/events/'])");

            int count = 0;
            for (Element section : historySections) {
                if (count >= 6) break; // Ограничение до 6 событий

                Element yearElement = section.selectFirst("em");
                Element linkElement = section.selectFirst(".title a[href^='/events/']");

                if (yearElement != null && linkElement != null) {
                    String year = yearElement.text().trim();
                    String eventText = linkElement.text().trim();
                    String eventUrl = "https://www.calend.ru" + linkElement.attr("href");
                    history.add(year + " — " + eventText + " - " + eventUrl);
                    count++;
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при парсинге страницы", e);
            history.add("Ошибка загрузки данных.");
        }
        return history;
    }

    @Scheduled(fixedRate = 120000)
    public void sendHolidaysToChat() {
        long chatId = 362122858;
        logger.info("Запуск отправки праздников в чат: {}", chatId);

        List<String> holidays = getHolidays();
        if (!holidays.isEmpty()) {
            String holidayMessage = String.join("\n", holidays);
            logger.info("Отправка праздников в чат: {}", holidayMessage);
            bot.sendMessage(chatId, holidayMessage);
        }

        List<String> history = getHistory();
        if (!history.isEmpty()) {
            String historyMessage = String.join("\n", history);
            logger.info("Отправка хронологии в чат: {}", historyMessage);
            bot.sendMessage(chatId, historyMessage);
        }
    }
}
