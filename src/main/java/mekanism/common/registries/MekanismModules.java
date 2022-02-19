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
import mekanism.common.content.gear.mekasuit.ModuleSolarRechargingUnit;
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
import net.minecraft.item.Rarity;

//Note: We need to declare our item providers like we do so that they don't end up being null due to us referencing these objects from the items
@SuppressWarnings({"Convert2MethodRef", "FunctionalExpressionCanBeFolded"})
public class MekanismModules {

    private MekanismModules() {
    }

    public static final ModuleDeferredRegister MODULES = new ModuleDeferredRegister(Mekanism.MODID);

    //Shared
    public static final ModuleRegistryObject<ModuleEnergyUnit> ENERGY_UNIT = MODULES.registerLegacy("energy_unit", ModuleEnergyUnit::new,
          () -> MekanismItems.MODULE_ENERGY.getItem(), builder -> builder.maxStackSize(8).rarity(Rarity.UNCOMMON).noDisable());
    //Shared Armor
    public static final ModuleRegistryObject<?> LASER_DISSIPATION_UNIT = MODULES.registerMarker("laser_dissipation_unit",
          () -> MekanismItems.MODULE_LASER_DISSIPATION.getItem(), builder -> builder.rarity(Rarity.UNCOMMON));
    public static final ModuleRegistryObject<?> RADIATION_SHIELDING_UNIT = MODULES.registerMarkerLegacy("radiation_shielding_unit",
          () -> MekanismItems.MODULE_RADIATION_SHIELDING.getItem(), builder -> builder.rarity(Rarity.UNCOMMON));

    //Meka-Tool
    public static final ModuleRegistryObject<ModuleExcavationEscalationUnit> EXCAVATION_ESCALATION_UNIT = MODULES.registerLegacy("excavation_escalation_unit",
          ModuleExcavationEscalationUnit::new, () -> MekanismItems.MODULE_EXCAVATION_ESCALATION.getItem(), builder -> builder.maxStackSize(4).rarity(Rarity.UNCOMMON)
                .handlesModeChange().rendersHUD());
    public static final ModuleRegistryObject<ModuleAttackAmplificationUnit> ATTACK_AMPLIFICATION_UNIT = MODULES.registerLegacy("attack_amplification_unit",
          ModuleAttackAmplificationUnit::new, () -> MekanismItems.MODULE_ATTACK_AMPLIFICATION.getItem(), builder -> builder.maxStackSize(4).rarity(Rarity.UNCOMMON)
                .rendersHUD());
    public static final ModuleRegistryObject<ModuleFarmingUnit> FARMING_UNIT = MODULES.registerLegacy("farming_unit", ModuleFarmingUnit::new,
          () -> MekanismItems.MODULE_FARMING.getItem(), builder -> builder.maxStackSize(4).rarity(Rarity.UNCOMMON).exclusive());
    //TODO: Eventually we may want to come up with a better exclusive method given realistically the shearing unit and farming unit don't need to be
    // exclusive of each other, but they both should be exclusive in regards to the teleportation unit
    public static final ModuleRegistryObject<ModuleShearingUnit> SHEARING_UNIT = MODULES.register("shearing_unit", ModuleShearingUnit::new,
          () -> MekanismItems.MODULE_SHEARING.getItem(), builder -> builder.rarity(Rarity.UNCOMMON).exclusive());
    public static final ModuleRegistryObject<ModuleSilkTouchUnit> SILK_TOUCH_UNIT = MODULES.registerLegacy("silk_touch_unit", ModuleSilkTouchUnit::new,
          () -> MekanismItems.MODULE_SILK_TOUCH.getItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<ModuleVeinMiningUnit> VEIN_MINING_UNIT = MODULES.registerLegacy("vein_mining_unit", ModuleVeinMiningUnit::new,
          () -> MekanismItems.MODULE_VEIN_MINING.getItem(), builder -> builder.maxStackSize(4).rarity(Rarity.RARE).rendersHUD());
    public static final ModuleRegistryObject<ModuleTeleportationUnit> TELEPORTATION_UNIT = MODULES.registerLegacy("teleportation_unit", ModuleTeleportationUnit::new,
          () -> MekanismItems.MODULE_TELEPORTATION.getItem(), builder -> builder.rarity(Rarity.EPIC).exclusive());

    //Helmet
    public static final ModuleRegistryObject<ModuleElectrolyticBreathingUnit> ELECTROLYTIC_BREATHING_UNIT = MODULES.registerLegacy("electrolytic_breathing_unit",
          ModuleElectrolyticBreathingUnit::new, () -> MekanismItems.MODULE_ELECTROLYTIC_BREATHING.getItem(), builder -> builder.maxStackSize(4).rarity(Rarity.UNCOMMON));
    public static final ModuleRegistryObject<ModuleInhalationPurificationUnit> INHALATION_PURIFICATION_UNIT = MODULES.registerLegacy("inhalation_purification_unit",
          ModuleInhalationPurificationUnit::new, () -> MekanismItems.MODULE_INHALATION_PURIFICATION.getItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<ModuleVisionEnhancementUnit> VISION_ENHANCEMENT_UNIT = MODULES.registerLegacy("vision_enhancement_unit",
          ModuleVisionEnhancementUnit::new, () -> MekanismItems.MODULE_VISION_ENHANCEMENT.getItem(), builder -> builder.maxStackSize(4).rarity(Rarity.RARE)
                .handlesModeChange().rendersHUD().disabledByDefault());
    //TODO - 1.18: Decide if we want to move this to Mekanism Generators so we can remove the requires mekanism generators from the description?
    // It may be a bit of a pain due to how we do the custom models, but also maybe we don't want to in case pack devs want to put in a custom recipe
    public static final ModuleRegistryObject<ModuleSolarRechargingUnit> SOLAR_RECHARGING_UNIT = MODULES.registerLegacy("solar_recharging_unit",
          ModuleSolarRechargingUnit::new, () -> MekanismItems.MODULE_SOLAR_RECHARGING.getItem(), builder -> builder.maxStackSize(8).rarity(Rarity.RARE));
    public static final ModuleRegistryObject<ModuleNutritionalInjectionUnit> NUTRITIONAL_INJECTION_UNIT = MODULES.registerLegacy("nutritional_injection_unit",
          ModuleNutritionalInjectionUnit::new, () -> MekanismItems.MODULE_NUTRITIONAL_INJECTION.getItem(), builder -> builder.rarity(Rarity.RARE).rendersHUD());

    //Chestplate
    public static final ModuleRegistryObject<ModuleDosimeterUnit> DOSIMETER_UNIT = MODULES.registerLegacy("dosimeter_unit",
          ModuleDosimeterUnit::new, () -> MekanismItems.MODULE_DOSIMETER.getItem(), builder -> builder.rarity(Rarity.UNCOMMON).rendersHUD());
    public static final ModuleRegistryObject<ModuleGeigerUnit> GEIGER_UNIT = MODULES.register("geiger_unit",
          ModuleGeigerUnit::new, () -> MekanismItems.MODULE_GEIGER.getItem(), builder -> builder.rarity(Rarity.UNCOMMON).rendersHUD());
    public static final ModuleRegistryObject<ModuleJetpackUnit> JETPACK_UNIT = MODULES.registerLegacy("jetpack_unit",
          ModuleJetpackUnit::new, () -> MekanismItems.MODULE_JETPACK.getItem(), builder -> builder.rarity(Rarity.RARE).handlesModeChange().rendersHUD().exclusive());
    public static final ModuleRegistryObject<ModuleChargeDistributionUnit> CHARGE_DISTRIBUTION_UNIT = MODULES.registerLegacy("charge_distribution_unit",
          ModuleChargeDistributionUnit::new, () -> MekanismItems.MODULE_CHARGE_DISTRIBUTION.getItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<ModuleGravitationalModulatingUnit> GRAVITATIONAL_MODULATING_UNIT = MODULES.registerLegacy("gravitational_modulating_unit",
          ModuleGravitationalModulatingUnit::new, () -> MekanismItems.MODULE_GRAVITATIONAL_MODULATING.getItem(), builder -> builder.rarity(Rarity.EPIC).handlesModeChange()
                .rendersHUD().exclusive());
    public static final ModuleRegistryObject<?> ELYTRA_UNIT = MODULES.registerMarker("elytra_unit", () -> MekanismItems.MODULE_ELYTRA.getItem(),
          builder -> builder.rarity(Rarity.EPIC));

    //Pants
    public static final ModuleRegistryObject<ModuleLocomotiveBoostingUnit> LOCOMOTIVE_BOOSTING_UNIT = MODULES.registerLegacy("locomotive_boosting_unit",
          ModuleLocomotiveBoostingUnit::new, () -> MekanismItems.MODULE_LOCOMOTIVE_BOOSTING.getItem(), builder -> builder.maxStackSize(4).rarity(Rarity.RARE)
                .handlesModeChange());

    //Boots
    public static final ModuleRegistryObject<ModuleHydraulicPropulsionUnit> HYDRAULIC_PROPULSION_UNIT = MODULES.registerLegacy("hydraulic_propulsion_unit",
          ModuleHydraulicPropulsionUnit::new, () -> MekanismItems.MODULE_HYDRAULIC_PROPULSION.getItem(), builder -> builder.maxStackSize(4).rarity(Rarity.RARE));
    public static final ModuleRegistryObject<ModuleMagneticAttractionUnit> MAGNETIC_ATTRACTION_UNIT = MODULES.registerLegacy("magnetic_attraction_unit",
          ModuleMagneticAttractionUnit::new, () -> MekanismItems.MODULE_MAGNETIC_ATTRACTION.getItem(), builder -> builder.maxStackSize(4).rarity(Rarity.RARE)
                .handlesModeChange());
    public static final ModuleRegistryObject<ModuleFrostWalkerUnit> FROST_WALKER_UNIT = MODULES.register("frost_walker_unit", ModuleFrostWalkerUnit::new,
          () -> MekanismItems.MODULE_FROST_WALKER.getItem(), builder -> builder.maxStackSize(2).rarity(Rarity.RARE));
}