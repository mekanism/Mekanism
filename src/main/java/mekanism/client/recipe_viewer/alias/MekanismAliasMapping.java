package mekanism.client.recipe_viewer.alias;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.upgrade.IUpgradeHelper;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.Mekanism;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.resource.IResource;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

public final class MekanismAliasMapping implements IAliasMapping {

    @Override
    public <ITEM, FLUID, CHEMICAL> void addAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        addChemicalAliases(rv);
        addUnitAliases(rv);
        addFactoryAliases(rv);
        addGearAliases(rv);
        addMultiblockAliases(rv);
        addStorageAliases(rv);
        addTransferAliases(rv);
        addUpgradeAliases(rv);
        addMiscAliases(rv);
    }

    private <ITEM, FLUID, CHEMICAL> void addChemicalAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(MekanismFluids.ETHENE, ChemicalIds.ETHENE, MekanismAliases.ETHENE_ETHYLENE);
    }

    private <ITEM, FLUID, CHEMICAL> void addUnitAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(MekanismBlocks.MODIFICATION_STATION, MekanismAliases.UNIT_INSTALLER, MekanismAliases.UNIT_INSTALLER_MODULE);
        addModuleAliases(rv, MekanismModules.RADIATION_SHIELDING_UNIT, MekanismAliases.RADIATION_PROTECTION);
        addModuleAliases(rv, MekanismModules.ENERGY_UNIT, MekanismAliases.ENERGY_STORAGE);

        addModuleAliases(rv, MekanismModules.FORTUNE_UNIT, getTranslationKey(Enchantments.FORTUNE));
        addModuleAliases(rv, MekanismModules.ATTACK_AMPLIFICATION_UNIT, MekanismAliases.UNIT_DAMAGE, getTranslationKey(Enchantments.SHARPNESS));
        addModuleAliases(rv, MekanismModules.EXCAVATION_ESCALATION_UNIT, MekanismAliases.UNIT_DIG_SPEED, getTranslationKey(Enchantments.EFFICIENCY));
        addModuleAliases(rv, MekanismModules.BLASTING_UNIT, MekanismAliases.TOOL_HAMMER, MekanismAliases.UNIT_AOE, MekanismAliases.UNIT_AOE_LONG);
        addModuleAliases(rv, MekanismModules.FARMING_UNIT, MekanismAliases.TOOL_AXE, MekanismAliases.TOOL_HOE, MekanismAliases.TOOL_SHOVEL);

        addModuleAliases(rv, MekanismModules.VISION_ENHANCEMENT_UNIT, MobEffects.NIGHT_VISION.value()::getDescriptionId);
        addModuleAliases(rv, MekanismModules.NUTRITIONAL_INJECTION_UNIT, MekanismAliases.UNIT_FEEDER);
        //Note: Jetpack module pairing with normal flight alias is in done in the gear section
        addModuleAliases(rv, MekanismModules.GRAVITATIONAL_MODULATING_UNIT, MekanismAliases.CREATIVE_FLIGHT);
        addModuleAliases(rv, MekanismModules.CHARGE_DISTRIBUTION_UNIT, MekanismAliases.ITEM_CHARGER);
        addModuleAliases(rv, MekanismModules.HYDRAULIC_PROPULSION_UNIT, MekanismAliases.AUTO_STEP, MekanismAliases.STEP_ASSIST, MobEffects.JUMP_BOOST.value()::getDescriptionId);
        addModuleAliases(rv, MekanismModules.HYDROSTATIC_REPULSOR_UNIT, MekanismAliases.UNIT_HYDROSTATIC_SPEED, getTranslationKey(Enchantments.DEPTH_STRIDER));
        addModuleAliases(rv, MekanismModules.MOTORIZED_SERVO_UNIT, getTranslationKey(Enchantments.SWIFT_SNEAK));
        addModuleAliases(rv, MekanismModules.LOCOMOTIVE_BOOSTING_UNIT, MobEffects.SPEED.value()::getDescriptionId);
        addModuleAliases(rv, MekanismModules.SOUL_SURFER_UNIT, getTranslationKey(Enchantments.SOUL_SPEED));
        addModuleAliases(rv, MekanismModules.JETPACK_UNIT, MekanismAliases.FLIGHT);

        rv.addModuleAliases(MekanismModules.MODULES);
    }

    private <ITEM, FLUID, CHEMICAL> void addModuleAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv, Holder<ModuleData<?>> module, IHasTranslationKey... aliases) {
        rv.addAliases(IModuleHelper.INSTANCE.asStack(module), aliases);
    }

    private static IHasTranslationKey getTranslationKey(ResourceKey<Enchantment> enchantmentKey) {
        return () -> enchantmentKey.identifier().toLanguageKey("enchantment");
    }

    private <ITEM, FLUID, CHEMICAL> void addFactoryAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        for (FactoryType factoryType : EnumUtils.FACTORY_TYPES) {
            //Allow searching for factories by the name of the base block
            rv.addAliases(List.of(
                  MekanismBlocks.getFactory(FactoryTier.BASIC, factoryType),
                  MekanismBlocks.getFactory(FactoryTier.ADVANCED, factoryType),
                  MekanismBlocks.getFactory(FactoryTier.ELITE, factoryType),
                  MekanismBlocks.getFactory(FactoryTier.ULTIMATE, factoryType)
            ), factoryType.getBaseBlock());
            //Add the type as a way to look-up the base block
            rv.addAliases(factoryType.getBaseBlock(), () -> Mekanism.rl(factoryType.getRegistryNameComponent()).toLanguageKey("alias"));
        }
    }

    private <ITEM, FLUID, CHEMICAL> void addGearAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addItemHolderAliases(List.of(MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.MEKA_TOOL),
              MekanismAliases.TOOL_MULTI,
              MekanismAliases.TOOL_AXE,
              MekanismAliases.TOOL_PICKAXE,
              MekanismAliases.TOOL_SHOVEL,
              MekanismAliases.TOOL_SWORD,
              MekanismAliases.TOOL_WEAPON
        );

        rv.addItemAliases(MekanismItems.CONFIGURATOR, MekanismAliases.TOOL_DIAGNOSTIC, MekanismAliases.TOOL_WRENCH);
        rv.addItemHolderAliases(List.of(
              MekanismItems.NETWORK_READER,
              MekanismItems.CONFIGURATION_CARD
        ), MekanismAliases.TOOL_DIAGNOSTIC);

        rv.addItemHolderAliases(List.of(
              MekanismItems.JETPACK,
              MekanismItems.ARMORED_JETPACK
        ), MekanismAliases.FLIGHT);
        rv.addItemHolderAliases(List.of(
              MekanismItems.HAZMAT_MASK,
              MekanismItems.HAZMAT_GOWN,
              MekanismItems.HAZMAT_PANTS,
              MekanismItems.HAZMAT_BOOTS
        ), MekanismAliases.RADIATION_PROTECTION);

        rv.addItemHolderAliases(List.of(MekanismItems.FREE_RUNNERS, MekanismItems.ARMORED_FREE_RUNNERS),
              MekanismAliases.FREE_RUNNER_LONG_FALL,
              MekanismAliases.FREE_RUNNER_FALL_PROTECTION,
              MekanismAliases.AUTO_STEP,
              MekanismAliases.STEP_ASSIST
        );
        rv.addItemAliases(MekanismItems.MEKASUIT_BOOTS, MekanismAliases.FREE_RUNNER_LONG_FALL, MekanismAliases.FREE_RUNNER_FALL_PROTECTION, MekanismAliases.MEKA_SUIT_POWER_ARMOR);
        rv.addItemHolderAliases(List.of(
              MekanismItems.MEKASUIT_HELMET,
              MekanismItems.MEKASUIT_BODYARMOR,
              MekanismItems.MEKASUIT_PANTS
        ), MekanismAliases.MEKA_SUIT_POWER_ARMOR);

        rv.addAliases(ContainerType.FLUID.getFilledVariant(MekanismItems.CANTEEN, MekanismFluids.NUTRITIONAL_PASTE, null),
              MekanismAliases.CANTEEN_EDIBLE,
              MekanismAliases.CANTEEN_FOOD_STORAGE
        );
    }

    private <ITEM, FLUID, CHEMICAL> void addMultiblockAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(List.of(
              MekanismBlocks.BOILER_CASING,
              MekanismBlocks.BOILER_VALVE,
              MekanismBlocks.PRESSURE_DISPERSER,
              MekanismBlocks.SUPERHEATING_ELEMENT,
              MekanismBlocks.STRUCTURAL_GLASS
        ), MekanismAliases.BOILER_COMPONENT);
        rv.addAliases(List.of(
              MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER,
              MekanismBlocks.THERMAL_EVAPORATION_BLOCK,
              MekanismBlocks.THERMAL_EVAPORATION_VALVE,
              MekanismBlocks.STRUCTURAL_GLASS
        ), MekanismAliases.EVAPORATION_COMPONENT);

        rv.addAliases(List.of(
              MekanismBlocks.INDUCTION_CASING,
              MekanismBlocks.INDUCTION_PORT,
              MekanismBlocks.BASIC_INDUCTION_CELL,
              MekanismBlocks.BASIC_INDUCTION_PROVIDER,
              MekanismBlocks.ADVANCED_INDUCTION_CELL,
              MekanismBlocks.ADVANCED_INDUCTION_PROVIDER,
              MekanismBlocks.ELITE_INDUCTION_CELL,
              MekanismBlocks.ELITE_INDUCTION_PROVIDER,
              MekanismBlocks.ULTIMATE_INDUCTION_CELL,
              MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER,
              MekanismBlocks.STRUCTURAL_GLASS
        ), MekanismAliases.MATRIX_COMPONENT);
        rv.addAliases(List.of(
              MekanismBlocks.INDUCTION_CASING,
              MekanismBlocks.INDUCTION_PORT,
              MekanismBlocks.BASIC_INDUCTION_CELL,
              MekanismBlocks.ADVANCED_INDUCTION_CELL,
              MekanismBlocks.ELITE_INDUCTION_CELL,
              MekanismBlocks.ULTIMATE_INDUCTION_CELL
        ), MekanismAliases.ENERGY_STORAGE, MekanismAliases.ENERGY_STORAGE_BATTERY, MekanismAliases.ITEM_CHARGER);
        rv.addAliases(List.of(
              MekanismBlocks.INDUCTION_PORT,
              MekanismBlocks.BASIC_INDUCTION_PROVIDER,
              MekanismBlocks.ADVANCED_INDUCTION_PROVIDER,
              MekanismBlocks.ELITE_INDUCTION_PROVIDER,
              MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER
        ), MekanismAliases.ENERGY_TRANSFER, MekanismAliases.ENERGY_THROUGHPUT, MekanismAliases.ITEM_CHARGER);

        rv.addAliases(List.of(
              MekanismBlocks.SPS_CASING,
              MekanismBlocks.SPS_PORT,
              MekanismBlocks.SUPERCHARGED_COIL,
              MekanismBlocks.STRUCTURAL_GLASS
        ), MekanismAliases.SPS_COMPONENT, MekanismAliases.SPS_FULL_COMPONENT);
        rv.addAliases(List.of(MekanismBlocks.DYNAMIC_TANK, MekanismBlocks.DYNAMIC_VALVE, MekanismBlocks.STRUCTURAL_GLASS),
              MekanismAliases.TANK_COMPONENT,
              MekanismAliases.FLUID_STORAGE,
              MekanismAliases.CHEMICAL_STORAGE,
              MekanismAliases.GAS_STORAGE,
              MekanismAliases.INFUSE_TYPE_STORAGE,
              MekanismAliases.INFUSION_STORAGE,
              MekanismAliases.PIGMENT_STORAGE,
              MekanismAliases.SLURRY_STORAGE
        );
    }

    private <ITEM, FLUID, CHEMICAL> void addStorageAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        addStorageBlockAliases(rv);
        addQIOAliases(rv);
        rv.addAliases(List.of(
              MekanismBlocks.BASIC_BIN,
              MekanismBlocks.ADVANCED_BIN,
              MekanismBlocks.ELITE_BIN,
              MekanismBlocks.ULTIMATE_BIN,
              MekanismBlocks.CREATIVE_BIN
        ), MekanismAliases.BIN_DRAWER, MekanismAliases.ITEM_STORAGE);
        rv.addItemAliases(MekanismTags.BlockItems.PERSONAL_STORAGE.item(), MekanismAliases.PERSONAL_BACKPACK, MekanismAliases.ITEM_STORAGE, MekanismAliases.STORAGE_PORTABLE);

        rv.addAliases(List.of(
              MekanismBlocks.BASIC_FLUID_TANK,
              MekanismBlocks.ADVANCED_FLUID_TANK,
              MekanismBlocks.ELITE_FLUID_TANK,
              MekanismBlocks.ULTIMATE_FLUID_TANK,
              MekanismBlocks.CREATIVE_FLUID_TANK
        ), MekanismAliases.FLUID_STORAGE, MekanismAliases.STORAGE_PORTABLE, Items.BUCKET::getDescriptionId);//Note: We add bucket as the tanks can act as buckets

        rv.addAliases(List.of(
                    MekanismBlocks.BASIC_CHEMICAL_TANK,
                    MekanismBlocks.ADVANCED_CHEMICAL_TANK,
                    MekanismBlocks.ELITE_CHEMICAL_TANK,
                    MekanismBlocks.ULTIMATE_CHEMICAL_TANK,
                    MekanismBlocks.CREATIVE_CHEMICAL_TANK
              ),
              MekanismAliases.CHEMICAL_STORAGE,
              MekanismAliases.GAS_STORAGE,
              MekanismAliases.INFUSE_TYPE_STORAGE,
              MekanismAliases.INFUSION_STORAGE,
              MekanismAliases.PIGMENT_STORAGE,
              MekanismAliases.SLURRY_STORAGE
        );

        rv.addItemAliases(List.of(
              new ItemStack(MekanismBlocks.BASIC_ENERGY_CUBE),
              new ItemStack(MekanismBlocks.ADVANCED_ENERGY_CUBE),
              new ItemStack(MekanismBlocks.ELITE_ENERGY_CUBE),
              new ItemStack(MekanismBlocks.ULTIMATE_ENERGY_CUBE),
              ContainerType.ENERGY.getFilledVariant(ItemBlockEnergyCube.withCreativeSideConfig(ItemBlockEnergyCube.ALL_OUTPUT), null)
        ), MekanismAliases.ENERGY_STORAGE, MekanismAliases.ENERGY_STORAGE_BATTERY, MekanismAliases.ITEM_CHARGER);
        rv.addItemAliases(MekanismItems.ENERGY_TABLET, MekanismAliases.ENERGY_STORAGE, MekanismAliases.ENERGY_STORAGE_BATTERY);

        rv.addItemAliases(List.of(
              new ItemStack(MekanismBlocks.CREATIVE_BIN),
              new ItemStack(MekanismBlocks.CREATIVE_FLUID_TANK),
              new ItemStack(MekanismBlocks.CREATIVE_CHEMICAL_TANK),
              ItemBlockEnergyCube.withCreativeSideConfig(ItemBlockEnergyCube.ALL_INPUT).toStack()
        ), MekanismAliases.STORAGE_TRASHCAN, MekanismAliases.STORAGE_VOID);
    }

    private <ITEM, FLUID, CHEMICAL> void addStorageBlockAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(MekanismBlocks.BRONZE_BLOCK, MekanismAliases.BLOCK_BRONZE);
        rv.addAliases(MekanismBlocks.CHARCOAL_BLOCK, MekanismAliases.BLOCK_CHARCOAL);
        rv.addAliases(MekanismBlocks.STEEL_BLOCK, MekanismAliases.BLOCK_STEEL);
        rv.addAliases(MekanismBlocks.FLUORITE_BLOCK, MekanismAliases.BLOCK_FLUORITE);
        //Dynamic storage blocks
        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            rv.addItemAliases(entry.getValue().getItemHolder(), () -> Mekanism.rl(entry.getKey().getRegistrySuffix()).toLanguageKey("alias"));
        }
    }

    private <ITEM, FLUID, CHEMICAL> void addQIOAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(MekanismBlocks.QIO_REDSTONE_ADAPTER, MekanismAliases.QIO_FULL, MekanismAliases.QIO_ADAPTER_EMITTER);
        rv.addAliases(MekanismBlocks.QIO_DRIVE_ARRAY, MekanismAliases.QIO_FULL, MekanismAliases.QIO_DRIVE_BAY);
        rv.addItemAliases(MekanismItems.PORTABLE_QIO_DASHBOARD, MekanismAliases.PORTABLE_CRAFTING_TABLE, MekanismAliases.QIO_DASHBOARD_WIRELESS_TERMINAL);
        rv.addAliases(List.of(
              MekanismBlocks.QIO_DASHBOARD,
              MekanismItems.PORTABLE_QIO_DASHBOARD
        ), MekanismAliases.QIO_FULL, MekanismAliases.QIO_DASHBOARD_TERMINAL, MekanismAliases.QIO_DASHBOARD_GRID);
        rv.addItemHolderAliases(List.of(
              MekanismItems.BASE_QIO_DRIVE,
              MekanismItems.HYPER_DENSE_QIO_DRIVE,
              MekanismItems.TIME_DILATING_QIO_DRIVE,
              MekanismItems.SUPERMASSIVE_QIO_DRIVE
        ), MekanismAliases.QIO_FULL, MekanismAliases.QIO_DRIVE_CELL, MekanismAliases.QIO_DRIVE_DISK, MekanismAliases.ITEM_STORAGE);
        rv.addAliases(MekanismBlocks.QIO_EXPORTER, MekanismAliases.QIO_FULL, MekanismAliases.ROUND_ROBIN);
        rv.addAliases(MekanismBlocks.QIO_IMPORTER, MekanismAliases.QIO_FULL);
    }

    private <ITEM, FLUID, CHEMICAL> void addTransferAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(List.of(
                    MekanismBlocks.BASIC_UNIVERSAL_CABLE,
                    MekanismBlocks.ADVANCED_UNIVERSAL_CABLE,
                    MekanismBlocks.ELITE_UNIVERSAL_CABLE,
                    MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE
              ),
              MekanismAliases.ENERGY_TRANSFER,
              MekanismAliases.TRANSMITTER,
              MekanismAliases.TRANSMITTER_CONDUIT,
              MekanismAliases.TRANSMITTER_PIPE,
              MekanismAliases.TRANSMITTER_TUBE
        );

        rv.addAliases(List.of(
                    MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR,
                    MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR,
                    MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR,
                    MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR
              ),
              MekanismAliases.HEAT_TRANSFER,
              MekanismAliases.TRANSMITTER,
              MekanismAliases.TRANSMITTER_CONDUIT,
              MekanismAliases.TRANSMITTER_PIPE,
              MekanismAliases.TRANSMITTER_TUBE
        );

        rv.addAliases(List.of(
              MekanismBlocks.BASIC_MECHANICAL_PIPE,
              MekanismBlocks.ADVANCED_MECHANICAL_PIPE,
              MekanismBlocks.ELITE_MECHANICAL_PIPE,
              MekanismBlocks.ULTIMATE_MECHANICAL_PIPE
        ), MekanismAliases.FLUID_TRANSFER, MekanismAliases.TRANSMITTER, MekanismAliases.TRANSMITTER_CONDUIT, MekanismAliases.TRANSMITTER_TUBE);

        rv.addAliases(List.of(
                    MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER,
                    MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER,
                    MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER,
                    MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER,
                    MekanismBlocks.RESTRICTIVE_TRANSPORTER,
                    MekanismBlocks.DIVERSION_TRANSPORTER
              ),
              MekanismAliases.ITEM_TRANSFER,
              MekanismAliases.TRANSMITTER,
              MekanismAliases.TRANSMITTER_CONDUIT,
              MekanismAliases.TRANSMITTER_PIPE,
              MekanismAliases.TRANSMITTER_TUBE
        );
        rv.addAliases(MekanismBlocks.DIVERSION_TRANSPORTER, MekanismAliases.REDSTONE_CONTROL);
        rv.addAliases(MekanismBlocks.LOGISTICAL_SORTER, MekanismAliases.ITEM_TRANSFER, MekanismAliases.ROUND_ROBIN);

        rv.addAliases(List.of(
                    MekanismBlocks.BASIC_PRESSURIZED_TUBE,
                    MekanismBlocks.ADVANCED_PRESSURIZED_TUBE,
                    MekanismBlocks.ELITE_PRESSURIZED_TUBE,
                    MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE
              ),
              MekanismAliases.CHEMICAL_TRANSFER,
              MekanismAliases.GAS_TRANSFER,
              MekanismAliases.INFUSE_TYPE_TRANSFER,
              MekanismAliases.INFUSION_TRANSFER,
              MekanismAliases.PIGMENT_TRANSFER,
              MekanismAliases.SLURRY_TRANSFER,
              MekanismAliases.TRANSMITTER,
              MekanismAliases.TRANSMITTER_CONDUIT,
              MekanismAliases.TRANSMITTER_PIPE
        );

        rv.addAliases(MekanismBlocks.QUANTUM_ENTANGLOPORTER,
              MekanismAliases.ENERGY_TRANSFER,
              MekanismAliases.HEAT_TRANSFER,
              MekanismAliases.FLUID_TRANSFER,
              MekanismAliases.ITEM_TRANSFER,
              MekanismAliases.CHEMICAL_TRANSFER,
              MekanismAliases.GAS_TRANSFER,
              MekanismAliases.INFUSE_TYPE_TRANSFER,
              MekanismAliases.INFUSION_TRANSFER,
              MekanismAliases.PIGMENT_TRANSFER,
              MekanismAliases.SLURRY_TRANSFER,
              //Other names
              MekanismAliases.QE_TESSERACT,
              MekanismAliases.QE_ENDER_TANK,
              //When paired with Ender (which we get from the ender tank piece)
              MekanismAliases.TRANSMITTER,
              MekanismAliases.TRANSMITTER_CONDUIT,
              MekanismAliases.TRANSMITTER_PIPE,
              MekanismAliases.TRANSMITTER_TUBE
        );
    }

    private <ITEM, FLUID, CHEMICAL> void addUpgradeAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        HolderGetter<Upgrade> upgrades = rv.registries().lookupOrThrow(MekanismRegistries.Keys.UPGRADES);
        rv.addItemAliases(MekanismItems.UPGRADE, MekanismAliases.UPGRADE_AUGMENT);
        addUpgrade(rv, upgrades, UpgradeIds.SPEED, MekanismAliases.UPGRADE_OVERCLOCK);
        addUpgrade(rv, upgrades, UpgradeIds.ENERGY, MekanismAliases.ENERGY_STORAGE);
        addUpgrade(rv, upgrades, UpgradeIds.MUFFLING, MekanismAliases.UPGRADE_MUFFLER);
        addUpgrade(rv, upgrades, UpgradeIds.ANCHOR, MekanismAliases.CHUNK_LOADER);
        addUpgrade(rv, upgrades, UpgradeIds.STONE_GENERATOR, MekanismAliases.UPGRADE_HOLE_FILLER);
        rv.addItemHolderAliases(List.of(
              MekanismItems.BASIC_TIER_INSTALLER,
              MekanismItems.ADVANCED_TIER_INSTALLER,
              MekanismItems.ELITE_TIER_INSTALLER,
              MekanismItems.ULTIMATE_TIER_INSTALLER
        ), MekanismAliases.INSTALLER_FACTORY, MekanismAliases.INSTALLER_UPGRADE);
    }

    private <ITEM, FLUID, CHEMICAL> void addUpgrade(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv, HolderGetter<Upgrade> upgrades, ResourceKey<Upgrade> upgrade, IHasTranslationKey... aliases) {
        Optional<Reference<Upgrade>> upgradeReference = upgrades.get(upgrade);
        if (upgradeReference.isPresent()) {
            rv.addAliases(IUpgradeHelper.INSTANCE.asStack(upgradeReference.get()), aliases);
        } else if (DatagenModLoader.isRunningDataGen()) {
            throw new IllegalStateException("Missing element " + upgrade);
        }
    }

    private <ITEM, FLUID, CHEMICAL> void addMiscAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addItemAliases(MekanismItems.CRAFTING_FORMULA, MekanismAliases.CRAFTING_PATTERN);
        rv.addAliases(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, MekanismAliases.AUTO_CRAFTER);
        rv.addAliases(MekanismBlocks.DIMENSIONAL_STABILIZER, MekanismAliases.CHUNK_LOADER);
        rv.addAliases(MekanismBlocks.FLUIDIC_PLENISHER, MekanismAliases.PLENISHER_PLACER, MekanismAliases.PLENISHER_REVERSE);
        rv.addAliases(MekanismBlocks.OREDICTIONIFICATOR, MekanismAliases.TAG_CONVERTER);
        rv.addAliases(MekanismBlocks.ROTARY_CONDENSENTRATOR,
              MekanismAliases.ROTARY_DECONDENSENTRATOR,
              MekanismAliases.ROTARY_CHEMICAL_TO_FLUID,
              MekanismAliases.ROTARY_GAS_TO_FLUID,
              MekanismAliases.ROTARY_FLUID_TO_CHEMICAL,
              MekanismAliases.ROTARY_FLUID_TO_GAS
        );
    }
}