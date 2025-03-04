package mekanism.client.recipe_viewer.alias;

import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.IResource;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

@NothingNullByDefault
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
        rv.addAliases(MekanismFluids.ETHENE, MekanismChemicals.ETHENE, MekanismAliases.ETHENE_ETHYLENE);
    }

    private <ITEM, FLUID, CHEMICAL> void addUnitAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(MekanismBlocks.MODIFICATION_STATION, MekanismAliases.UNIT_INSTALLER, MekanismAliases.UNIT_INSTALLER_MODULE);
        rv.addItemAliases(MekanismItems.MODULE_RADIATION_SHIELDING, MekanismAliases.RADIATION_PROTECTION);
        rv.addItemAliases(MekanismItems.MODULE_ENERGY, MekanismAliases.ENERGY_STORAGE);

        rv.addItemAliases(MekanismItems.MODULE_FORTUNE, getTranslationKey(Enchantments.FORTUNE));
        rv.addItemAliases(MekanismItems.MODULE_ATTACK_AMPLIFICATION, MekanismAliases.UNIT_DAMAGE, getTranslationKey(Enchantments.SHARPNESS));
        rv.addItemAliases(MekanismItems.MODULE_EXCAVATION_ESCALATION, MekanismAliases.UNIT_DIG_SPEED, getTranslationKey(Enchantments.EFFICIENCY));
        rv.addItemAliases(MekanismItems.MODULE_BLASTING, MekanismAliases.TOOL_HAMMER, MekanismAliases.UNIT_AOE, MekanismAliases.UNIT_AOE_LONG);
        rv.addItemAliases(MekanismItems.MODULE_FARMING, MekanismAliases.TOOL_AXE, MekanismAliases.TOOL_HOE, MekanismAliases.TOOL_SHOVEL);

        rv.addItemAliases(MekanismItems.MODULE_VISION_ENHANCEMENT, MobEffects.NIGHT_VISION.value()::getDescriptionId);
        rv.addItemAliases(MekanismItems.MODULE_NUTRITIONAL_INJECTION, MekanismAliases.UNIT_FEEDER);
        //Note: Jetpack module pairing with normal flight alias is in done in the gear section
        rv.addItemAliases(MekanismItems.MODULE_GRAVITATIONAL_MODULATING, MekanismAliases.CREATIVE_FLIGHT);
        rv.addItemAliases(MekanismItems.MODULE_CHARGE_DISTRIBUTION, MekanismAliases.ITEM_CHARGER);
        rv.addItemAliases(MekanismItems.MODULE_HYDRAULIC_PROPULSION, MekanismAliases.AUTO_STEP, MekanismAliases.STEP_ASSIST, MobEffects.JUMP.value()::getDescriptionId);
        rv.addItemAliases(MekanismItems.MODULE_HYDROSTATIC_REPULSOR, MekanismAliases.UNIT_HYDROSTATIC_SPEED, getTranslationKey(Enchantments.DEPTH_STRIDER));
        rv.addItemAliases(MekanismItems.MODULE_MOTORIZED_SERVO, getTranslationKey(Enchantments.SWIFT_SNEAK));
        rv.addItemAliases(MekanismItems.MODULE_LOCOMOTIVE_BOOSTING, MobEffects.MOVEMENT_SPEED.value()::getDescriptionId);
        rv.addItemAliases(MekanismItems.MODULE_SOUL_SURFER, getTranslationKey(Enchantments.SOUL_SPEED));

        rv.addModuleAliases(MekanismItems.ITEMS);
    }

    private static IHasTranslationKey getTranslationKey(ResourceKey<Enchantment> enchantmentKey) {
        return () -> Util.makeDescriptionId("enchantment", enchantmentKey.location());
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
            rv.addAliases(factoryType.getBaseBlock(), () -> Util.makeDescriptionId("alias", Mekanism.rl(factoryType.getRegistryNameComponent())));
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
              MekanismItems.ARMORED_JETPACK,
              MekanismItems.MODULE_JETPACK
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

        rv.addAliases(FluidUtils.getFilledVariant(MekanismItems.CANTEEN, MekanismFluids.NUTRITIONAL_PASTE),
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
        rv.addItemAliases(MekanismTags.Items.PERSONAL_STORAGE, MekanismAliases.PERSONAL_BACKPACK, MekanismAliases.ITEM_STORAGE, MekanismAliases.STORAGE_PORTABLE);

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
              StorageUtils.getFilledEnergyVariant(
                    ItemBlockEnergyCube.withCreativeSideConfig(ItemBlockEnergyCube.ALL_OUTPUT)
              )
        ), MekanismAliases.ENERGY_STORAGE, MekanismAliases.ENERGY_STORAGE_BATTERY, MekanismAliases.ITEM_CHARGER);
        rv.addItemAliases(MekanismItems.ENERGY_TABLET, MekanismAliases.ENERGY_STORAGE, MekanismAliases.ENERGY_STORAGE_BATTERY);

        rv.addItemAliases(List.of(
              new ItemStack(MekanismBlocks.CREATIVE_BIN),
              new ItemStack(MekanismBlocks.CREATIVE_FLUID_TANK),
              new ItemStack(MekanismBlocks.CREATIVE_CHEMICAL_TANK),
              ItemBlockEnergyCube.withCreativeSideConfig(ItemBlockEnergyCube.ALL_INPUT)
        ), MekanismAliases.STORAGE_TRASHCAN, MekanismAliases.STORAGE_VOID);
    }

    private <ITEM, FLUID, CHEMICAL> void addStorageBlockAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(MekanismBlocks.BRONZE_BLOCK, MekanismAliases.BLOCK_BRONZE);
        rv.addAliases(MekanismBlocks.CHARCOAL_BLOCK, MekanismAliases.BLOCK_CHARCOAL);
        rv.addAliases(MekanismBlocks.STEEL_BLOCK, MekanismAliases.BLOCK_STEEL);
        rv.addAliases(MekanismBlocks.FLUORITE_BLOCK, MekanismAliases.BLOCK_FLUORITE);
        //Dynamic storage blocks
        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            rv.addItemAliases(entry.getValue().getItemHolder(), () -> Util.makeDescriptionId("alias", Mekanism.rl(entry.getKey().getRegistrySuffix())));
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
        rv.addItemAliases(MekanismItems.SPEED_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.UPGRADE_OVERCLOCK);
        rv.addItemAliases(MekanismItems.ENERGY_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.ENERGY_STORAGE);
        rv.addItemAliases(MekanismItems.FILTER_UPGRADE, MekanismAliases.UPGRADE_AUGMENT);
        rv.addItemAliases(MekanismItems.MUFFLING_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.UPGRADE_MUFFLER);
        rv.addItemAliases(MekanismItems.CHEMICAL_UPGRADE, MekanismAliases.UPGRADE_AUGMENT);
        rv.addItemAliases(MekanismItems.ANCHOR_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.CHUNK_LOADER);
        rv.addItemAliases(MekanismItems.STONE_GENERATOR_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.UPGRADE_HOLE_FILLER);
        rv.addItemHolderAliases(List.of(
              MekanismItems.BASIC_TIER_INSTALLER,
              MekanismItems.ADVANCED_TIER_INSTALLER,
              MekanismItems.ELITE_TIER_INSTALLER,
              MekanismItems.ULTIMATE_TIER_INSTALLER
        ), MekanismAliases.INSTALLER_FACTORY, MekanismAliases.INSTALLER_UPGRADE);
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