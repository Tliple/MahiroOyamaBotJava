package org.constanta.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.constanta.database.GithubDatabase;
import org.constanta.database.UserDataManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandManager extends ListenerAdapter {

    private final List<SlashCommand> commands = new ArrayList<>();
    private final GithubDatabase dataManager;

    public CommandManager(GithubDatabase dataManager) {

        this.dataManager = dataManager;

        registerCommand(new FantikCommand(dataManager));
        registerCommand(new BalanceCommand(dataManager));
        registerCommand(new PingCommand());
        registerCommand(new AboutCommand());
        registerCommand(new RollCommand());
    }

    private void registerCommand(SlashCommand command) {

        commands.add(command);
        System.out.println("Registred command: /" + command.getName());
    }

    public void registerCommands(JDA client, String guildId) {

        List<SlashCommandData> commandData = new ArrayList<>();

        for(SlashCommand command : commands) {

            commandData.add(command.getCommandData());
        }

        if(guildId != null && !guildId.isEmpty()) {

            Objects.requireNonNull(client.getGuildById(guildId)).updateCommands().addCommands(commandData).queue();
            System.out.println("Commands updated on test server!");
        } else {

            client.updateCommands().addCommands(commandData).queue();
            System.out.println("Commands updated!");
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        String commandName = event.getName();

        for(SlashCommand command : commands) {

            if(command.getName().equals(commandName)) {

                System.out.println("Command: " + commandName);

                command.execute(event);
                return;
            }
        }

        event.reply("Invalid command!").setEphemeral(true).queue();
    }
}
