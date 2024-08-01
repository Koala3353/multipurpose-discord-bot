package com.general_hello.commands.commands.Currency;

import com.general_hello.commands.commands.CommandContext;
import com.general_hello.commands.commands.CommandType;
import com.general_hello.commands.commands.Emoji.Emojis;
import com.general_hello.commands.commands.GetData;
import com.general_hello.commands.commands.ICommand;
import com.general_hello.commands.commands.Others.UpdateIgniteCoinsCommand;
import com.general_hello.commands.commands.Register.Data;
import com.general_hello.commands.commands.User.UserPhoneUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BalanceCommand implements ICommand {
    public static HashMap<Long, Integer> dataInTheSky = new HashMap<>();
    @Override
    public void handle(CommandContext ctx) throws InterruptedException, IOException, SQLException {
        User user = ctx.getAuthor();


        if (!ctx.getMessage().getMentionedMembers().isEmpty()) {
            user = ctx.getMessage().getMentionedUsers().get(0);
        }

        GetData getData = new GetData();
        getData.checkIfContainsData(user, ctx);

        UpdateIgniteCoinsCommand.loadData();

        UserPhoneUser userPhoneUser = Data.userUserPhoneUserHashMap.get(user);

        if (userPhoneUser == null) {
            ctx.getChannel().sendMessage("Kindly register with the bot by using `ignt register`").queue();
            return;
        }

        Integer balance;

        try {
            balance = userPhoneUser.getBalance();
        } catch (Exception e) {
            balance = null;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.CYAN);
        embedBuilder.setThumbnail("https://images-ext-2.discordapp.net/external/dSmBwljIOT0xUFDxjc5_KNUZx9g3dNviWTCkCZ6oleY/https/cdn.discordapp.com/emojis/718138332982280272.gif");
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        embedBuilder.setTitle(user.getName() + "'s Balance").setFooter("Nice balance you have 😀");
        embedBuilder.setDescription(Emojis.igntCoins + " Ignite Coins: **" + (balance == null ? "The ignite coins spreadsheet is being updated**\n" :"Press the button for your ignite coins**\n") +
                Emojis.credits + " Credits: ** " + formatter.format(userPhoneUser.getCredits()) + " credits**\n" +
                Emojis.shekels + " Shekels: **To arrive soon**");
        ctx.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(Button.of(ButtonStyle.PRIMARY, user.getIdLong() + ":balance", "View Coins").withEmoji(Emoji.fromEmote("ignt_coins", 905999722374905857L, false)).withDisabled(balance == null)).queue();

        if (balance != null) {
            dataInTheSky.put(user.getIdLong(), balance);
        }
    }

    @Override
    public CommandType getCategory() {
        return CommandType.WALLET;
    }

    @Override
    public String getName() {
        return "bal";
    }

    @Override
    public String getHelp(String prefix) {
        return "Shows your ignite coins!\n" +
                "Example: `" + prefix + getName() + "`";
    }

    @Override
    public List<String> getAliases() {
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("balance");
        aliases.add("coins");
        aliases.add("coin");
        return aliases;
    }
}
