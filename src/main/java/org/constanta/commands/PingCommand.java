package org.constanta.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCommand implements SlashCommand {

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "Ping check.";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        long time = System.currentTimeMillis();

        event.reply("Pong!").queue(response -> {

            long latency = System.currentTimeMillis() - time;

            response.editOriginal("Pong! Latency: " + latency + "ms").queue();
        });
    }
}