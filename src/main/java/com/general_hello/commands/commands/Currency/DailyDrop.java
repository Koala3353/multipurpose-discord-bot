package com.general_hello.commands.commands.Currency;

import com.general_hello.commands.Bot;
import com.general_hello.commands.commands.Info.InfoUserCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.time.OffsetDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DailyDrop {
    public static void runDrop() {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(DailyDrop::drop, 0, 1, TimeUnit.DAYS);
    }

    private static void drop() {
        Bot.jda.getTextChannelById(876363970108334162L).sendMessage("<@&905691492205621278>").queue();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("New Chest Drop!!!").setTimestamp(OffsetDateTime.now()).setColor(InfoUserCommand.randomColor());
        embedBuilder.setDescription("A new chest 🧰 has been found! **First igniter** to press the button below will get  <:credit:905976767821525042> **10,000** to  <:credit:905976767821525042> **100,000 **!\n" +
                "\n**Warning:** There is NO possibility to be reduced in credits due to this chest being a daily dropper 💸💸💸!!!");
        embedBuilder.setThumbnail("https://images-ext-1.discordapp.net/external/e4iDunw5XV3-Hspl7LA8XBLbTLZMQP7rVPJqMkGuMco/https/cdn.discordapp.com/emojis/861390922410360883.gif");
        Bot.jda.getTextChannelById(876363970108334162L).sendMessageEmbeds(embedBuilder.build()).setActionRows(
                ActionRow.of(Button.of(ButtonStyle.PRIMARY, "0000:claimdaily", "Claim"), Button.of(ButtonStyle.DANGER, "0000:NADAME", "Dropped by the daily dropper").asDisabled())
        ).queue((message -> {
            DropCommand.isClaimed.put(message.getIdLong(), false);
            DropCommand.button.put(message.getIdLong(), Button.of(ButtonStyle.DANGER, "0000:NADAME", "Dropped by the daily drop").asDisabled());
        }));
    }
}
