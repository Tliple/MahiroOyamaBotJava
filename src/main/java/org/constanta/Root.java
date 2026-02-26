package org.constanta;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.constanta.commands.CommandManager;
import org.constanta.config.ConfigManager;
import org.constanta.database.GithubDatabase;
import org.constanta.database.UserDataManager;

import java.util.Timer;
import java.util.TimerTask;

public class Root {

    public static void main(String[] args) {

        System.out.println("Running bot...");

        ConfigManager config = new ConfigManager();
        if(!config.isConfigLoaded()) {

            System.err.println("No configuration. Shutdown...");
            return;
        }

        String token = config.getToken();
        if(token.isEmpty()) {

            System.err.println("Token error!");
            return;
        }

        System.out.println("Token ready!");
        System.out.println(config.getBotName() + " loading...");

        try {

            String repoOwner = config.getProperty("github.repo.owner", " ");
            String repoName = config.getProperty("github.repo.name", " ");
            String branch = config.getProperty("github.branch", "main");
            String githubToken = config.getProperty("github.token", " ");

            if (repoOwner.isEmpty() || repoName.isEmpty() || githubToken.isEmpty()) {
                System.err.println("❌ Не указаны настройки GitHub!");
                return;
            }

            GithubDatabase dataManager = new GithubDatabase(
                    repoOwner, repoName, branch, githubToken
            );

            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    dataManager.forceSave();
                }
            }, 30 * 60 * 1000, 30 * 60 * 1000);

            Activity activity = Activity.watching(config.getBotActivity());
            CommandManager commandManager = new CommandManager(dataManager);
            JDA client = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .addEventListeners(commandManager)
                    .setActivity(activity)
                    .build();

            client.awaitReady();

            String guildId = config.getTestGuildId();
            commandManager.registerCommands(client, "");

            System.out.println("Bot is running!");
            System.out.println("Activity: " + config.getBotActivity());

        } catch(Exception e) {

            System.err.println("Launch error: " + e.getMessage());
        }
    }
}