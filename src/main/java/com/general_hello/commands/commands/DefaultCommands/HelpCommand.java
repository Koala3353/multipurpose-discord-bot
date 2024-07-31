package com.general_hello.commands.commands.DefaultCommands;

import com.general_hello.commands.CommandManager;
import com.general_hello.commands.Config;
import com.general_hello.commands.commands.CommandContext;
import com.general_hello.commands.commands.ICommand;
import com.general_hello.commands.commands.Info.InfoUserCommand;
import com.general_hello.commands.commands.PrefixStoring;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;

public class HelpCommand implements ICommand {

    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(CommandContext ctx) {
        List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();
        final long guildID = ctx.getGuild().getIdLong();
        String prefix = PrefixStoring.PREFIXES.computeIfAbsent(guildID, (id) -> Config.get("prefix"));

        if (args.isEmpty()) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Groups");
            embedBuilder.setColor(Color.cyan);
            embedBuilder.addField(com.general_hello.commands.commands.Emoji.Emoji.USER + " | User (1)", "Shows basic to complex commands that the user can do with the bot", false);
            embedBuilder.addField(com.general_hello.commands.commands.Emoji.Emoji.DISCORD_BOT + " | Bot (8)", "Shows the commands you can do with the bot", false);
            embedBuilder.addField(com.general_hello.commands.commands.Emoji.Emoji.INFO + " | Info (3)", "Shows basic to complex information about users, servers, or mods", false);
            embedBuilder.addField(com.general_hello.commands.commands.Emoji.Emoji.MOD + " | Moderation (0)","Basic to advanced moderation tools used by staff to control or monitor the server.", false);

            embedBuilder.setFooter("Type " + prefix + "help [group name] to see their commands");

            boolean disableOrEnable = true;

            if (ctx.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                disableOrEnable = false;
            }
            channel.sendMessageEmbeds(embedBuilder.build()).setActionRow(
                    Button.secondary(ctx.getMember().getUser().getId() + ":user", "User").withEmoji(Emoji.fromEmote("user", Long.parseLong("862895295239028756"), true)),
                    Button.secondary(ctx.getMember().getUser().getId() + ":bot", "Bot").withEmoji(Emoji.fromEmote("discord_bot", Long.parseLong("862895574960701440"), false)),
                    Button.secondary(ctx.getMember().getUser().getId() + ":info", "Info").withEmoji(Emoji.fromEmote("info", Long.parseLong("870871190217060393"), true)),
                    Button.secondary(ctx.getMember().getUser().getId() + ":mod", "Moderation").withDisabled(disableOrEnable).withEmoji(Emoji.fromEmote("mod", Long.parseLong("862898484041482270"), true)),
                    Button.danger(ctx.getMember().getUser().getId() + ":end", "Cancel").withDisabled(disableOrEnable).withEmoji(Emoji.fromEmote("cancel", Long.parseLong("863204248657461298"), true))
            ).queue();

            ctx.getMessage().delete().queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();

        String search = args.get(0);
        ICommand command = manager.getCommand(search);

        if (command == null) {
            channel.sendMessage("Nothing found for " + search).queue();
            return;
        }

        embedBuilder.setTitle("Help!!!");
        embedBuilder.setColor(InfoUserCommand.randomColor());
        embedBuilder.setDescription(command.getHelp(prefix));
        embedBuilder.setTimestamp(OffsetDateTime.now());
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getHelp(String prefix) {
        return "Shows the list with commands in the bot\n" +
                "Usage: `" + prefix + "help [command]`";
    }

    @Override
    public List<String> getAliases() {
        List<String> strings = new java.util.ArrayList<>();
        strings.add("cmds");
        strings.add("commands");
        strings.add("commandlist");
        return strings;
    }
}
