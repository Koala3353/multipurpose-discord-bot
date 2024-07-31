package com.general_hello.commands;

import com.general_hello.commands.Database.DatabaseManager;
import com.general_hello.commands.Database.SQLiteDataSource;
import com.general_hello.commands.commands.GroupOfGames.Games.TriviaCommand;
import com.general_hello.commands.commands.GetData;
import com.general_hello.commands.commands.PrefixStoring;
import com.general_hello.commands.commands.Settings.SettingsData;
import com.general_hello.commands.commands.Utils.MoneyData;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Listener extends ListenerAdapter {
    public static CommandManager manager;
    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    public static HashMap<String, Integer> count = new HashMap<>();
    public static JDA jda;

    public Listener(EventWaiter waiter) {
        manager = new CommandManager(waiter);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        GetData getData = new GetData();
        getData.checkIfContainsData(event.getAuthor(), event);

        EmbedBuilder em;

        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }

        final long guildID = event.getGuild().getIdLong();
        String prefix = PrefixStoring.PREFIXES.computeIfAbsent(guildID, DatabaseManager.INSTANCE::getPrefix);
        String raw = event.getMessage().getContentRaw();

        if (event.getMessage().getContentRaw().equals(prefix + "commands")) {
            if (event.getAuthor().getId().equals(Config.get("owner_id"))) {
                em = new EmbedBuilder().setTitle("Command Count details!!!!").setColor(Color.red).setFooter("Commands used until now ").setTimestamp(LocalDateTime.now());
                em.addField("Command made by ", event.getAuthor().getName(), false);
                em.addField("Date", LocalDateTime.now().getMonthValue() + "/" + LocalDateTime.now().getDayOfMonth() + "/" + LocalDateTime.now().getYear(), false);
                em.addField("Total number of Commands used in this session....", CommandManager.commandNames.size() + " commands", false);
                em.addField("List of Commands used in this session....", commandsCount(), false);
                event.getAuthor().openPrivateChannel().complete().sendMessageEmbeds(em.build()).queue();
            }
        }

        try {
            if (SettingsData.pingForPrefix.get(event.getAuthor())) {
                if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())) {
                    event.getChannel().sendMessage("Psst. Check your **DMS** for the prefix of this bot").queue();
                    event.getMessage().getAuthor().openPrivateChannel().complete().sendMessage("The prefix for this bot is `" + prefix + "`").queue();
                }
            }
        } catch (Exception e) {
            SettingsData.pingForPrefix.put(event.getAuthor(), true);
        }

        jda = event.getJDA();

        System.out.println(prefix);
        if (raw.equalsIgnoreCase(prefix + "shutdown") && event.getAuthor().getId().equals(Config.get("owner_id"))) {
            shutdown(event, true);
            return;
        } else if (raw.equalsIgnoreCase(prefix + "shutdown") && event.getAuthor().getId().equals(Config.get("owner_id_partner"))) {
            shutdown(event, false);
            return;
        }

        if (raw.startsWith(prefix)) {
            try {
                manager.handle(event, prefix);
            } catch (InterruptedException | IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String commandsCount() {
        int x = 0;
        int size = CommandManager.commandNames.size();
        StringBuilder result = new StringBuilder();

        while (x < size) {
            String commandName = CommandManager.commandNames.get(x);
            result.append(x+1).append(".) ").append(commandName).append(" - ").append(count.get(commandName)).append("\n");
            x++;
        }

        return String.valueOf(result);
    }

    public static void shutdown(GuildMessageReceivedEvent event, boolean isOwner) {
        LOGGER.info("The bot " + event.getAuthor().getAsMention() + " is shutting down.\n" +
                "Thank you for using General_Hello's Code!!!");

        event.getChannel().sendMessage("Shutting down...").queue();
        event.getChannel().sendMessage("Bot successfully shutdown!").queue();
        EmbedBuilder em = new EmbedBuilder().setTitle("Shutdown details!").setColor(Color.red).setFooter("Shutdown on ").setTimestamp(LocalDateTime.now());
        em.addField("Shutdown made by ", event.getAuthor().getName(), false);
        em.addField("Date", LocalDateTime.now().getMonthValue() + "/" + LocalDateTime.now().getDayOfMonth() + "/" + LocalDateTime.now().getYear(), false);
        em.addField("Total number of Commands used during this session....", CommandManager.commandNames.size() + " commands", false);
        em.addField("List of Commands used during this session....", commandsCount(), false);
        event.getAuthor().openPrivateChannel().complete().sendMessageEmbeds(em.build()).queue();

        if (!isOwner) {
            User owner = event.getJDA().retrieveUserById(Config.get("owner_id")).complete();
            owner.openPrivateChannel().complete().sendMessageEmbeds(em.build()).queue();
        }


        event.getJDA().shutdown();
        SQLiteDataSource.ds.close();
        BotCommons.shutdown(event.getJDA());
    }

    public static void trivia(GuildMessageReceivedEvent event) {
        if (TriviaCommand.storeUser.contains(event.getAuthor())) {
            final String answer = event.getMessage().getContentRaw();

            String[] split = TriviaCommand.storeAnswer.get(event.getAuthor()).toLowerCase()
                    .split("\\s+");
            List<String> args = Arrays.asList(split).subList(1, split.length);

            if (args.contains(answer.toLowerCase())) {
                event.getChannel().sendMessage("Correct answer!!!!\n" +
                        "You got \uD83E\uDE99 5,000 for getting the correct answer").queue();
                final Double money = MoneyData.money.get(event.getAuthor());
                MoneyData.money.put(event.getAuthor(), money + 5000);
            } else {
                EmbedBuilder e = new EmbedBuilder();
                e.setTitle("Incorrect answer");
                e.setFooter("A correct answer gives you \uD83E\uDE99 5,000");
                e.addField("The correct answer is " + TriviaCommand.storeAnswer.get(event.getAuthor()).toUpperCase(), "Better luck next time", false);
                event.getChannel().sendMessageEmbeds(e.build()).queue();
            }
            TriviaCommand.storeUser.remove(event.getAuthor());
            TriviaCommand.storeAnswer.remove(event.getAuthor());
        }
    }
}
