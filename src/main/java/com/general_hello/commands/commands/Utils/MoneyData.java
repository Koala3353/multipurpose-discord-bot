package com.general_hello.commands.commands.Utils;

import net.dv8tion.jda.api.entities.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class MoneyData {
    public static HashMap<User, Double> money = new HashMap<>();
    public static HashMap<User, Double> bank = new HashMap<>();
    public static HashMap<User, Double> goal = new HashMap<>();
    public static HashMap<User, Double> robGoal = new HashMap<>();
    public static HashMap<User, Double> robGoalProgress = new HashMap<>();
    public static HashMap<User, Double> moneyGoalProgress = new HashMap<>();
    public static HashMap<User, LocalDateTime> time = new HashMap<>();
    public static HashMap<User, Integer> Hour = new HashMap<>();
    public static HashMap<User, Integer> timeMonthly = new HashMap<>();
    public static HashMap<User, Integer> timeMinute = new HashMap<>();
    public static HashMap<User, LocalDateTime> timeRob = new HashMap<>();
    public static ArrayList<User> users = new ArrayList<>();
    public static HashMap<User, LocalDateTime> timeBankRob = new HashMap<>();
}
