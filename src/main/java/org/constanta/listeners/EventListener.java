package org.constanta.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.constanta.config.ConfigManager;

public class EventListener extends ListenerAdapter {

    private ConfigManager config;

     public EventListener() {

         this.config = new ConfigManager();
     }

     @Override
     public void onReady(ReadyEvent event) {

         System.out.println("Bot connected to Discord!");
         System.out.println("Logged on " + event.getGuildAvailableCount() + " servers.");
     }

     @Override
     public void onMessageReceived(MessageReceivedEvent msg) {

         String context = msg.getMessage().getContentRaw();
         String prefix = config.getCommandPrefix();

         if(msg.getAuthor().isBot()) return;
         if(!context.startsWith(prefix)) return;

         String withoutPrefix = context.substring(prefix.length());
         String[] parts = withoutPrefix.split(" ");
         String command = parts[0].toLowerCase();

     }
}
