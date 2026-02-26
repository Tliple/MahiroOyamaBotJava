package org.constanta.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.constanta.database.GithubDatabase;
import org.constanta.database.UserDataManager;

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class FantikCommand implements SlashCommand{

    private final GithubDatabase dataManager;
    private final Random random = new Random();

    // Фразы для разных случаев (как в Python коде)
    private final String[] SUCCESS_PHRASES = {
            "ограбил банк на **{amount}** фантиков! 🏦",
            "нашел под подушкой **{amount}** фантиков! 🛏️",
            "выиграл в лотерею **{amount}** фантиков! 🎰",
            "получил зарплату **{amount}** фантиков! 💼",
            "нашел клад с **{amount}** фантиками! 💎",
            "собрал урожай фантиков на **{amount}** штук! 🌱",
            "выиграл в казино **{amount}** фантиков! 🎲",
            "нашел кошелек с **{amount}** фантиками! 👛",
            "продал ненужные вещи за **{amount}** фантиков! 🛒",
            "получил премию **{amount}** фантиков! 🏆"
    };

    private final String[] ALREADY_CLAIMED_PHRASES = {
            "Эй, {user}, ты уже получил свои фантики сегодня! Отдохни немного 😴",
            "{user}, фантики так просто не даются! Приходи завтра 🗓️",
            "Поле фантиков еще не выросло, {user}! Жди до завтра 🌱",
            "{user}, ты уже исчерпал лимит на сегодня! Завтра будет новый урожай 🌾",
            "Банк фантиков закрыт на пересчет, {user}! Возвращайся завтра 🏦",
            "{user}, твой сейф с фантиками уже заполнен! Жди до завтра 🔒"
    };

    private final String[] AMOUNT_PHRASES = {
            "Маловато будет... **{amount}** фантиков. В следующий раз повезет больше! 🍀",
            "Эх, всего **{amount}** фантиков... Но это лучше, чем ничего! 😅",
            "Скромненько, но со вкусом: **{amount}** фантиков! 🎀",
            "На мороженку хватит: **{amount}** фантиков! 🍦",
            "Карманные расходы: **{amount}** фантиков! 👝",
            "Неплохо! **{amount}** фантиков отправляются в копилку! 🐷",
            "Хороший улов: **{amount}** фантиков! 🎣",
            "В самый раз: **{amount}** фантиков! ⚖️",
            "Стабильный результат: **{amount}** фантиков! 📊",
            "Джекпот! **{amount}** фантиков! 🎰",
            "Ты сорвал куш в **{amount}** фантиков! 💰",
            "**{amount}** фантиков падают с неба! ☁️",
            "Мечты сбываются! **{amount}** фантиков! ✨"
    };

    public FantikCommand(GithubDatabase dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public String getName() {
        return "fantik";
    }

    @Override
    public String getDescription() {
        return "Получить ежедневный бонус фантиков";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();

        if (dataManager.hasClaimedToday(userId)) {
            handleAlreadyClaimed(event);
            return;
        }

        int amount = 12 + random.nextInt(44); // 44 = 55-12+1

        int oldBalance = dataManager.getBalance(userId);

        dataManager.addBalance(userId, amount);
        dataManager.setLastClaimedToday(userId);

        int newBalance = dataManager.getBalance(userId);

        String successPhrase = SUCCESS_PHRASES[random.nextInt(SUCCESS_PHRASES.length)]
                .replace("{amount}", String.valueOf(amount));

        String amountPhrase = AMOUNT_PHRASES[random.nextInt(AMOUNT_PHRASES.length)]
                .replace("{amount}", String.valueOf(amount));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎁 Ежедневный бонус!");
        embed.setDescription(event.getUser().getAsMention() + " " + successPhrase);
        embed.setColor(Color.GREEN);

        embed.addField("💰 Старый баланс", String.valueOf(oldBalance), true);
        embed.addField("➕ Получено", "+" + amount, true);
        embed.addField("💎 Новый баланс", String.valueOf(newBalance), true);
        embed.addField("📝 Комментарий", amountPhrase, false);

        LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        long hoursUntil = LocalDateTime.now().until(tomorrow, ChronoUnit.HOURS);
        long minutesUntil = LocalDateTime.now().until(tomorrow, ChronoUnit.MINUTES) % 60;

        embed.addField("⏳ Следующий бонус",
                String.format("Через %d ч %d мин (доступен %s)",
                        hoursUntil, minutesUntil,
                        tomorrow.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))),
                false);

        embed.setThumbnail(event.getUser().getEffectiveAvatarUrl());

        embed.setFooter("Сегодня: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        event.replyEmbeds(embed.build()).queue();
    }

    private void handleAlreadyClaimed(SlashCommandInteractionEvent event) {
        String phrase = ALREADY_CLAIMED_PHRASES[random.nextInt(ALREADY_CLAIMED_PHRASES.length)]
                .replace("{user}", event.getUser().getAsMention());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();
        long hoursUntil = now.until(midnight, ChronoUnit.HOURS);
        long minutesUntil = now.until(midnight, ChronoUnit.MINUTES) % 60;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("❌ Фантики уже получены!");
        embed.setDescription(phrase);
        embed.setColor(Color.RED);

        embed.addField("💰 Текущий баланс",
                String.valueOf(dataManager.getBalance(event.getUser().getId())), true);

        embed.addField("⏳ До следующего бонуса",
                String.format("%d ч %d мин", hoursUntil, minutesUntil), true);

        embed.setThumbnail(event.getUser().getEffectiveAvatarUrl());
        embed.setFooter("Приходи завтра!");

        event.replyEmbeds(embed.build()).queue();
    }
}
