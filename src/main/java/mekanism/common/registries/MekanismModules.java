package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.content.gear.mekasuit.ModuleChargeDistributionUnit;
import mekanism.common.content.gear.mekasuit.ModuleDosimeterUnit;
import mekanism.common.content.gear.mekasuit.ModuleElectrolyticBreathingUnit;
import mekanism.common.content.gear.mekasuit.ModuleFrostWalkerUnit;
import mekanism.common.content.gear.mekasuit.ModuleGeigerUnit;
import mekanism.common.content.gear.mekasuit.ModuleGravitationalModulatingUnit;
import mekanism.common.content.gear.mekasuit.ModuleHydraulicPropulsionUnit;
import mekanism.common.content.gear.mekasuit.ModuleInhalationPurificationUnit;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit;
import mekanism.common.content.gear.mekasuit.ModuleMagneticAttractionUnit;
import mekanism.common.content.gear.mekasuit.ModuleNutritionalInjectionUnit;
import mekanism.common.content.gear.mekasuit.ModuleVisionEnhancementUnit;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit;
import mekanism.common.content.gear.mekatool.ModuleFarmingUnit;
import mekanism.common.content.gear.mekatool.ModuleShearingUnit;
import mekanism.common.content.gear.mekatool.ModuleSilkTouchUnit;
import mekanism.common.content.gear.mekatool.ModuleTeleportationUnit;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import net.minecraft.world.item.Rarity;

//Note: We need to declare our item providers like we do so that they don't end up being null due to us referencing these objects from the items
@SuppressWarnings({"Convert2MethodRef", "FunctionalExpressionCanBeFolded"})
public class MekanismModules {

    private MekanismModules() {
    }

    public static final ModuleDeferredRegister MODULES = new ModuleDeferredRegister(Mekanism.MODID);

    //Shared
    public static final ModuleRegistryObject<ModuleEnergyUnit> ENERGY_UNIT = MODULES.register("energy_unit", ModuleEnergyUnit::new,
          () -> MekanismItems.MODULE_ENERGY.asItem(), builder -> builder.maxStackSize(8).rarity(Rarity.UNCOMMON).noDisable());
    //Shared Armor
    public static final ModuleRegistryObject<?> LASER_DISSIPATION_UNIT = MODULES.registerMarker("laser_dissipation_unit",
          () -> MekanismItems.MODULE_LASER_DISSIPATION.asItem(), builder -> builder.rarity(Rarity.UNCOMMON));
    public static final ModuleRegistryObject<?> RADIATION_SHIELDING_UNIT = MODULES.registerMarker("radiation_shielding_unit",
          () -> MekanismItems.MODULE_RADIATION_SHIELDING.asItem(), builder -> builder.rarity(Rarity.UNCOMMON));

    //Meka-Tool
    public static final ModuleRegistryObject<ModuleExcavationEscalationUnit> EXCAVATION_ESCALATION_UNIT = MODULES.register("excavation_escalation_unit",
          ModuleExcavationEscalationUnit::new, () -> MekanismItems.MODULE_EXCAVATION_ESCALATION.asItem(), builder -> builder.maxStackSize(4).rarity(Rarity.UNCOMMON)
                .handlesModeChange().rendersHUD());
    public static final ModuleRegistryObject<ModuleAttackAmplificationUnit> ATTACK_AMPLIFICATION_UNIT = MODULES.register("attack_amplification_unit",
          ModuleAttackAmplificationUnit::new, () -> MekanismItems.MODULE_ATTACK_AMPLIFICATION.asItem(), builder -> builder.maxStackSize(4).rarity(Rarity.UNCOMMON)
                .rendersHUD());
    public static final ModuleRegistryObject<ModuleFarmingUnit> FARMING_UNIT = MODULES.register("farming_unit", ModuleFarmingUnit::new,
          () -> MekanismItems.MODULE_FARMING.asItem(), builder -> builder.maxStackSize(4).rarity(Rarity.UNCOMMON).exclusive());
    //TODO: Eventually we may want to come up with a better exclusive method given realistically the shearing unit and farming unit don't need to be
    // exclusive of each other, but they both should be exclusive in regards to the teleportation unit
    public static final ModuleRegistryObject<ModuleShearingUnit> SHEARING_UNIT = MODULES.register("shearing_unit", ModuleShearingUnit::new,
          () -> MekanismItems.MODULE_SHEARING.asItem(), builder -> builder.rarity(Rarity.UNCOMMON).exclusive());
    public static final ModuleRegistryObject<ModuleSilkTouchUnit> SILK_TOUCH_UNIT = MODULES.register("silk_touch_unit", ModuleSilkTouchUnit::new,
          () -> MekanismItems.MODULE_SILK_TOUCH.asItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<ModuleVeinMiningUnit> VEIN_MINING_UNIT = MODULES.register("vein_mining_unit", ModuleVeinMiningUnit::new,
          () -> MekanismItems.MODULE_VEIN_MINING.asItem(), builder -> builder.maxStackSize(4).rarity(Rarity.RARE).rendersHUD());
    public static final ModuleRegistryObject<ModuleTeleportationUnit> TELEPORTATION_UNIT = MODULES.register("teleportation_unit", ModuleTeleportationUnit::new,
          () -> MekanismItems.MODULE_TELEPORTATION.asItem(), builder -> builder.rarity(Rarity.EPIC).exclusive());

    //Helmet
    public static final ModuleRegistryObject<ModuleElectrolyticBreathingUnit> ELECTROLYTIC_BREATHING_UNIT = MODULES.register("electrolytic_breathing_unit",
          ModuleElectrolyticBreathingUnit::new, () -> MekanismItems.MODULE_ELECTROLYTIC_BREATHING.asItem(), builder -> builder.maxStackSize(4).rarity(Rarity.UNCOMMON));
    public static final ModuleRegistryObject<ModuleInhalationPurificationUnit> INHALATION_PURIFICATION_UNIT = MODULES.register("inhalation_purification_unit",
          ModuleInhalationPurificationUnit::new, () -> MekanismItems.MODULE_INHALATION_PURIFICATION.asItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<ModuleVisionEnhancementUnit> VISION_ENHANCEMENT_UNIT = MODULES.register("vision_enhancement_unit",
          ModuleVisionEnhancementUnit::new, () -> MekanismItems.MODULE_VISION_ENHANCEMENT.asItem(), builder -> builder.maxStackSize(4).rarity(Rarity.RARE)
                .handlesModeChange().rendersHUD().disabledByDefault());
    public static final ModuleRegistryObject<ModuleNutritionalInjectionUnit> NUTRITIONAL_INJECTION_UNIT = MODULES.register("nutritional_injection_unit",
          ModuleNutritionalInjectionUnit::new, () -> MekanismItems.MODULE_NUTRITIONAL_INJECTION.asItem(), builder -> builder.rarity(Rarity.RARE).rendersHUD());

    //Chestplate
    public static final ModuleRegistryObject<ModuleDosimeterUnit> DOSIMETER_UNIT = MODULES.register("dosimeter_unit",
          ModuleDosimeterUnit::new, () -> MekanismItems.MODULE_DOSIMETER.asItem(), builder -> builder.rarity(Rarity.UNCOMMON).rendersHUD());
    public static final ModuleRegistryObject<ModuleGeigerUnit> GEIGER_UNIT = MODULES.register("geiger_unit",
          ModuleGeigerUnit::new, () -> MekanismItems.MODULE_GEIGER.asItem(), builder -> builder.rarity(Rarity.UNCOMMON).rendersHUD());
    public static final ModuleRegistryObject<ModuleJetpackUnit> JETPACK_UNIT = MODULES.register("jetpack_unit",
          ModuleJetpackUnit::new, () -> MekanismItems.MODULE_JETPACK.asItem(), builder -> builder.rarity(Rarity.RARE).handlesModeChange().rendersHUD().exclusive());
    public static final ModuleRegistryObject<ModuleChargeDistributionUnit> CHARGE_DISTRIBUTION_UNIT = MODULES.register("charge_distribution_unit",
          ModuleChargeDistributionUnit::new, () -> MekanismItems.MODULE_CHARGE_DISTRIBUTION.asItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<ModuleGravitationalModulatingUnit> GRAVITATIONAL_MODULATING_UNIT = MODULES.register("gravitational_modulating_unit",
          ModuleGravitationalModulatingUnit::new, () -> MekanismItems.MODULE_GRAVITATIONAL_MODULATING.asItem(), builder -> builder.rarity(Rarity.EPIC).handlesModeChange()
                .rendersHUD().exclusive());
    public static final ModuleRegistryObject<?> ELYTRA_UNIT = MODULES.registerMarker("elytra_unit", () -> MekanismItems.MODULE_ELYTRA.asItem(),
          builder -> builder.rarity(Rarity.EPIC));
    public static final ModuleRegistryObject<?> GYROSCOPIC_STABILIZATION_UNIT = MODULES.registerMarker("gyroscopic_stabilization_unit", () -> MekanismItems.MODULE_GYROSCOPIC_STABILIZATION.asItem(),
          builder -> builder.rarity(Rarity.RARE));

    //Pants
    public static final ModuleRegistryObject<ModuleLocomotiveBoostingUnit> LOCOMOTIVE_BOOSTING_UNIT = MODULES.register("locomotive_boosting_unit",
          ModuleLocomotiveBoostingUnit::new, () -> MekanismItems.MODULE_LOCOMOTIVE_BOOSTING.asItem(), builder -> builder.maxStackSize(4).rarity(Rarity.RARE)
                .handlesModeChange());

    //Boots
    public static final ModuleRegistryObject<ModuleHydraulicPropulsionUnit> HYDRAULIC_PROPULSION_UNIT = MODULES.register("hydraulic_propulsion_unit",
          ModuleHydraulicPropulsionUnit::new, () -> MekanismItems.MODULE_HYDRAULIC_PROPULSION.asItem(), builder -> builder.maxStackSize(4).rarity(Rarity.RARE));
    public static final ModuleRegistryObject<ModuleMagneticAttractionUnit> MAGNETIC_ATTRACTION_UNIT = MODULES.register("magnetic_attraction_unit",
          ModuleMagneticAttractionUnit::new, () -> MekanismItems.MODULE_MAGNETIC_ATTRACTION.asItem(), builder -> builder.maxStackSize(4).rarity(Rarity.RARE)
                .handlesModeChange());
    public static final ModuleRegistryObject<ModuleFrostWalkerUnit> FROST_WALKER_UNIT = MODULES.register("frost_walker_unit", ModuleFrostWalkerUnit::new,
          () -> MekanismItems.MODULE_FROST_WALKER.asItem(), builder -> builder.maxStackSize(2).rarity(Rarity.RARE));
}