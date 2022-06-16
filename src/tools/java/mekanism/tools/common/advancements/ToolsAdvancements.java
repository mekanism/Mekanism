package mekanism.tools.common.advancements;

import javax.annotation.Nullable;
import mekanism.common.advancements.MekanismAdvancement;
import mekanism.common.advancements.MekanismAdvancements;
import mekanism.tools.common.MekanismTools;

public class ToolsAdvancements {

    private ToolsAdvancements() {
    }

    private static MekanismAdvancement advancement(@Nullable MekanismAdvancement parent, String name) {
        return new MekanismAdvancement(parent, MekanismTools.rl(name));
    }

    public static final MekanismAdvancement PAXEL = advancement(MekanismAdvancements.ROOT, "paxel");
    //TODO - 1.19: Evaluate some of the better armor/tools etc as it may not be as good as some vanilla things?
    // so maybe we want to call it by something else
    public static final MekanismAdvancement BETTER_ARMOR = advancement(MekanismAdvancements.MATERIALS, "better_armor");
    public static final MekanismAdvancement BETTER_TOOLS = advancement(MekanismAdvancements.MATERIALS, "better_tools");
    public static final MekanismAdvancement BETTER_SHIELDS = advancement(MekanismAdvancements.MATERIALS, "better_shields");
    public static final MekanismAdvancement BETTER_THAN_NETHERITE = advancement(BETTER_ARMOR, "better_than_netherite");
}