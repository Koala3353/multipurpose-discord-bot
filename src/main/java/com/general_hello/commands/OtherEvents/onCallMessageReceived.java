package com.general_hello.commands.OtherEvents;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.general_hello.commands.commands.Call.QueueDatabase;
import com.general_hello.commands.commands.Register.Data;
import com.general_hello.commands.commands.User.UserPhoneUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class onCallMessageReceived extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (QueueDatabase.retrieveCallId.containsKey(event.getChannel())) {
            callReplyAndStuff(event);
        }
    }

    public static void callReplyAndStuff(GuildMessageReceivedEvent event) {
        Integer callID = QueueDatabase.retrieveCallId.get(event.getChannel());
        ArrayList<TextChannel> callChannels = QueueDatabase.activeCall.get(callID);
        TextChannel channel1 = event.getChannel();
        TextChannel channel2 = callChannels.get(0);

        if (channel1 == callChannels.get(0)) {
            channel2 = callChannels.get(1);
        }

        String url;

        if (Data.serverToWebhookUrl.containsKey(channel2)) {
            url = Data.serverToWebhookUrl.get(channel2);
        } else {
            url = channel2.createWebhook("Unknown user").complete().getUrl();
            Data.serverToWebhookUrl.put(channel2, url);
        }

        WebhookClientBuilder builder = new WebhookClientBuilder(url);
        builder.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName("Hello");
            thread.setDaemon(true);
            return thread;
        });

        builder.setWait(true);
        WebhookClient client = builder.build();
        WebhookMessageBuilder builder1 = new WebhookMessageBuilder();
        UserPhoneUser userPhoneUser = Data.userUserPhoneUserHashMap.get(event.getAuthor());
        builder1.setUsername(userPhoneUser.getUserPhoneUserName());
        builder1.setAvatarUrl(userPhoneUser.getGetUserPhoneUserProfilePicture());
        builder1.setContent(event.getMessage().getContentRaw());
        client.send(builder1.build());
    }
}
