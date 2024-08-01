package com.general_hello.commands.OtherEvents;

import com.general_hello.commands.Bot;
import com.general_hello.commands.commands.Currency.DailyDrop;
import com.general_hello.commands.commands.Others.UpdateIgniteCoinsCommand;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class OnReadyEvent extends ListenerAdapter {
    public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Bot.jda = event.getJDA();

        try {
            UpdateIgniteCoinsCommand.loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DailyDrop.runDrop();
    }
}
