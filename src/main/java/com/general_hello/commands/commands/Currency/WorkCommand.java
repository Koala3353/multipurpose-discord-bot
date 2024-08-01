package com.general_hello.commands.commands.Currency;

import com.general_hello.commands.Database.DatabaseManager;
import com.general_hello.commands.commands.CommandContext;
import com.general_hello.commands.commands.CommandType;
import com.general_hello.commands.commands.Emoji.Emojis;
import com.general_hello.commands.commands.ICommand;
import com.general_hello.commands.commands.RankingSystem.LevelPointManager;
import com.general_hello.commands.commands.Register.Data;
import com.general_hello.commands.commands.User.UserPhoneUser;
import com.general_hello.commands.commands.Utils.UtilNum;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.util.HashMap;

public class WorkCommand implements ICommand {
    public static HashMap<User, OffsetDateTime> cooldown = new HashMap<>();
    @Override
    public void handle(CommandContext ctx) throws InterruptedException, IOException, SQLException {
        User author = ctx.getAuthor();
        if (cooldown.containsKey(author)) {
            OffsetDateTime offsetDateTime = cooldown.get(author);
            if (offsetDateTime.plusHours(1).isAfter(OffsetDateTime.now())) {
                ctx.getChannel().sendMessage("Woah.... You should take a break first! Kindly wait until " + TimeFormat.RELATIVE.format(offsetDateTime.plusHours(1)) + " before working again! Also, have a coffee ☕ :D").queue();
                return;
            }
        }

        cooldown.put(author, OffsetDateTime.now());

        int i = (int) LevelPointManager.calculateLevel(author);

        if (OffsetDateTime.now().getDayOfWeek().name().equalsIgnoreCase("sunday")) {
            i = 200;
        }

        UserPhoneUser userPhoneUser = Data.userUserPhoneUserHashMap.get(author);
        int credits = userPhoneUser.getCredits();
        UserPhoneUser bankUser = Data.userUserPhoneUserHashMap.get(ctx.getSelfUser());
        int bankCredits = bankUser.getCredits();

        int minRobOrFine = 0;
        int maxRobOrFine = credits;

        if (maxRobOrFine > bankCredits) {
            maxRobOrFine = bankCredits;
        }

        if (maxRobOrFine > 1500000) maxRobOrFine = 1500000;

        int randomNum = UtilNum.randomNum(minRobOrFine, maxRobOrFine);

        DecimalFormat formatter = new DecimalFormat("#,###.00");

        if (robOrNoRob(i)) {
            DatabaseManager.INSTANCE.setCredits(author.getIdLong(), randomNum);
            DatabaseManager.INSTANCE.setCredits(ctx.getSelfUser().getIdLong(), (-randomNum));
            if (randomNum == maxRobOrFine) {
                ctx.getChannel().sendMessage(author.getAsMention() + " Your boss is PROUD of you. 🤑\n" +
                        "Your salary for this not so illegal job is " + Emojis.credits + formatter.format(randomNum)).queue();
                return;
            }

            ctx.getChannel().sendMessage(author.getAsMention() + " Your boss is QUITE satisfied!\n" +
                    "Your salary for this not so illegal job is " + Emojis.credits + formatter.format(randomNum)).queue();
            //rob stuff
            return;
        }

        //fine stuff
        DatabaseManager.INSTANCE.setCredits(author.getIdLong(), (-randomNum));
        DatabaseManager.INSTANCE.setCredits(ctx.getSelfUser().getIdLong(), (randomNum));

        ctx.getChannel().sendMessage(author.getAsMention() + " " + robFailMessage() + " " + formatter.format(randomNum) + " because of your disobedience!!!").queue();
    }

    private boolean robOrNoRob(int rank) {
        int i = UtilNum.randomNum(0, 100);
        return i < 50+(rank/2);
    }

    private String robFailMessage() {
        int randomNum = UtilNum.randomNum(1, 5);

        switch (randomNum) {
            case 1:
                return "While working you took a visit to McDonalds and got caught by your boss! Losing";
            case 2:
                return "You have been caught sleeping on your job!!! Losing";
            case 3:
                return "A kid came up to you and said, **Can you give me some candy...** \uD83D\uDC40.... You felt pity and gave him a candy Costing you";
            case 4:
                return "Starbucks isn't a place to work, the strange thing is that you have bought everyone in the room a drink \uD83D\uDC4C. All in all you spent";
            case 5:
                return "While going to the place where you'll work you met your girlfriend and proposed to her! Costing you a dinner worth";
        }

        return "This is NOT supposed to appear if it does.... LUCKY YOU!!!";
    }

    @Override
    public String getName() {
        return "work";
    }

    @Override
    public String getHelp(String prefix) {
        return "Work the ignite bank!!!";
    }

    @Override
    public CommandType getCategory() {
        return CommandType.SPECIAL;
    }
}
