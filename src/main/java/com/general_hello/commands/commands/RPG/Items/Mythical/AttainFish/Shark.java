package com.general_hello.commands.RPG.Items.Mythical.AttainFish;

import com.general_hello.commands.RPG.Objects.SeaAnimals;
import com.general_hello.commands.RPG.Types.AttainableBy;
import com.general_hello.commands.RPG.Types.Level;
import com.general_hello.commands.RPG.Types.Rarity;

public class Shark extends SeaAnimals {
    public Shark() {
        super(Rarity.SUPERRARE, 15, "Shark", Level.LEVELONE, AttainableBy.FISHING, "");
    }
}
