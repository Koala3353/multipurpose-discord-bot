package com.general_hello.commands.commands.DefaultCommands;

import com.general_hello.commands.Config;
import com.general_hello.commands.commands.CommandContext;
import com.general_hello.commands.commands.CommandType;
import com.general_hello.commands.commands.ICommand;
import com.general_hello.commands.commands.Info.InfoUserCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.io.IOException;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class DropCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws InterruptedException, IOException, SQLException {
        ctx.getMessage().delete().queue();
        if (ctx.getAuthor().getId().equals(Config.get("owner_id")) || ctx.getAuthor().getId().equals("owner_id_partner")) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("New Chest Drop!!!").setTimestamp(OffsetDateTime.now()).setColor(InfoUserCommand.randomColor());
            embedBuilder.setDescription("A new chest 🧰 has been found! **First igniter** to press the button below will get **-10,000 credits** to **50,000 credits**!\n" +
                    "**Warning:** There is a possibility to be reduced in credits for at least **10,000 credits** 💸💸💸!!!");
            embedBuilder.setThumbnail("https://images-ext-1.discordapp.net/external/PgQ-J10dFBhL_GISqeojJ5bfixAmZRd8c1CrwYjNTSI/https/cdn.discordapp.com/emojis/861390900219478037.gif");
            ctx.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRows(
                    ActionRow.of(Button.of(ButtonStyle.PRIMARY, "0000:claim", "Claim"), Button.of(ButtonStyle.DANGER, "0000:NADAME", "Dropped by " + ctx.getAuthor().getName()).asDisabled())
            ).queue();
        }
    }

    @Override
    public String getName() {
        return "drop";
    }

    @Override
    public String getHelp(String prefix) {
        return "Drops a credit chest to users!";
    }

    @Override
    public CommandType getCategory() {
        return CommandType.SPECIAL;
    }
}
