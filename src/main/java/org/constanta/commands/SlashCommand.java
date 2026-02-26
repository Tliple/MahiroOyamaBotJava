package org.constanta.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommand {

    String getName();
    String getDescription();

    void execute(SlashCommandInteractionEvent event);

    default SlashCommandData getCommandData() {

        return Commands.slash(getName(), getDescription());
    }
}
