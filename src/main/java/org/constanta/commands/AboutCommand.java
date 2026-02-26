package org.constanta.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.constanta.config.ConfigManager;

import java.awt.*;

public class AboutCommand implements SlashCommand {

    private final ConfigManager config;

    public AboutCommand() {

        this.config = new ConfigManager();
    }

    @Override
    public String getName() {
        return "about";
    }

    @Override
    public String getDescription() {
        return "Display info about bot";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(255, 105, 180));
        embed.setTitle("**" + config.getBotName() + "**", "https://github.com/");
        embed.setDescription(

                "Discord bot, created by tliple on Java 25."
        );

        embed.addField("Version", config.getVersion(), true);

        embed.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());

        event.replyEmbeds(embed.build()).queue();
    }
}
