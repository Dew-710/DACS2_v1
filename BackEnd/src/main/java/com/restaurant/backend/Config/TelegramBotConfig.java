package com.restaurant.backend.Config;

import com.restaurant.backend.Service.impl.TelegramBotServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Slf4j
public class TelegramBotConfig {

    @Value("${telegram.bot.enabled:false}")
    private boolean enabled;

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotServiceImpl telegramBotService) {
        if (!enabled) {
            log.info("Telegram bot is disabled. Set telegram.bot.enabled=true to enable.");
            return null;
        }

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotService);
            log.info("✅ Telegram bot registered successfully!");
            log.info("📱 Bot username: {}", telegramBotService.getBotUsername());
            log.info("💬 Chat ID configured: {}", telegramBotService.getBotToken() != null ? "Yes" : "No");
            return botsApi;
        } catch (TelegramApiException e) {
            log.error("❌ Failed to register Telegram bot: {}", e.getMessage(), e);
            return null;
        }
    }
}


