package org.constanta.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {

    private Properties properties;

    public ConfigManager() {

        properties = new Properties();

        try {

            FileInputStream fis = new FileInputStream("config.properties");
            properties.load(fis);
            fis.close();

            System.out.println("File config.properties loaded!");
        } catch (IOException e) {

            System.err.println("Can't find file config.properties!");
        }
    }

    public String getProperty(String name, String def) {

        return properties.getProperty(name, def);
    }

    public String getToken() {

        return properties.getProperty("discord.token", "");
    }

    public String getVersion() {

        return properties.getProperty("version", "0.0.0");
    }

    public String getCommandPrefix() {

        return properties.getProperty("discord.command.prefix", ">");
    }

    public String getTestGuildId() {

        return properties.getProperty("discord.test.guild.id", "Nothing...");
    }

    public String getBotName() {

        return properties.getProperty("bot.name", "Discord Bot");
    }

    public String getBotActivity() {

        return properties.getProperty("bot.activity", "Watching you");
    }

    public boolean isConfigLoaded() {

        return !properties.isEmpty();
    }
}