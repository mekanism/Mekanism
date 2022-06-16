package mekanism.additions.common.advancements;

import javax.annotation.Nullable;
import mekanism.additions.common.MekanismAdditions;
import mekanism.common.advancements.MekanismAdvancement;
import mekanism.common.advancements.MekanismAdvancements;

public class AdditionsAdvancements {

    private AdditionsAdvancements() {
    }

    private static MekanismAdvancement advancement(@Nullable MekanismAdvancement parent, String name) {
        return new MekanismAdvancement(parent, MekanismAdditions.rl(name));
    }

    public static final MekanismAdvancement BALLOON = advancement(MekanismAdvancements.ROOT, "balloon");
    public static final MekanismAdvancement GLOW_PANEL = advancement(MekanismAdvancements.ROOT, "glow_panel");
}