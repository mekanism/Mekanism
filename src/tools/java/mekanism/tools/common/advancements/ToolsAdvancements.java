package mekanism.tools.common.advancements;

import mekanism.common.advancements.MekanismAdvancement;
import mekanism.common.advancements.MekanismAdvancements;
import mekanism.tools.common.MekanismTools;
import org.jetbrains.annotations.Nullable;

public class ToolsAdvancements {

    private ToolsAdvancements() {
    }

    private static MekanismAdvancement advancement(@Nullable MekanismAdvancement parent, String name) {
        return new MekanismAdvancement(parent, MekanismTools.rl(name));
    }

    public static final MekanismAdvancement PAXEL = advancement(MekanismAdvancements.ROOT, "paxel");
    public static final MekanismAdvancement ALTERNATE_ARMOR = advancement(MekanismAdvancements.MATERIALS, "alternate_armor");
    public static final MekanismAdvancement ALTERNATE_TOOLS = advancement(MekanismAdvancements.MATERIALS, "alternate_tools");
    public static final MekanismAdvancement NOT_ENOUGH_SHIELDING = advancement(MekanismAdvancements.MATERIALS, "not_enough_shielding");
    public static final MekanismAdvancement BETTER_THAN_NETHERITE = advancement(ALTERNATE_ARMOR, "better_than_netherite");
    public static final MekanismAdvancement LOVED_BY_PIGLINS = advancement(ALTERNATE_ARMOR, "loved_by_piglins");
}