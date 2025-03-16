package mekanism.common.advancements;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.advancements.triggers.AlloyUpgradeTrigger;
import mekanism.common.advancements.triggers.BlockLaserTrigger;
import mekanism.common.advancements.triggers.ChangeRobitSkinTrigger;
import mekanism.common.advancements.triggers.ConfigurationCardTrigger;
import mekanism.common.advancements.triggers.MekanismDamageTrigger;
import mekanism.common.advancements.triggers.SPSExperimentTrigger;
import mekanism.common.advancements.triggers.UnboxCardboardBoxTrigger;
import mekanism.common.advancements.triggers.UseGaugeDropperTrigger;
import mekanism.common.advancements.triggers.UseTierInstallerTrigger;
import mekanism.common.advancements.triggers.ViewVibrationsTrigger;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.registration.impl.DeferredCriterionTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.registries.Registries;

public class MekanismCriteriaTriggers {

    private MekanismCriteriaTriggers() {
    }

    public static final MekanismDeferredRegister<CriterionTrigger<?>> CRITERIA_TRIGGERS = new MekanismDeferredRegister<>(Registries.TRIGGER_TYPE, Mekanism.MODID, DeferredCriterionTrigger::new);

    private static <INSTANCE extends CriterionTriggerInstance, TRIGGER extends CriterionTrigger<INSTANCE>> DeferredCriterionTrigger<INSTANCE, TRIGGER> register(String name, Supplier<TRIGGER> sup) {
        return (DeferredCriterionTrigger<INSTANCE, TRIGGER>) CRITERIA_TRIGGERS.register(name, sup);
    }

    //TODO: Eventually we may want to require parent advancements to be unlocked as one of the "and" conditions. So that then once the parent unlocks
    // if they already completed all the other requirements it auto unlocks as well, but then they can't skip things
    public static final DeferredCriterionTrigger<AlloyUpgradeTrigger.TriggerInstance, AlloyUpgradeTrigger> ALLOY_UPGRADE = register("alloy_upgrade", AlloyUpgradeTrigger::new);
    public static final DeferredCriterionTrigger<BlockLaserTrigger.TriggerInstance, BlockLaserTrigger> BLOCK_LASER = register("block_laser", BlockLaserTrigger::new);
    public static final DeferredCriterionTrigger<ConfigurationCardTrigger.TriggerInstance, ConfigurationCardTrigger> CONFIGURATION_CARD = register("configuration_card", ConfigurationCardTrigger::new);
    public static final DeferredCriterionTrigger<ChangeRobitSkinTrigger.TriggerInstance, ChangeRobitSkinTrigger> CHANGE_ROBIT_SKIN = register("change_robit_skin", ChangeRobitSkinTrigger::new);
    public static final DeferredCriterionTrigger<MekanismDamageTrigger.TriggerInstance, MekanismDamageTrigger> DAMAGE = register("damage", MekanismDamageTrigger::new);
    public static final DeferredCriterionTrigger<PlayerTrigger.TriggerInstance, PlayerTrigger> LOGGED_IN = register("logged_in", PlayerTrigger::new);
    public static final DeferredCriterionTrigger<SPSExperimentTrigger.TriggerInstance, SPSExperimentTrigger> SPS_EXPERIMENT = register("sps_experiment", SPSExperimentTrigger::new);
    public static final DeferredCriterionTrigger<PlayerTrigger.TriggerInstance, PlayerTrigger> TELEPORT = register("teleport", PlayerTrigger::new);
    public static final DeferredCriterionTrigger<UnboxCardboardBoxTrigger.TriggerInstance, UnboxCardboardBoxTrigger> UNBOX_CARDBOARD_BOX = register("unbox_cardboard_box", UnboxCardboardBoxTrigger::new);
    public static final DeferredCriterionTrigger<UseGaugeDropperTrigger.TriggerInstance, UseGaugeDropperTrigger> USE_GAUGE_DROPPER = register("use_gauge_dropper", UseGaugeDropperTrigger::new);
    public static final DeferredCriterionTrigger<UseTierInstallerTrigger.TriggerInstance, UseTierInstallerTrigger> USE_TIER_INSTALLER = register("use_tier_installer", UseTierInstallerTrigger::new);
    public static final DeferredCriterionTrigger<ViewVibrationsTrigger.TriggerInstance, ViewVibrationsTrigger> VIEW_VIBRATIONS = register("view_vibrations", ViewVibrationsTrigger::new);
}