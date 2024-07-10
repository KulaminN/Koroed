package com.example.Koroed.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Vector;

import static java.lang.Math.toIntExact;

@Component
public class Koroed implements SpringLongPollingBot,LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    int Scores, maxScores, counter;
    Vector<String> questions = new Vector<>();
    Vector<String> answers = new Vector<>();

    private final String token;

    public Koroed(@Value("${token}") String token) {
        this.token = token;

        questions.add("Сколько цветов в радуге?");
        questions.add("Какой город является столицей Франции?");
        questions.add("Как называется самый высокий горный хребет в мире?");
        questions.add("Кто написал \"Войну и мир\"?");
        questions.add("Как называется самое большое животное на Земле?");

        answers.add("семь");
        answers.add("шесть");
        answers.add("Париж");
        answers.add("Стамбул");
        answers.add("Гималаи");
        answers.add("Эверест");
        answers.add("Лев Толстой");
        answers.add("Александр Пушкин");
        answers.add("Синий кит");
        answers.add("Слон");
        telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        String message_text = "";
        if (update.hasMessage() && update.getMessage().hasText()) {
            message_text = update.getMessage().getText();
        }
        if (message_text.equals("/start")) {
            message_text = "Добро пожаловать на увлекательную викторину, где вас ждут простые вопросы и захватывающие ответы!\n И так Первый вопрос: " + questions.get(0);
            Scores = 0;
            maxScores = 5;
            long chat_id = update.getMessage().getChatId();
            Send(message_text, chat_id);
        } else if (update.hasCallbackQuery()) {
            System.out.println("Here");

            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            String answer;
            if (call_data.equals("правильно")) {
                if(Scores+1 == maxScores){
                    answer = "Поздравляю с победой!";
                    EditMessageText new_message = EditMessageText
                            .builder()
                            .chatId(chat_id)
                            .messageId(toIntExact(message_id))
                            .text(answer)
                            .build();
                    try {
                        telegramClient.execute(new_message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    Scores = 0;
                    counter = 0;
                }else {
                    answer = "Правильно! Твои очки " + ++Scores + "/" + maxScores + "\n" + questions.get(Scores);
                    counter += 2;
                    Edit(answer, message_id, chat_id);
                }
            } else {
                answer = "Неправильно. Твои очки " + Scores + "/" + maxScores + "\n" + questions.get(Scores);
                Edit(answer, message_id, chat_id);
            }

        }
    }
    void Edit(String answer,long message_id,long chat_id){
            Vector<InlineKeyboardButton> button = new Vector<>();
            button.add(InlineKeyboardButton
                    .builder()
                    .text(answers.get(counter))
                    .callbackData("правильно")
                    .build());
            button.add(InlineKeyboardButton
                    .builder()
                    .text(answers.get(counter + 1))
                    .callbackData("неправильно")
                    .build());

            EditMessageText new_message = EditMessageText
                    .builder()
                    .chatId(chat_id)
                    .messageId(toIntExact(message_id))
                    .text(answer)
                    .replyMarkup(InlineKeyboardMarkup
                            .builder()
                            .keyboardRow(
                                    new InlineKeyboardRow(button)

                            ).build())
                    .build();
            try {
                telegramClient.execute(new_message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    void Send(String message_text,long chat_id){
        Vector<InlineKeyboardButton> buttons = new Vector<>();
        buttons.add(InlineKeyboardButton
                .builder()
                .text(answers.get(counter))
                .callbackData("правильно")
                .build());
        buttons.add(InlineKeyboardButton
                .builder()
                .text(answers.get(counter + 1))
                .callbackData("неправильно")
                .build());

        SendMessage message = SendMessage
                .builder()
                .chatId(chat_id)
                .text(message_text)
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(
                                new InlineKeyboardRow(buttons)

                        ).build())
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        System.out.println("Registered bot running state is: " + botSession.isRunning());
    }
}


