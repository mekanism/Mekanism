package mekanism.common.advancements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import mekanism.common.Mekanism;
import mekanism.common.advancements.triggers.ChangeRobitSkinTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.resources.ResourceLocation;

public class MekanismCriteriaTriggers {

    private MekanismCriteriaTriggers() {
    }

    private static final List<CriterionTrigger<?>> lazyToRegister = new ArrayList<>();

    //TODO - 1.19: Require parent advancements to be unlocked??
    //TODO - 1.19: Test all these custom triggers
    public static final PlayerTrigger LOGGED_IN = lazyRegister("logged_in", PlayerTrigger::new);
    public static final PlayerTrigger TELEPORT = lazyRegister("teleport", PlayerTrigger::new);
    public static final ChangeRobitSkinTrigger CHANGE_ROBIT_SKIN = lazyRegister("change_robit_skin", ChangeRobitSkinTrigger::new);

    private static <TRIGGER extends CriterionTrigger<?>> TRIGGER lazyRegister(String name, Function<ResourceLocation, TRIGGER> constructor) {
        return lazyRegister(constructor.apply(Mekanism.rl(name)));
    }

    private static <TRIGGER extends CriterionTrigger<?>> TRIGGER lazyRegister(TRIGGER criterion) {
        lazyToRegister.add(criterion);
        return criterion;
    }

    public static void init() {
        for (CriterionTrigger<?> trigger : lazyToRegister) {
            CriteriaTriggers.register(trigger);
        }
        lazyToRegister.clear();
    }
}