package com.general_hello.commands.commands.DefaultCommands;

import com.general_hello.commands.commands.CommandContext;
import com.general_hello.commands.commands.CommandType;
import com.general_hello.commands.commands.ICommand;
import com.general_hello.commands.commands.Info.InfoUserCommand;
import com.general_hello.commands.commands.Register.Data;
import com.general_hello.commands.commands.User.UserPhoneUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws InterruptedException, IOException, SQLException {
        ArrayList<UserPhoneUser> userPhoneUsers = Data.userPhoneUsers;
        List<UserPhoneUser> collectedUsers = userPhoneUsers.stream().sorted(UserPhoneUser::compareTo).collect(Collectors.toList());

        List<User> toFilter = new ArrayList<>();

        int x = 0;
        while (x < collectedUsers.size()) {
            toFilter.add(collectedUsers.get(x).getDiscordUser());
            x++;
        }

        Set<User> set = new LinkedHashSet<>(toFilter);

        toFilter.clear();
        toFilter.addAll(set);
        HashMap<User, UserPhoneUser> userUserPhoneUserHashMap = Data.userUserPhoneUserHashMap;

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Leaderboard of Credits").setFooter("Who is the richest of the richest?").setTimestamp(OffsetDateTime.now());
        StringBuilder stringBuilder = new StringBuilder();

        x = 0;
        while (x < userUserPhoneUserHashMap.size()) {
            UserPhoneUser credits = userUserPhoneUserHashMap.get(toFilter.get(x));
            int rank = x+1;
            String rankShow;

            if (rank == 1) {
                rankShow = "🥇";
            } else if (rank == 2) {
                rankShow = "🥈";
            } else if (rank == 3) {
                rankShow = "🥉";
            } else {
                rankShow = "🔹";
            }

            DecimalFormat formatter = new DecimalFormat("#,###.00");
            Integer credits1 = credits.getCredits();
            stringBuilder.append(rankShow).append(" ** 💰").append(formatter.format(credits1)).append("** - ").append(credits.getDiscordUser().getAsTag()).append("\n");

            if (x == 15) {
                return;
            }
            x++;
        }

        embedBuilder.setDescription(stringBuilder.toString());
        embedBuilder.setColor(InfoUserCommand.randomColor());

        ctx.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "lb";
    }

    @Override
    public List<String> getAliases() {
        ArrayList<String > aliases = new ArrayList<>();
        aliases.add("leaderboard");
        aliases.add("richest");
        return aliases;
    }

    @Override
    public String getHelp(String prefix) {
        return "Shows you the richest of the richest!!!";
    }

    @Override
    public CommandType getCategory() {
        return CommandType.SPECIAL;
    }
}
