package com.general_hello.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

public class Status {
    public Thread status() {
        Runnable runnable = () -> {
            while (true) {
                System.out.println("Yeet");
                Bot.jda.getPresence().setActivity(Activity.listening("Made by General Hello#4756"));

                try {
                    Thread.sleep(1000000);
                } catch (Exception ignored) {
                }

                JDA jda = Bot.jda;
                jda.getPresence().setActivity(Activity.competing("This bot uses over 3,000 lines of code"));

                try {
                    Thread.sleep(1000000);
                } catch (Exception ignored) {
                }

                jda.getPresence().setActivity(Activity.watching("ignt help"));

                try {
                    Thread.sleep(1000000);
                } catch (Exception ignored) {
                }

                jda.getPresence().setActivity(Activity.listening("Made by HELLO66#0066"));

                try {
                    Thread.sleep(1000000);
                } catch (Exception ignored) {
                }

                jda.getPresence().setActivity(Activity.listening("Emojis made by TwoTonTan#9336"));

                try {
                    Thread.sleep(1000000);
                } catch (Exception ignored) {
                }

                jda.getPresence().setActivity(Activity.listening("Design made by SkyacinthClues#9536"));

                try {
                    Thread.sleep(1000000);
                } catch (Exception ignored) {
                }

                jda.getPresence().setActivity(Activity.listening("Emojis made by kitkatsnickers#3901"));

                try {
                    Thread.sleep(1000000);
                } catch (Exception ignored) {
                }

                jda.getPresence().setActivity(Activity.listening("The Passion of Christ"));

                try {
                    Thread.sleep(1000000);
                } catch (Exception ignored) {
                }

                jda.getPresence().setActivity(Activity.streaming("Merry Christmas! 🎄🎄🎄", "discord.com"));

                try {
                    Thread.sleep(1000000);
                } catch (Exception ignored) {
                }
            }
        };

        return new Thread(runnable);
    }
}
