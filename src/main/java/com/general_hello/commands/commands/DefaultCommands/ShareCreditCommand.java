package com.general_hello.commands.commands.DefaultCommands;

import com.general_hello.commands.commands.CommandContext;
import com.general_hello.commands.commands.CommandType;
import com.general_hello.commands.commands.ICommand;
import com.general_hello.commands.commands.Info.InfoUserCommand;
import com.general_hello.commands.commands.Register.Data;
import com.general_hello.commands.commands.User.UserPhoneUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.io.IOException;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class ShareCreditCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws InterruptedException, IOException, SQLException {
        if (ctx.getMessage().getMentionedUsers().isEmpty()) {
            ctx.getChannel().sendMessage("Kindly mention someone to give it to!").queue();
            return;
        }

        if (ctx.getArgs().size() > 2) {
            ctx.getChannel().sendMessage("Kindly state the money to be given!").queue();
            return;
        }
        User member = ctx.getMessage().getMentionedUsers().get(0);
        UserPhoneUser targetUserData = Data.userUserPhoneUserHashMap.get(member);

        if (targetUserData == null) {
            ctx.getChannel().sendMessage("The person didn't made an account yet!").queue();
        }
        User author = ctx.getAuthor();
        UserPhoneUser senderUserData = Data.userUserPhoneUserHashMap.get(author);
        int money = Integer.parseInt(ctx.getArgs().get(0));

        int tax = (money*3)/100;
        int toBeGiven = money - tax;

        if (senderUserData.getCredits() < money) {
            ctx.getChannel().sendMessage("You do not have enough money to give!").queue();
            return;
        }

        targetUserData.addCredits(toBeGiven);
        senderUserData.addCredits(-toBeGiven);
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Successful");
        embedBuilder.setDescription("Successfully given " + toBeGiven + " credits to " + targetUserData + "!\n" +
                tax + " credits has been deducted due to tax!");

        embedBuilder.setFooter("3% tax was applied");
        embedBuilder.setTimestamp(OffsetDateTime.now());
        embedBuilder.setColor(InfoUserCommand.randomColor());
        ctx.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(Button.of(ButtonStyle.DANGER, "TAXAMMOUNT", tax + " credits spent on tax")).queue();
    }

    @Override
    public String getName() {
        return "share";
    }

    @Override
    public String getHelp(String prefix) {
        return "Gives credits to other people!";
    }

    @Override
    public CommandType getCategory() {
        return CommandType.WALLET;
    }
}
