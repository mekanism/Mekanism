package mekanism.common.advancements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import mekanism.common.Mekanism;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;

public class MekanismCriteriaTriggers {

    private MekanismCriteriaTriggers() {
    }

    private static final List<CriterionTrigger<?>> lazyToRegister = new ArrayList<>();

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