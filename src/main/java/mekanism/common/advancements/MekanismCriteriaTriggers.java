package mekanism.common.advancements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import mekanism.common.Mekanism;
import mekanism.common.advancements.triggers.AlloyUpgradeTrigger;
import mekanism.common.advancements.triggers.BlockLaserTrigger;
import mekanism.common.advancements.triggers.ChangeRobitSkinTrigger;
import mekanism.common.advancements.triggers.ConfigurationCardTrigger;
import mekanism.common.advancements.triggers.MekanismDamageTrigger;
import mekanism.common.advancements.triggers.UnboxCardboardBoxTrigger;
import mekanism.common.advancements.triggers.UseGaugeDropperTrigger;
import mekanism.common.advancements.triggers.ViewVibrationsTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.resources.ResourceLocation;

public class MekanismCriteriaTriggers {

    private MekanismCriteriaTriggers() {
    }

    private static final List<CriterionTrigger<?>> lazyToRegister = new ArrayList<>();

    //TODO: Eventually we may want to require parent advancements to be unlocked as one of the "and" conditions. So that then once the parent unlocks
    // if they already completed all the other requirements it auto unlocks as well, but then they can't skip things
    public static final PlayerTrigger LOGGED_IN = lazyRegister("logged_in", PlayerTrigger::new);
    public static final PlayerTrigger TELEPORT = lazyRegister("teleport", PlayerTrigger::new);
    public static final AlloyUpgradeTrigger ALLOY_UPGRADE = lazyRegister("alloy_upgrade", AlloyUpgradeTrigger::new);
    public static final BlockLaserTrigger BLOCK_LASER = lazyRegister("block_laser", BlockLaserTrigger::new);
    public static final ConfigurationCardTrigger CONFIGURATION_CARD = lazyRegister("configuration_card", ConfigurationCardTrigger::new);
    public static final ChangeRobitSkinTrigger CHANGE_ROBIT_SKIN = lazyRegister("change_robit_skin", ChangeRobitSkinTrigger::new);
    public static final MekanismDamageTrigger DAMAGE = lazyRegister("damage", MekanismDamageTrigger::new);
    public static final UseGaugeDropperTrigger USE_GAUGE_DROPPER = lazyRegister("use_gauge_dropper", UseGaugeDropperTrigger::new);
    public static final UnboxCardboardBoxTrigger UNBOX_CARDBOARD_BOX = lazyRegister("unbox_cardboard_box", UnboxCardboardBoxTrigger::new);
    public static final ViewVibrationsTrigger VIEW_VIBRATIONS = lazyRegister("view_vibrations", ViewVibrationsTrigger::new);

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