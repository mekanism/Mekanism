package mekanism.additions.common.advancements;

import mekanism.additions.common.MekanismAdditions;
import mekanism.common.advancements.MekanismAdvancement;
import mekanism.common.advancements.MekanismAdvancements;
import org.jetbrains.annotations.Nullable;

public class AdditionsAdvancements {

    private AdditionsAdvancements() {
    }

    private static MekanismAdvancement advancement(@Nullable MekanismAdvancement parent, String name) {
        return new MekanismAdvancement(parent, MekanismAdditions.rl(name));
    }

    public static final MekanismAdvancement BALLOON = advancement(MekanismAdvancements.ROOT, "balloon");
    public static final MekanismAdvancement POP_POP = advancement(BALLOON, "pop_pop");
    public static final MekanismAdvancement GLOW_IN_THE_DARK = advancement(MekanismAdvancements.ROOT, "glow_in_the_dark");
    public static final MekanismAdvancement HURT_BY_BABIES = advancement(MekanismAdvancements.ROOT, "hurt_by_babies");
    public static final MekanismAdvancement NOT_THE_BABIES = advancement(MekanismAdvancements.ROOT, "not_the_babies");
}