package com.general_hello.commands.RPG.Items.Common.Artifacts;

import com.general_hello.commands.RPG.Objects.Artifacts;
import com.general_hello.commands.RPG.Types.AttainableBy;
import com.general_hello.commands.RPG.Types.Rarity;

public class ClayPot extends Artifacts {
    public ClayPot() {
        super(Rarity.COMMON, false, "Clay Pot", AttainableBy.CRAFTING, "");
    }
}