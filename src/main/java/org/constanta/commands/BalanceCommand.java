package org.constanta.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.constanta.database.GithubDatabase;
import org.constanta.database.UserDataManager;

import java.awt.*;

public class BalanceCommand implements SlashCommand{

    private final GithubDatabase dataManager;

    public BalanceCommand(GithubDatabase dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public String getName() {
        return "balance";
    }

    @Override
    public String getDescription() {
        return "Check balance.";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        User target = event.getOption("user", event::getUser, OptionMapping::getAsUser);
        int realBalance = dataManager.getBalance(target.getId());
        int balance = event.getOption("fake", () -> realBalance, OptionMapping::getAsInt);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.CYAN);

        if (target.getId().equals(event.getUser().getId())) {
            embed.setTitle("Баланс");
            embed.setDescription("\uD83C\uDF6C \uD83D\uDC49 **" + String.valueOf(balance) + "**");
        } else {
            embed.setTitle("Баланс " + target.getName());
            embed.setDescription("\uD83C\uDF6C \uD83D\uDC49 **" + String.valueOf(balance) + "**");
        }

        embed.setThumbnail(target.getEffectiveAvatarUrl());

        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.USER, "user", "To check user's balance.", false)
                .addOption(OptionType.INTEGER, "fake", "To fake balance", false);
    }
}
