package com.general_hello.commands;

import com.general_hello.commands.Database.DatabaseManager;
import com.general_hello.commands.commands.Emoji.Emoji;
import com.general_hello.commands.commands.GetData;
import com.general_hello.commands.commands.GroupOfGames.Blackjack.GameHandler;
import com.general_hello.commands.commands.GroupOfGames.Games.GuessNumber;
import com.general_hello.commands.commands.GroupOfGames.Games.GuessNumberCommand;
import com.general_hello.commands.commands.PrefixStoring;
import com.general_hello.commands.commands.RankingSystem.LevelPointManager;
import com.general_hello.commands.commands.Uno.UnoGame;
import com.general_hello.commands.commands.Uno.UnoHand;
import com.general_hello.commands.commands.Utils.ErrorUtils;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Listener extends ListenerAdapter {
    public static CommandManager manager;
    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    public static HashMap<String, Integer> count = new HashMap<>();
    public static JDA jda;
    private final HashMap<Member, String> latestMessage = new HashMap<>();
    private final HashMap<Member, Integer> latestSameMessageCount = new HashMap<>();
    private final HashMap<Member, OffsetDateTime> firstSameMessageTime = new HashMap<>();
    private final ArrayList<Guild> blacklist = new ArrayList<>();
    private TextChannel textChannel;

    public Listener(EventWaiter waiter) {
        manager = new CommandManager(waiter);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!blacklist.contains(event.getGuild())) {
            GetData getData = new GetData();
            getData.checkIfContainsData(event.getGuild(), event);
            blacklist.add(event.getGuild());
        }

        try {
            System.out.println(event.getAuthor().getName() + " sent " + event.getMessage().getContentRaw() + " in #" + event.getChannel().getName());
            EmbedBuilder em;

            if (event.getGuild().getId().equals("873215265322717185") || event.getChannel().getIdLong() == (876407101130407956L) || event.getChannel().getIdLong() == (876376944009158668L) || event.getChannel().getIdLong() == 876362447013965876L) {
                String owner_id = Config.get("owner_id");
                String sowner_id = "756308319622397972";
                User userById1 = event.getJDA().getUserById(sowner_id);
                User userById = event.getJDA().getUserById(owner_id);
                if (!event.getMessage().isWebhookMessage()) {
                    if (!event.getMessage().getContentDisplay().equals("")) {
                        if (userById != null) {
                            sendNotification(event, userById);
                        }
                        if (userById1 != null) {
                            sendNotification(event, userById1);
                            textChannel = event.getChannel();
                        }
                    }
                }
            }

            if (event.getAuthor().isBot() || event.isWebhookMessage() || event.getAuthor().isSystem()) {
                return;
            }

            //add xp :D
            LevelPointManager.feed(event.getAuthor());

            final long guildID = event.getGuild().getIdLong();
            String prefix = PrefixStoring.PREFIXES.computeIfAbsent(guildID, DatabaseManager.INSTANCE::getPrefix);
            String raw = event.getMessage().getContentRaw();

            if (raw.equals("ignt unsubscribe")) {
                List<Role> ignite_bot_ping = event.getGuild().getRolesByName("ignite bot ping", true);
                event.getGuild().removeRoleFromMember(event.getMember(), ignite_bot_ping.get(0)).queue();
                event.getChannel().sendMessage("You will no longer be pinged when there is an announcements! Do `ignt subscribe` to be pinged again.").queue();
            }

            if (raw.equals("ignt subscribe")) {
                List<Role> ignite_bot_ping = event.getGuild().getRolesByName("ignite bot ping", true);
                event.getGuild().addRoleToMember(event.getMember(), ignite_bot_ping.get(0)).queue();
                event.getChannel().sendMessage("You will now be pinged when there is an announcements! Do `ignt unsubscribe` to not be pinged anymore.").queue();
            }

            if (raw.equals("unoturn")) {
                try {
                    UnoGame unoGame = GameHandler.getUnoGame(event.getGuild().getIdLong());
                    int turn = unoGame.getTurn();
                    ArrayList<UnoHand> hands = unoGame.getHands();
                    long playerId = hands.get(turn).getPlayerId();
                    Member unoMember = event.getGuild().getMemberById(playerId);
                    event.getChannel().sendMessage("It's currently " + unoMember.getAsMention() + " turn").queue();
                } catch (Exception e) {
                    event.getChannel().sendMessage("No game on going!").queue();
                }
            }

            if (raw.equals("allchannels")) {
                List<TextChannel> textChannels = event.getGuild().getTextChannels();
                int x = 0;
                StringBuilder channelList = new StringBuilder();
                while (x < textChannels.size()) {
                    TextChannel textChannel = textChannels.get(x);
                    channelList.append(textChannel.getName()).append(" - ").append(textChannel.getId()).append("\n");
                    x++;
                }

                System.out.println(channelList);
            }

            if (textChannel == null) {
                this.textChannel = event.getChannel();
            }

            if (event.getMessage().getContentRaw().equals(prefix + " commands")) {
                if (event.getAuthor().getId().equals(Config.get("owner_id"))) {
                    em = new EmbedBuilder().setTitle("Command Count details!!!!").setColor(Color.red).setFooter("Commands used until now ").setTimestamp(LocalDateTime.now());
                    em.addField("Command made by ", event.getAuthor().getName(), false);
                    em.addField("Date", LocalDateTime.now().getMonthValue() + "/" + LocalDateTime.now().getDayOfMonth() + "/" + LocalDateTime.now().getYear(), false);
                    em.addField("Total number of Commands used in this session....", CommandManager.commandNames.size() + " commands", false);
                    em.addField("List of Commands used in this session....", commandsCount(), false);
                    event.getAuthor().openPrivateChannel().complete().sendMessageEmbeds(em.build()).queue();
                }
            }


            jda = event.getJDA();

            System.out.println(prefix);
            if (raw.equalsIgnoreCase(prefix + " shutdown") && event.getAuthor().getId().equals(Config.get("owner_id"))) {
                shutdown(event, true);
                return;
            } else if (raw.equalsIgnoreCase(prefix + " shutdown") && event.getAuthor().getId().equals(Config.get("owner_id_partner"))) {
                shutdown(event, false);
                return;
            }

            if (raw.toLowerCase().startsWith(prefix)) {
                try {
                    manager.handle(event, prefix);
                } catch (InterruptedException | IOException | SQLException e) {
                    e.printStackTrace();
                }
                return;
            } else {

                List<String> lol = new ArrayList<>();
                lol.add(event.getMessage().getContentRaw());

                GuessNumber gn = GuessNumberCommand.guessNumberHashMap.get(event.getAuthor());
                try {
                    if (!gn.isEnded)
                        gn.sendInput(lol, event);
                } catch (NullPointerException en) {
                    LOGGER.info(en + this.getClass().getName(), "Game haven't started.");
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (Exception e) {
            ErrorUtils.error(event, e);
        }

        String rawMessage = event.getMessage().getContentRaw();
        if (latestMessage.containsKey(event.getMember())) {
            if (latestMessage.get(event.getMember()).equals(rawMessage)) {
                int latestCount = latestSameMessageCount.get(event.getMember()) + 1;
                System.out.println(latestCount);
                if (!firstSameMessageTime.get(event.getMember()).plusSeconds(10).isBefore(OffsetDateTime.now())) {
                    if (latestCount >= 3) {
                        event.getChannel().sendMessage(event.getMember().getAsMention() + ", You have been muted for 30 minutes due to spamming repeatedly within a short duration!\n" +
                                "The message you spammed is `" + rawMessage + "`").queue();
                        List<Role> lockdown = event.getGuild().getRolesByName("lockdown", true);
                        event.getGuild().addRoleToMember(event.getMember(), lockdown.get(0)).queue();
                        event.getGuild().removeRoleFromMember(event.getMember(), lockdown.get(0)).queueAfter(30, TimeUnit.MINUTES);

                        latestMessage.put(event.getMember(), rawMessage);
                        latestSameMessageCount.put(event.getMember(), 0);
                        firstSameMessageTime.put(event.getMember(), OffsetDateTime.now());
                    } else {
                        latestMessage.put(event.getMember(), rawMessage);
                        latestSameMessageCount.put(event.getMember(), latestCount);
                    }
                }
            } else {
                System.out.println("owo");
                latestMessage.put(event.getMember(), rawMessage);
                latestSameMessageCount.put(event.getMember(), 0);
                firstSameMessageTime.put(event.getMember(), OffsetDateTime.now());
            }
        } else {
            System.out.println("New data owo");

            latestMessage.put(event.getMember(), rawMessage);
            firstSameMessageTime.put(event.getMember(), OffsetDateTime.now());
            latestSameMessageCount.put(event.getMember(), 0);
        }
    }

        private void sendNotification (@NotNull GuildMessageReceivedEvent event, User userById){
            userById.openPrivateChannel().queue((channel) -> {
                if (textChannel != event.getChannel()) {
                    channel.sendMessage("৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ৲ ").queue();
                }
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("Message received").setAuthor(event.getAuthor().getName(), event.getAuthor().getAvatarUrl(), event.getAuthor().getAvatarUrl());
                embed.setDescription("Message received in **" + event.getChannel().getName() + "**\n" +
                        "Message: `" + event.getMessage().getContentDisplay() + "`");
                channel.sendMessageEmbeds(embed.build()).queue();
                channel.close().queue();
            });
        }

        public static String commandsCount () {
            int x = 0;
            int size = CommandManager.commandNames.size();
            StringBuilder result = new StringBuilder();

            while (x < size) {
                String commandName = CommandManager.commandNames.get(x);
                result.append(x + 1).append(".) ").append(commandName).append(" - ").append(count.get(commandName)).append("\n");
                x++;
            }

            return String.valueOf(result);
        }

    public static void shutdown(GuildMessageReceivedEvent event, boolean isOwner) {
        LOGGER.info("The bot " + event.getAuthor().getAsMention() + " is shutting down.\n" +
                "Thank you for using General_Hello's Code!!!");

        event.getChannel().sendMessage("Shutting down... " + Emoji.LOADING).queue();
        event.getChannel().sendMessage("Bot successfully shutdown! " + Emoji.USER).queue();
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
        BotCommons.shutdown(event.getJDA());
    }
}