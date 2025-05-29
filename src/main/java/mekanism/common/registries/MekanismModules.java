package mekanism.common.registries;

import java.util.function.UnaryOperator;
import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.gear.ModuleData.ModuleDataBuilder;
import mekanism.api.gear.config.ModuleBooleanConfig;
import mekanism.api.gear.config.ModuleColorConfig;
import mekanism.api.gear.config.ModuleEnumConfig;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.mekasuit.GyroscopicStabilizationUnit;
import mekanism.common.content.gear.mekasuit.ModuleChargeDistributionUnit;
import mekanism.common.content.gear.mekasuit.ModuleDosimeterUnit;
import mekanism.common.content.gear.mekasuit.ModuleElectrolyticBreathingUnit;
import mekanism.common.content.gear.mekasuit.ModuleElytraUnit;
import mekanism.common.content.gear.mekasuit.ModuleGeigerUnit;
import mekanism.common.content.gear.mekasuit.ModuleGravitationalModulatingUnit;
import mekanism.common.content.gear.mekasuit.ModuleHydraulicPropulsionUnit;
import mekanism.common.content.gear.mekasuit.ModuleHydraulicPropulsionUnit.JumpBoost;
import mekanism.common.content.gear.mekasuit.ModuleHydraulicPropulsionUnit.StepAssist;
import mekanism.common.content.gear.mekasuit.ModuleHydrostaticRepulsorUnit;
import mekanism.common.content.gear.mekasuit.ModuleInhalationPurificationUnit;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit.ThrustMultiplier;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit.SprintBoost;
import mekanism.common.content.gear.mekasuit.ModuleMagneticAttractionUnit;
import mekanism.common.content.gear.mekasuit.ModuleMagneticAttractionUnit.Range;
import mekanism.common.content.gear.mekasuit.ModuleNutritionalInjectionUnit;
import mekanism.common.content.gear.mekasuit.ModuleVisionEnhancementUnit;
import mekanism.common.content.gear.mekasuit.MotorizedServoUnit;
import mekanism.common.content.gear.mekasuit.SoulSurferUnit;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit.AttackDamage;
import mekanism.common.content.gear.mekatool.ModuleBlastingUnit;
import mekanism.common.content.gear.mekatool.ModuleBlastingUnit.BlastRadius;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit.ExcavationMode;
import mekanism.common.content.gear.mekatool.ModuleFarmingUnit;
import mekanism.common.content.gear.mekatool.ModuleFarmingUnit.FarmingRadius;
import mekanism.common.content.gear.mekatool.ModuleShearingUnit;
import mekanism.common.content.gear.mekatool.ModuleTeleportationUnit;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit.ExcavationRange;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit.ModuleExtendedModeConfig;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleDeferredRegister.SimpleEnchantmentAwareModule;
import mekanism.common.registration.impl.ModuleRegistryObject;
import net.minecraft.world.item.enchantment.Enchantments;

public class MekanismModules {

    private MekanismModules() {
    }

    public static final ModuleDeferredRegister MODULES = new ModuleDeferredRegister(Mekanism.MODID);

    //Shared
    public static final ModuleRegistryObject<ModuleEnergyUnit> ENERGY_UNIT = MODULES.registerInstanced("energy_unit", ModuleEnergyUnit::new,
          () -> MekanismItems.MODULE_ENERGY, builder -> builder.maxStackSize(8).noDisable());
    //Shared Armor
    public static final ModuleRegistryObject<ModuleColorModulationUnit> COLOR_MODULATION_UNIT = MODULES.register("color_modulation_unit",
          ModuleColorModulationUnit::new, () -> MekanismItems.MODULE_COLOR_MODULATION, builder -> builder.noDisable()
                .addConfig(ModuleColorConfig.argb(ModuleColorModulationUnit.COLOR), ModuleColorConfig.ARGB_CODEC, ModuleColorConfig.ARGB_STREAM_CODEC)
    );
    public static final ModuleRegistryObject<?> LASER_DISSIPATION_UNIT = MODULES.registerMarker("laser_dissipation_unit",
          () -> MekanismItems.MODULE_LASER_DISSIPATION);
    public static final ModuleRegistryObject<?> RADIATION_SHIELDING_UNIT = MODULES.registerMarker("radiation_shielding_unit",
          () -> MekanismItems.MODULE_RADIATION_SHIELDING);

    //Meka-Tool
    public static final ModuleRegistryObject<ModuleExcavationEscalationUnit> EXCAVATION_ESCALATION_UNIT = MODULES.register("excavation_escalation_unit",
          ModuleExcavationEscalationUnit::new, () -> MekanismItems.MODULE_EXCAVATION_ESCALATION, builder -> builder.maxStackSize(4).handlesModeChange().rendersHUD()
                .addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleExcavationEscalationUnit.EXCAVATION_MODE, ExcavationMode.NORMAL, installed + 2),
                      installed -> ModuleEnumConfig.codec(ExcavationMode.CODEC, ExcavationMode.class, installed + 2),
                      installed -> ModuleEnumConfig.streamCodec(ExcavationMode.STREAM_CODEC, ExcavationMode.class, installed + 2)
                )
    );
    public static final ModuleRegistryObject<ModuleAttackAmplificationUnit> ATTACK_AMPLIFICATION_UNIT = MODULES.register("attack_amplification_unit",
          ModuleAttackAmplificationUnit::new, () -> MekanismItems.MODULE_ATTACK_AMPLIFICATION, builder -> builder.maxStackSize(4).rendersHUD()
                .addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleAttackAmplificationUnit.ATTACK_DAMAGE, AttackDamage.MED, installed + 2),
                      installed -> ModuleEnumConfig.codec(AttackDamage.CODEC, AttackDamage.class, installed + 2),
                      installed -> ModuleEnumConfig.streamCodec(AttackDamage.STREAM_CODEC, AttackDamage.class, installed + 2)
                )
    );
    public static final ModuleRegistryObject<ModuleFarmingUnit> FARMING_UNIT = MODULES.register("farming_unit", ModuleFarmingUnit::new,
          () -> MekanismItems.MODULE_FARMING, builder -> builder.maxStackSize(4).exclusive(ExclusiveFlag.INTERACT_BLOCK)
                .addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleFarmingUnit.FARMING_RADIUS, FarmingRadius.LOW, installed + 1),
                      installed -> ModuleEnumConfig.codec(FarmingRadius.CODEC, FarmingRadius.class, installed + 1),
                      installed -> ModuleEnumConfig.streamCodec(FarmingRadius.STREAM_CODEC, FarmingRadius.class, installed + 1)
                )
    );
    public static final ModuleRegistryObject<ModuleShearingUnit> SHEARING_UNIT = MODULES.registerInstanced("shearing_unit", ModuleShearingUnit::new,
          () -> MekanismItems.MODULE_SHEARING, builder -> builder.exclusive(ExclusiveFlag.INTERACT_ENTITY, ExclusiveFlag.INTERACT_BLOCK));
    public static final ModuleRegistryObject<SimpleEnchantmentAwareModule> SILK_TOUCH_UNIT = MODULES.registerEnchantBased("silk_touch_unit",
          Enchantments.SILK_TOUCH, () -> MekanismItems.MODULE_SILK_TOUCH, builder -> builder.exclusive(ExclusiveFlag.OVERRIDE_DROPS));
    public static final ModuleRegistryObject<SimpleEnchantmentAwareModule> FORTUNE_UNIT = MODULES.registerEnchantBased("fortune_unit", Enchantments.FORTUNE,
          () -> MekanismItems.MODULE_FORTUNE, builder -> builder.maxStackSize(3).exclusive(ExclusiveFlag.OVERRIDE_DROPS));
    public static final ModuleRegistryObject<ModuleBlastingUnit> BLASTING_UNIT = MODULES.register("blasting_unit", ModuleBlastingUnit::new,
          () -> MekanismItems.MODULE_BLASTING, builder -> builder.maxStackSize(4).handlesModeChange().rendersHUD()
                .addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleBlastingUnit.BLAST_RADIUS, BlastRadius.LOW, installed + 1),
                      installed -> ModuleEnumConfig.codec(BlastRadius.CODEC, BlastRadius.class, installed + 1),
                      installed -> ModuleEnumConfig.streamCodec(BlastRadius.STREAM_CODEC, BlastRadius.class, installed + 1)
                )
    );
    public static final ModuleRegistryObject<ModuleVeinMiningUnit> VEIN_MINING_UNIT = MODULES.register("vein_mining_unit", ModuleVeinMiningUnit::new,
          () -> MekanismItems.MODULE_VEIN_MINING, builder -> builder.maxStackSize(4).handlesModeChange().rendersHUD()
                .addConfig(new ModuleExtendedModeConfig(ModuleVeinMiningUnit.EXTENDED_MODE, false), ModuleExtendedModeConfig.CODEC, ModuleExtendedModeConfig.STREAM_CODEC)
                .addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleVeinMiningUnit.EXCAVATION_RANGE, ExcavationRange.LOW, installed + 1),
                      installed -> ModuleEnumConfig.codec(ExcavationRange.CODEC, ExcavationRange.class, installed + 1),
                      installed -> ModuleEnumConfig.streamCodec(ExcavationRange.STREAM_CODEC, ExcavationRange.class, installed + 1)
                )
    );
    public static final ModuleRegistryObject<ModuleTeleportationUnit> TELEPORTATION_UNIT = MODULES.register("teleportation_unit", ModuleTeleportationUnit::new,
          () -> MekanismItems.MODULE_TELEPORTATION, builder -> builder.exclusive(ExclusiveFlag.INTERACT_ANY)
                .addConfig(ModuleBooleanConfig.create(ModuleTeleportationUnit.REQUIRE_TARGET, true))
    );

    //Helmet
    public static final ModuleRegistryObject<ModuleElectrolyticBreathingUnit> ELECTROLYTIC_BREATHING_UNIT = MODULES.register("electrolytic_breathing_unit",
          ModuleElectrolyticBreathingUnit::new, () -> MekanismItems.MODULE_ELECTROLYTIC_BREATHING, builder -> builder.maxStackSize(4)
                .addConfig(ModuleBooleanConfig.create(ModuleElectrolyticBreathingUnit.FILL_HELD, true))
    );
    public static final ModuleRegistryObject<ModuleInhalationPurificationUnit> INHALATION_PURIFICATION_UNIT = MODULES.register("inhalation_purification_unit",
          ModuleInhalationPurificationUnit::new, () -> MekanismItems.MODULE_INHALATION_PURIFICATION, builder -> builder
                .addConfig(ModuleBooleanConfig.create(ModuleInhalationPurificationUnit.BENEFICIAL_EFFECTS, false))
                .addConfig(ModuleBooleanConfig.create(ModuleInhalationPurificationUnit.NEUTRAL_EFFECTS, false))
                .addConfig(ModuleBooleanConfig.create(ModuleInhalationPurificationUnit.HARMFUL_EFFECTS, true))
    );
    public static final ModuleRegistryObject<ModuleVisionEnhancementUnit> VISION_ENHANCEMENT_UNIT = MODULES.registerInstanced("vision_enhancement_unit",
          ModuleVisionEnhancementUnit::new, () -> MekanismItems.MODULE_VISION_ENHANCEMENT, builder -> builder.maxStackSize(4).handlesModeChange().rendersHUD()
                .disabledByDefault());
    public static final ModuleRegistryObject<ModuleNutritionalInjectionUnit> NUTRITIONAL_INJECTION_UNIT = MODULES.registerInstanced("nutritional_injection_unit",
          ModuleNutritionalInjectionUnit::new, () -> MekanismItems.MODULE_NUTRITIONAL_INJECTION, ModuleDataBuilder::rendersHUD);

    //Chestplate
    public static final ModuleRegistryObject<ModuleDosimeterUnit> DOSIMETER_UNIT = MODULES.registerInstanced("dosimeter_unit",
          ModuleDosimeterUnit::new, () -> MekanismItems.MODULE_DOSIMETER, ModuleDataBuilder::rendersHUD);
    public static final ModuleRegistryObject<ModuleGeigerUnit> GEIGER_UNIT = MODULES.registerInstanced("geiger_unit",
          ModuleGeigerUnit::new, () -> MekanismItems.MODULE_GEIGER, ModuleDataBuilder::rendersHUD);
    public static final ModuleRegistryObject<ModuleJetpackUnit> JETPACK_UNIT = MODULES.register("jetpack_unit",
          ModuleJetpackUnit::new, () -> MekanismItems.MODULE_JETPACK, builder -> builder.maxStackSize(4).handlesModeChange().rendersHUD()
                .exclusive(ExclusiveFlag.OVERRIDE_JUMP)
                .addConfig(
                      ModuleEnumConfig.create(ModuleJetpackUnit.JETPACK_MODE, JetpackMode.NORMAL),
                      ModuleEnumConfig.codec(JetpackMode.CODEC),
                      ModuleEnumConfig.streamCodec(JetpackMode.STREAM_CODEC)
                ).addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleJetpackUnit.JETPACK_MULT, ThrustMultiplier.NORMAL, installed + 1),
                      installed -> ModuleEnumConfig.codec(ThrustMultiplier.CODEC, ThrustMultiplier.class, installed + 1),
                      installed -> ModuleEnumConfig.streamCodec(ThrustMultiplier.STREAM_CODEC, ThrustMultiplier.class, installed + 1)
                ).addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleJetpackUnit.JETPACK_HOVER_MULT, ThrustMultiplier.NORMAL, installed + 1),
                      installed -> ModuleEnumConfig.codec(ThrustMultiplier.CODEC, ThrustMultiplier.class, installed + 1),
                      installed -> ModuleEnumConfig.streamCodec(ThrustMultiplier.STREAM_CODEC, ThrustMultiplier.class, installed + 1)
                )
    );
    public static final ModuleRegistryObject<ModuleChargeDistributionUnit> CHARGE_DISTRIBUTION_UNIT = MODULES.register("charge_distribution_unit",
          ModuleChargeDistributionUnit::new, () -> MekanismItems.MODULE_CHARGE_DISTRIBUTION, builder -> builder
                .addConfig(ModuleBooleanConfig.create(ModuleChargeDistributionUnit.CHARGE_SUIT, true))
                .addConfig(ModuleBooleanConfig.create(ModuleChargeDistributionUnit.CHARGE_INVENTORY, false))
    );
    public static final ModuleRegistryObject<ModuleGravitationalModulatingUnit> GRAVITATIONAL_MODULATING_UNIT = MODULES.register("gravitational_modulating_unit",
          ModuleGravitationalModulatingUnit::new, () -> MekanismItems.MODULE_GRAVITATIONAL_MODULATING, builder -> builder.handlesModeChange()
                .rendersHUD().exclusive(ExclusiveFlag.OVERRIDE_JUMP)
                // we share the enum type with the locomotive boosting unit
                .addConfig(
                      ModuleEnumConfig.create(ModuleGravitationalModulatingUnit.SPEED_BOOST, SprintBoost.LOW),
                      ModuleEnumConfig.codec(SprintBoost.CODEC),
                      ModuleEnumConfig.streamCodec(SprintBoost.STREAM_CODEC)
                )
    );
    public static final ModuleRegistryObject<ModuleElytraUnit> ELYTRA_UNIT = MODULES.registerInstanced("elytra_unit", ModuleElytraUnit::new,
          () -> MekanismItems.MODULE_ELYTRA, builder -> builder.handlesModeChange().modeChangeDisabledByDefault());

    //Pants
    public static final ModuleRegistryObject<ModuleLocomotiveBoostingUnit> LOCOMOTIVE_BOOSTING_UNIT = MODULES.register("locomotive_boosting_unit",
          ModuleLocomotiveBoostingUnit::new, () -> MekanismItems.MODULE_LOCOMOTIVE_BOOSTING, builder -> builder.maxStackSize(4).handlesModeChange()
                .addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleLocomotiveBoostingUnit.SPRINT_BOOST, SprintBoost.LOW, installed + 1),
                      installed -> ModuleEnumConfig.codec(SprintBoost.CODEC, SprintBoost.class, installed + 1),
                      installed -> ModuleEnumConfig.streamCodec(SprintBoost.STREAM_CODEC, SprintBoost.class, installed + 1)
                )
    );
    public static final ModuleRegistryObject<GyroscopicStabilizationUnit> GYROSCOPIC_STABILIZATION_UNIT = MODULES.registerInstanced("gyroscopic_stabilization_unit",
          GyroscopicStabilizationUnit::new, () -> MekanismItems.MODULE_GYROSCOPIC_STABILIZATION, UnaryOperator.identity());
    public static final ModuleRegistryObject<ModuleHydrostaticRepulsorUnit> HYDROSTATIC_REPULSOR_UNIT = MODULES.register("hydrostatic_repulsor_unit",
          ModuleHydrostaticRepulsorUnit::new, () -> MekanismItems.MODULE_HYDROSTATIC_REPULSOR, builder -> builder.maxStackSize(4)
                .addInstalledCountConfig(
                      //Conditionally add the config option for when we are max installed
                      installed -> installed >= ModuleHydrostaticRepulsorUnit.BOOST_STACKS,
                      installed -> ModuleBooleanConfig.create(ModuleHydrostaticRepulsorUnit.SWIM_BOOST, true),
                      installed -> ModuleBooleanConfig.CODEC,
                      installed -> ModuleBooleanConfig.STREAM_CODEC
                )
    );
    public static final ModuleRegistryObject<MotorizedServoUnit> MOTORIZED_SERVO_UNIT = MODULES.registerInstanced("motorized_servo_unit",
          MotorizedServoUnit::new, () -> MekanismItems.MODULE_MOTORIZED_SERVO, builder -> builder.maxStackSize(5));

    //Boots
    public static final ModuleRegistryObject<ModuleHydraulicPropulsionUnit> HYDRAULIC_PROPULSION_UNIT = MODULES.register("hydraulic_propulsion_unit",
          ModuleHydraulicPropulsionUnit::new, () -> MekanismItems.MODULE_HYDRAULIC_PROPULSION, builder -> builder.maxStackSize(4)
                .addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleHydraulicPropulsionUnit.JUMP_BOOST, JumpBoost.LOW, installed + 1),
                      installed -> ModuleEnumConfig.codec(JumpBoost.CODEC, JumpBoost.class, installed + 1),
                      installed -> ModuleEnumConfig.streamCodec(JumpBoost.STREAM_CODEC, JumpBoost.class, installed + 1)
                ).addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleHydraulicPropulsionUnit.STEP_ASSIST, StepAssist.LOW, installed + 1),
                      installed -> ModuleEnumConfig.codec(StepAssist.CODEC, StepAssist.class, installed + 1),
                      installed -> ModuleEnumConfig.streamCodec(StepAssist.STREAM_CODEC, StepAssist.class, installed + 1)
                )
    );
    public static final ModuleRegistryObject<ModuleMagneticAttractionUnit> MAGNETIC_ATTRACTION_UNIT = MODULES.register("magnetic_attraction_unit",
          ModuleMagneticAttractionUnit::new, () -> MekanismItems.MODULE_MAGNETIC_ATTRACTION, builder -> builder.maxStackSize(4).handlesModeChange()
                .addInstalledCountConfig(
                      installed -> ModuleEnumConfig.createBounded(ModuleMagneticAttractionUnit.RANGE, Range.LOW, installed + 1),
                      installed -> ModuleEnumConfig.codec(Range.CODEC, Range.class, installed + 1),
                      installed -> ModuleEnumConfig.streamCodec(Range.STREAM_CODEC, Range.class, installed + 1)
                )
    );
    public static final ModuleRegistryObject<SimpleEnchantmentAwareModule> FROST_WALKER_UNIT = MODULES.registerEnchantBased("frost_walker_unit",
          Enchantments.FROST_WALKER, () -> MekanismItems.MODULE_FROST_WALKER, builder -> builder.maxStackSize(2));
    public static final ModuleRegistryObject<SoulSurferUnit> SOUL_SURFER_UNIT = MODULES.registerInstanced("soul_surfer_unit",
          SoulSurferUnit::new, () -> MekanismItems.MODULE_SOUL_SURFER, builder -> builder.maxStackSize(3));
}
