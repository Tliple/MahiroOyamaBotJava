package org.constanta.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Random;

public class RollCommand implements SlashCommand {

    private final Random random = new Random();

    @Override
    public String getName() {
        return "roll";
    }

    @Override
    public String getDescription() {
        return "Rolls number between min & max";
    }

    @Override
    public SlashCommandData getCommandData() {

        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.INTEGER, "min", "Minimal value", false)
                .addOption(OptionType.INTEGER, "max", "Maximal value", false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        long min = event.getOption("min", () -> 1L, OptionMapping::getAsLong);
        long max = event.getOption("max", () -> 100L, OptionMapping::getAsLong);

        if(min > max) {

            event.reply("❌ Минимальное значение не может быть больше максимального!")
                    .setEphemeral(false)
                    .queue();
            return;
        }

        long result = min + (long) (random.nextDouble() * (max - min + 1));
        event.reply(String.format("🎲 **Результат:** %d (%d-%d)", result, min, max))
                .queue();
    }
}
