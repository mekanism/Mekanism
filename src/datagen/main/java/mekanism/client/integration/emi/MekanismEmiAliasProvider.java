package mekanism.client.integration.emi;

import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.integration.MekanismAliases;
import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.IResource;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

@NothingNullByDefault
public class MekanismEmiAliasProvider extends BaseEmiAliasProvider {

    public MekanismEmiAliasProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Mekanism.MODID);
    }

    @Override
    protected void addAliases(HolderLookup.Provider lookupProvider) {
        addChemicalAliases();
        addUnitAliases();
        addFactoryAliases();
        addGearAliases();
        addMultiblockAliases();
        addStorageAliases();
        addTransferAliases();
        addUpgradeAliases();
        addMiscAliases();
    }

    private void addChemicalAliases() {
        addAliases(List.of(
              ingredient(MekanismFluids.ETHENE),
              ingredient(MekanismGases.ETHENE)
        ), MekanismAliases.ETHENE_ETHYLENE);
    }

    private void addUnitAliases() {
        addAliases(MekanismBlocks.MODIFICATION_STATION, MekanismAliases.UNIT_INSTALLER, MekanismAliases.UNIT_INSTALLER_MODULE);
        addAliases(MekanismItems.MODULE_RADIATION_SHIELDING, MekanismAliases.RADIATION_PROTECTION);
        addAliases(MekanismItems.MODULE_ENERGY, MekanismAliases.ENERGY_STORAGE);

        addAliases(MekanismItems.MODULE_FORTUNE, getTranslationKey(Enchantments.FORTUNE));
        addAliases(MekanismItems.MODULE_ATTACK_AMPLIFICATION, MekanismAliases.UNIT_DAMAGE, getTranslationKey(Enchantments.SHARPNESS));
        addAliases(MekanismItems.MODULE_EXCAVATION_ESCALATION, MekanismAliases.UNIT_DIG_SPEED, getTranslationKey(Enchantments.EFFICIENCY));
        addAliases(MekanismItems.MODULE_BLASTING, MekanismAliases.TOOL_HAMMER, MekanismAliases.UNIT_AOE, MekanismAliases.UNIT_AOE_LONG);
        addAliases(MekanismItems.MODULE_FARMING, MekanismAliases.TOOL_AXE, MekanismAliases.TOOL_HOE, MekanismAliases.TOOL_SHOVEL);

        addAliases(MekanismItems.MODULE_VISION_ENHANCEMENT, MobEffects.NIGHT_VISION.value()::getDescriptionId);
        addAliases(MekanismItems.MODULE_NUTRITIONAL_INJECTION, MekanismAliases.UNIT_FEEDER);
        //Note: Jetpack module pairing with normal flight alias is in done in the gear section
        addAliases(MekanismItems.MODULE_GRAVITATIONAL_MODULATING, MekanismAliases.CREATIVE_FLIGHT);
        addAliases(MekanismItems.MODULE_CHARGE_DISTRIBUTION, MekanismAliases.ITEM_CHARGER);
        addAliases(MekanismItems.MODULE_HYDRAULIC_PROPULSION, MekanismAliases.AUTO_STEP, MekanismAliases.STEP_ASSIST, MobEffects.JUMP.value()::getDescriptionId);
        addAliases(MekanismItems.MODULE_HYDROSTATIC_REPULSOR, MekanismAliases.UNIT_HYDROSTATIC_SPEED, getTranslationKey(Enchantments.DEPTH_STRIDER));
        addAliases(MekanismItems.MODULE_MOTORIZED_SERVO, getTranslationKey(Enchantments.SWIFT_SNEAK));
        addAliases(MekanismItems.MODULE_LOCOMOTIVE_BOOSTING, MobEffects.MOVEMENT_SPEED.value()::getDescriptionId);
        addAliases(MekanismItems.MODULE_SOUL_SURFER, getTranslationKey(Enchantments.SOUL_SPEED));
    }

    private IHasTranslationKey getTranslationKey(ResourceKey<Enchantment> enchantmentKey) {
        return () -> Util.makeDescriptionId("enchantment", enchantmentKey.location());
    }

    private void addFactoryAliases() {
        for (FactoryType factoryType : EnumUtils.FACTORY_TYPES) {
            //Allow searching for factories by the name of the base block
            addAlias(factoryType.getBaseBlock(),
                  MekanismBlocks.getFactory(FactoryTier.BASIC, factoryType),
                  MekanismBlocks.getFactory(FactoryTier.ADVANCED, factoryType),
                  MekanismBlocks.getFactory(FactoryTier.ELITE, factoryType),
                  MekanismBlocks.getFactory(FactoryTier.ULTIMATE, factoryType)
            );
            //Add the type as a way to look-up the base block
            addAliases(factoryType.getBaseBlock(), () -> Util.makeDescriptionId("alias", Mekanism.rl(factoryType.getRegistryNameComponent())));
        }
    }

    private void addGearAliases() {
        addAliases(MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.MEKA_TOOL,
              MekanismAliases.TOOL_MULTI,
              MekanismAliases.TOOL_AXE,
              MekanismAliases.TOOL_PICKAXE,
              MekanismAliases.TOOL_SHOVEL,
              MekanismAliases.TOOL_SWORD,
              MekanismAliases.TOOL_WEAPON
        );

        addAliases(MekanismItems.CONFIGURATOR, MekanismAliases.TOOL_DIAGNOSTIC, MekanismAliases.TOOL_WRENCH);
        addAlias(MekanismAliases.TOOL_DIAGNOSTIC, MekanismItems.NETWORK_READER, MekanismItems.CONFIGURATION_CARD);

        addAlias(MekanismAliases.FLIGHT, MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK, MekanismItems.MODULE_JETPACK);
        addAlias(MekanismAliases.RADIATION_PROTECTION, MekanismItems.HAZMAT_MASK, MekanismItems.HAZMAT_GOWN, MekanismItems.HAZMAT_PANTS, MekanismItems.HAZMAT_BOOTS);

        addAliases(List.of(MekanismItems.FREE_RUNNERS, MekanismItems.ARMORED_FREE_RUNNERS),
              MekanismAliases.FREE_RUNNER_LONG_FALL,
              MekanismAliases.FREE_RUNNER_FALL_PROTECTION,
              MekanismAliases.AUTO_STEP,
              MekanismAliases.STEP_ASSIST
        );
        addAliases(MekanismItems.MEKASUIT_BOOTS, MekanismAliases.FREE_RUNNER_LONG_FALL, MekanismAliases.FREE_RUNNER_FALL_PROTECTION, MekanismAliases.MEKA_SUIT_POWER_ARMOR);
        addAlias(MekanismAliases.MEKA_SUIT_POWER_ARMOR, MekanismItems.MEKASUIT_HELMET, MekanismItems.MEKASUIT_BODYARMOR, MekanismItems.MEKASUIT_PANTS);

        addAliases(EmiStack.of(
              FluidUtils.getFilledVariant(MekanismItems.CANTEEN, MekanismFluids.NUTRITIONAL_PASTE.getFluid())
        ), MekanismAliases.CANTEEN_EDIBLE, MekanismAliases.CANTEEN_FOOD_STORAGE);
    }

    private void addMultiblockAliases() {
        addAlias(MekanismAliases.BOILER_COMPONENT,
              MekanismBlocks.BOILER_CASING,
              MekanismBlocks.BOILER_VALVE,
              MekanismBlocks.PRESSURE_DISPERSER,
              MekanismBlocks.SUPERHEATING_ELEMENT,
              MekanismBlocks.STRUCTURAL_GLASS
        );
        addAlias(MekanismAliases.EVAPORATION_COMPONENT,
              MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER,
              MekanismBlocks.THERMAL_EVAPORATION_BLOCK,
              MekanismBlocks.THERMAL_EVAPORATION_VALVE,
              MekanismBlocks.STRUCTURAL_GLASS
        );

        addAliases(List.of(
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
        addAliases(List.of(
              MekanismBlocks.INDUCTION_CASING,
              MekanismBlocks.INDUCTION_PORT,
              MekanismBlocks.BASIC_INDUCTION_CELL,
              MekanismBlocks.ADVANCED_INDUCTION_CELL,
              MekanismBlocks.ELITE_INDUCTION_CELL,
              MekanismBlocks.ULTIMATE_INDUCTION_CELL
        ), MekanismAliases.ENERGY_STORAGE, MekanismAliases.ENERGY_STORAGE_BATTERY, MekanismAliases.ITEM_CHARGER);
        addAliases(List.of(
              MekanismBlocks.INDUCTION_PORT,
              MekanismBlocks.BASIC_INDUCTION_PROVIDER,
              MekanismBlocks.ADVANCED_INDUCTION_PROVIDER,
              MekanismBlocks.ELITE_INDUCTION_PROVIDER,
              MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER
        ), MekanismAliases.ENERGY_TRANSFER, MekanismAliases.ENERGY_THROUGHPUT, MekanismAliases.ITEM_CHARGER);

        addAliases(List.of(
              MekanismBlocks.SPS_CASING,
              MekanismBlocks.SPS_PORT,
              MekanismBlocks.SUPERCHARGED_COIL,
              MekanismBlocks.STRUCTURAL_GLASS
        ), MekanismAliases.SPS_COMPONENT, MekanismAliases.SPS_FULL_COMPONENT);
        addAliases(List.of(MekanismBlocks.DYNAMIC_TANK, MekanismBlocks.DYNAMIC_VALVE, MekanismBlocks.STRUCTURAL_GLASS),
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

    private void addStorageAliases() {
        addStorageBlockAliases();
        addQIOAliases();
        addAliases(List.of(
              MekanismBlocks.BASIC_BIN,
              MekanismBlocks.ADVANCED_BIN,
              MekanismBlocks.ELITE_BIN,
              MekanismBlocks.ULTIMATE_BIN,
              MekanismBlocks.CREATIVE_BIN
        ), MekanismAliases.BIN_DRAWER, MekanismAliases.ITEM_STORAGE);
        addAliases(List.of(
              MekanismBlocks.PERSONAL_BARREL,
              MekanismBlocks.PERSONAL_CHEST
        ), MekanismAliases.PERSONAL_BACKPACK, MekanismAliases.ITEM_STORAGE, MekanismAliases.STORAGE_PORTABLE);

        addAliases(List.of(
              MekanismBlocks.BASIC_FLUID_TANK,
              MekanismBlocks.ADVANCED_FLUID_TANK,
              MekanismBlocks.ELITE_FLUID_TANK,
              MekanismBlocks.ULTIMATE_FLUID_TANK,
              MekanismBlocks.CREATIVE_FLUID_TANK
        ), MekanismAliases.FLUID_STORAGE, MekanismAliases.STORAGE_PORTABLE, Items.BUCKET::getDescriptionId);//Note: We add bucket as the tanks can act as buckets

        addAliases(List.of(
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

        addAliases(List.of(
              EmiStack.of(MekanismBlocks.BASIC_ENERGY_CUBE),
              EmiStack.of(MekanismBlocks.ADVANCED_ENERGY_CUBE),
              EmiStack.of(MekanismBlocks.ELITE_ENERGY_CUBE),
              EmiStack.of(MekanismBlocks.ULTIMATE_ENERGY_CUBE),
              EmiStack.of(StorageUtils.getFilledEnergyVariant(
                    ItemBlockEnergyCube.withEnergyCubeSideConfig(MekanismBlocks.CREATIVE_ENERGY_CUBE, ItemBlockEnergyCube.ALL_OUTPUT)
              ))
        ), MekanismAliases.ENERGY_STORAGE, MekanismAliases.ENERGY_STORAGE_BATTERY, MekanismAliases.ITEM_CHARGER);
        addAliases(MekanismItems.ENERGY_TABLET, MekanismAliases.ENERGY_STORAGE, MekanismAliases.ENERGY_STORAGE_BATTERY);

        addAliases(List.of(
              EmiStack.of(MekanismBlocks.CREATIVE_BIN),
              EmiStack.of(MekanismBlocks.CREATIVE_FLUID_TANK),
              EmiStack.of(MekanismBlocks.CREATIVE_CHEMICAL_TANK),
              EmiStack.of(ItemBlockEnergyCube.withEnergyCubeSideConfig(MekanismBlocks.CREATIVE_ENERGY_CUBE, ItemBlockEnergyCube.ALL_INPUT))
        ), MekanismAliases.STORAGE_TRASHCAN, MekanismAliases.STORAGE_VOID);
    }

    private void addStorageBlockAliases() {
        addAliases(MekanismBlocks.BRONZE_BLOCK, MekanismAliases.BLOCK_BRONZE);
        addAliases(MekanismBlocks.CHARCOAL_BLOCK, MekanismAliases.BLOCK_CHARCOAL);
        addAliases(MekanismBlocks.STEEL_BLOCK, MekanismAliases.BLOCK_STEEL);
        addAliases(MekanismBlocks.FLUORITE_BLOCK, MekanismAliases.BLOCK_FLUORITE);
        //Dynamic storage blocks
        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            BlockRegistryObject<?, ?> block = entry.getValue();
            addAliases(block, () -> Util.makeDescriptionId("alias", block.getId().withPath(entry.getKey().getRegistrySuffix())));
        }
    }

    private void addQIOAliases() {
        addAliases(MekanismBlocks.QIO_REDSTONE_ADAPTER, MekanismAliases.QIO_FULL, MekanismAliases.QIO_ADAPTER_EMITTER);
        addAliases(MekanismBlocks.QIO_DRIVE_ARRAY, MekanismAliases.QIO_FULL, MekanismAliases.QIO_DRIVE_BAY);
        addAliases(MekanismItems.PORTABLE_QIO_DASHBOARD, MekanismAliases.PORTABLE_CRAFTING_TABLE, MekanismAliases.QIO_FULL, MekanismAliases.QIO_DASHBOARD_WIRELESS_TERMINAL);
        addAliases(List.of(
              MekanismBlocks.QIO_DASHBOARD,
              MekanismItems.PORTABLE_QIO_DASHBOARD
        ), MekanismAliases.QIO_FULL, MekanismAliases.QIO_DASHBOARD_TERMINAL, MekanismAliases.QIO_DASHBOARD_GRID);
        addAliases(List.of(
              MekanismItems.BASE_QIO_DRIVE,
              MekanismItems.HYPER_DENSE_QIO_DRIVE,
              MekanismItems.TIME_DILATING_QIO_DRIVE,
              MekanismItems.SUPERMASSIVE_QIO_DRIVE
        ), MekanismAliases.QIO_FULL, MekanismAliases.QIO_DRIVE_CELL, MekanismAliases.QIO_DRIVE_DISK, MekanismAliases.ITEM_STORAGE);
        addAliases(MekanismBlocks.QIO_EXPORTER, MekanismAliases.QIO_FULL, MekanismAliases.ROUND_ROBIN);
        addAliases(MekanismBlocks.QIO_IMPORTER, MekanismAliases.QIO_FULL);
    }

    private void addTransferAliases() {
        addAliases(List.of(
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

        addAliases(List.of(
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

        addAliases(List.of(
              MekanismBlocks.BASIC_MECHANICAL_PIPE,
              MekanismBlocks.ADVANCED_MECHANICAL_PIPE,
              MekanismBlocks.ELITE_MECHANICAL_PIPE,
              MekanismBlocks.ULTIMATE_MECHANICAL_PIPE
        ), MekanismAliases.FLUID_TRANSFER, MekanismAliases.TRANSMITTER, MekanismAliases.TRANSMITTER_CONDUIT, MekanismAliases.TRANSMITTER_TUBE);

        addAliases(List.of(
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
        addAliases(MekanismBlocks.DIVERSION_TRANSPORTER, MekanismAliases.REDSTONE_CONTROL);
        addAliases(MekanismBlocks.LOGISTICAL_SORTER, MekanismAliases.ITEM_TRANSFER, MekanismAliases.ROUND_ROBIN);

        addAliases(List.of(
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

        addAliases(MekanismBlocks.QUANTUM_ENTANGLOPORTER,
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

    private void addUpgradeAliases() {
        addAliases(MekanismItems.SPEED_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.UPGRADE_OVERCLOCK);
        addAliases(MekanismItems.ENERGY_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.ENERGY_STORAGE);
        addAliases(MekanismItems.FILTER_UPGRADE, MekanismAliases.UPGRADE_AUGMENT);
        addAliases(MekanismItems.MUFFLING_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.UPGRADE_MUFFLER);
        addAliases(MekanismItems.GAS_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.UPGRADE_CHEMICAL);
        addAliases(MekanismItems.ANCHOR_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.CHUNK_LOADER);
        addAliases(MekanismItems.STONE_GENERATOR_UPGRADE, MekanismAliases.UPGRADE_AUGMENT, MekanismAliases.UPGRADE_HOLE_FILLER);
        addAliases(List.of(
              MekanismItems.BASIC_TIER_INSTALLER,
              MekanismItems.ADVANCED_TIER_INSTALLER,
              MekanismItems.ELITE_TIER_INSTALLER,
              MekanismItems.ULTIMATE_TIER_INSTALLER
        ), MekanismAliases.INSTALLER_FACTORY, MekanismAliases.INSTALLER_UPGRADE);
    }

    private void addMiscAliases() {
        addAliases(MekanismItems.CRAFTING_FORMULA, MekanismAliases.CRAFTING_PATTERN);
        addAliases(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, MekanismAliases.AUTO_CRAFTER);
        addAliases(MekanismBlocks.DIMENSIONAL_STABILIZER, MekanismAliases.CHUNK_LOADER);
        addAliases(MekanismBlocks.FLUIDIC_PLENISHER, MekanismAliases.PLENISHER_PLACER, MekanismAliases.PLENISHER_REVERSE);
        addAliases(MekanismBlocks.OREDICTIONIFICATOR, MekanismAliases.TAG_CONVERTER);
        addAliases(MekanismBlocks.ROTARY_CONDENSENTRATOR,
              MekanismAliases.ROTARY_DECONDENSENTRATOR,
              MekanismAliases.ROTARY_CHEMICAL_TO_FLUID,
              MekanismAliases.ROTARY_GAS_TO_FLUID,
              MekanismAliases.ROTARY_FLUID_TO_CHEMICAL,
              MekanismAliases.ROTARY_FLUID_TO_GAS

        );
    }
}