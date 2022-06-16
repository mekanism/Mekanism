package mekanism.generators.common.advancements;

import javax.annotation.Nullable;
import mekanism.common.advancements.MekanismAdvancement;
import mekanism.generators.common.MekanismGenerators;

public class GeneratorsAdvancements {

    private GeneratorsAdvancements() {
    }

    private static MekanismAdvancement advancement(@Nullable MekanismAdvancement parent, String name) {
        return new MekanismAdvancement(parent, MekanismGenerators.rl(name));
    }

    //TODO - 1.19: Various types of generators?
    //public static final MekanismAdvancement BALLOON = advancement("balloon", null);
}