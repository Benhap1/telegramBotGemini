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
        List<String> events = new ArrayList<>();
        try {
            logger.info("Подключение к сайту: {}", HOLIDAYS_URL);
            Document document = Jsoup.connect(HOLIDAYS_URL).get();

            // Получаем текущую дату в нужном формате (YYYY-M-D)
            LocalDate today = LocalDate.now();
            String todayId = "div_" + today.getYear() + "-" + today.getMonthValue() + "-" + today.getDayOfMonth();

            // Парсим все события на текущий день из всплывающего блока
            events.add("🎉 Праздники сегодня:");
            Elements todayHolidays = document.select("div[id='" + todayId + "'] p a[href^='/holidays/']");
            for (Element holiday : todayHolidays) {
                String title = holiday.text();
                String link = holiday.absUrl("href");
                events.add(title + " - " + link);
            }

        } catch (IOException e) {
            logger.error("Ошибка при парсинге страницы", e);
            events.add("Ошибка загрузки данных.");
        }
        return events;
    }


    @Scheduled(fixedRate = 120000)
    public void sendHolidaysToChat() {
      long chatId = 362122858;
//        long chatId = -1002384347165L;
        logger.info("Запуск отправки данных в чат: {}", chatId);

        List<String> events = getHolidays();
        if (events.isEmpty()) {
            logger.warn("Список событий пуст, сообщение не отправляется");
            return;
        }

        String message = String.join("\n", events);
        logger.info("Отправка сообщения в чат: {}", message);
        bot.sendMessage(chatId, message);
    }
}



