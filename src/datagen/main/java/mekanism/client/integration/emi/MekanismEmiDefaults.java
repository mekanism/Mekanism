package mekanism.client.integration.emi;

import java.util.concurrent.CompletableFuture;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.client.recipe_viewer.recipe.SPSRecipeViewerRecipe;
import mekanism.common.Mekanism;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.item.ItemModule;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class MekanismEmiDefaults extends BaseEmiDefaults {

    public MekanismEmiDefaults(PackOutput output, ExistingFileHelper existingFileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, existingFileHelper, registries, Mekanism.MODID);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider lookupProvider) {
        addMiscRecipes();
        addGearModuleRecipes();
        addLateGameRecipes();
        addInfusingRecipes();
        addCrushingRecipes();
        addEnrichingRecipes();
        addEvaporationRecipes();
        addInductionRecipes();
        addRotaryRecipes();
        addFactoryRecipes();
        addTransmitterRecipes();
        addStorageRecipes();
        addTieredRecipes("bin/");
        addTieredRecipes("chemical_tank/");
        addTieredRecipes("energy_cube/");
        addTieredRecipes("fluid_tank/");
        addTieredRecipes("tier_installer/");
        for (Upgrade upgrade : EnumUtils.UPGRADES) {
            addRecipe("upgrade/" + upgrade.getSerializedName());
        }
        //Note: We intentionally skip basic circuits as they are considered a "base" material
        addRecipe("control_circuit/" + BaseTier.ADVANCED.getLowerName());
        addRecipe("control_circuit/" + BaseTier.ELITE.getLowerName());
        addRecipe("control_circuit/" + BaseTier.ULTIMATE.getLowerName());

        addRecipe("crystallizing/lithium");
        addRecipe("separator/brine");
        //Note: We intentionally don't add the water -> hydrogen and oxygen as they are "base" enough materials
        // that it is probably more beneficial to users to default by showing how much of that they need than how much water
        addRecipe("chemical_infusing/hydrogen_chloride");
        addRecipe("chemical_infusing/sulfur_trioxide");
        addRecipe("chemical_infusing/sulfuric_acid");
        addRecipe("reaction/substrate/water_hydrogen");
        addRecipe("reaction/substrate/ethene_oxygen");
        //Note: We intentionally don't cover the other gas conversions as there are better defaults for them
        addRecipe("chemical_conversion/osmium_from_ingot");

        //Custom pigments that only exist by mixing
        addPigmentMix(EnumColor.DARK_AQUA, EnumColor.WHITE, EnumColor.AQUA);
        addPigmentMix(EnumColor.BLACK, EnumColor.RED, EnumColor.DARK_RED);
    }

    private void addPigmentMix(EnumColor leftInput, EnumColor rightInput, EnumColor output) {
        addRecipe("pigment_mixing/" + leftInput.getRegistryPrefix() + "_" + rightInput.getRegistryPrefix() + "_to_" + output.getRegistryPrefix());
    }

    private void addStorageRecipes() {
        String nuggetPath = "nuggets/";
        addRecipe(nuggetPath + "bronze");
        addRecipe(nuggetPath + "refined_glowstone");
        addRecipe(nuggetPath + "refined_obsidian");
        addRecipe(nuggetPath + "steel");

        String storagePath = "storage_blocks/";
        addStorageBlockRecipe(storagePath, MekanismBlocks.BRONZE_BLOCK);
        addStorageBlockRecipe(storagePath, MekanismBlocks.REFINED_GLOWSTONE_BLOCK);
        addStorageBlockRecipe(storagePath, MekanismBlocks.REFINED_OBSIDIAN_BLOCK);
        addStorageBlockRecipe(storagePath, MekanismBlocks.STEEL_BLOCK);
        addStorageBlockRecipe(storagePath, MekanismBlocks.FLUORITE_BLOCK);
        addStorageBlockRecipe(storagePath, MekanismBlocks.CHARCOAL_BLOCK);
        addRecipe(storagePath + "bio_fuel");
        addRecipe(storagePath + "salt");

    }

    private void addStorageBlockRecipe(String basePath, DeferredHolder<Block, BlockResource> block) {
        addRecipe(basePath + block.value().getResourceInfo().getRegistrySuffix());
    }

    private void addFactoryRecipes() {
        String basePath = "factory/";
        for (FactoryTier factoryTier : EnumUtils.FACTORY_TIERS) {
            String tieredPath = basePath + factoryTier.getBaseTier().getLowerName() + "/";
            for (FactoryType type : EnumUtils.FACTORY_TYPES) {
                addRecipe(tieredPath + type.getRegistryNameComponent());
            }
        }
    }

    private void addTransmitterRecipes() {
        addTieredRecipes("transmitter/logistical_transporter/");
        addTieredRecipes("transmitter/mechanical_pipe/");
        addTieredRecipes("transmitter/pressurized_tube/");
        addTieredRecipes("transmitter/thermodynamic_conductor/");
        addTieredRecipes("transmitter/universal_cable/");
        addRecipe("transmitter/diversion_transporter");
        addRecipe("transmitter/restrictive_transporter");
    }

    private void addInfusingRecipes() {
        String conversionPath = "chemical_conversion/";
        addRecipe(conversionPath + "bio/from_bio_fuel");
        addRecipe(conversionPath + "carbon/from_enriched");
        addRecipe(conversionPath + "diamond/from_enriched");
        addRecipe(conversionPath + "fungi/from_mushrooms");
        addRecipe(conversionPath + "redstone/from_enriched");
        addRecipe(conversionPath + "refined_obsidian/from_enriched");
        addRecipe(conversionPath + "gold/from_enriched");
        addRecipe(conversionPath + "tin/from_enriched");
    }

    private void addCrushingRecipes() {
        String basePath = "crushing/";
        addRecipe(basePath + "pointed_dripstone_from_block");
        addRecipe(basePath + "charcoal_dust");
        addRecipe(basePath + "obsidian_to_dust");
    }

    private void addEnrichingRecipes() {
        String basePath = "enriching/";
        addRecipe(basePath + "hdpe_sheet");
    }

    private void addEvaporationRecipes() {
        String basePath = "thermal_evaporation/";
        addRecipe(basePath + "block");
        addRecipe(basePath + "controller");
        addRecipe(basePath + "valve");
        //Note: We intentionally don't bother converting brine to water for showing amounts by default
        addRecipe("evaporating/lithium");
    }

    private void addInductionRecipes() {
        String basePath = "induction/";
        addTieredRecipes(basePath + "cell/");
        addTieredRecipes(basePath + "provider/");
        addRecipe(basePath + "casing");
        addRecipe(basePath + "port");
    }

    private void addRotaryRecipes() {
        addRotaryRecipe(MekanismChemicals.BRINE);
        addRotaryRecipe(MekanismChemicals.CHLORINE);
        addRotaryRecipe(MekanismChemicals.ETHENE);
        addRotaryRecipe(MekanismChemicals.HYDROGEN);
        addRotaryRecipe(MekanismChemicals.HYDROGEN_CHLORIDE);
        addRotaryRecipe(MekanismChemicals.LITHIUM);
        addRotaryRecipe(MekanismChemicals.OXYGEN);
        addRotaryRecipe(MekanismChemicals.SODIUM);
        addRotaryRecipe(MekanismChemicals.SUPERHEATED_SODIUM);
        addRotaryRecipe(MekanismChemicals.STEAM);
        addRotaryRecipe(MekanismChemicals.SULFUR_DIOXIDE);
        addRotaryRecipe(MekanismChemicals.SULFUR_TRIOXIDE);
        addRotaryRecipe(MekanismChemicals.SULFURIC_ACID);
        addRotaryRecipe(MekanismChemicals.HYDROFLUORIC_ACID);
        addRotaryRecipe(MekanismChemicals.URANIUM_OXIDE);
        addRotaryRecipe(MekanismChemicals.URANIUM_HEXAFLUORIDE);
    }

    private void addMiscRecipes() {
        addRecipe(MekanismItems.CANTEEN);
        addRecipe(MekanismItems.CONFIGURATION_CARD);
        addRecipe(MekanismItems.CONFIGURATOR);
        addRecipe(MekanismItems.CRAFTING_FORMULA);
        addRecipe(MekanismItems.DICTIONARY);
        addRecipe(MekanismItems.DOSIMETER);
        addRecipe(MekanismItems.GEIGER_COUNTER);
        addRecipe(MekanismItems.DYE_BASE);
        addRecipe(MekanismItems.ELECTRIC_BOW);
        addRecipe(MekanismItems.ELECTROLYTIC_CORE);
        addRecipe(MekanismItems.ENERGY_TABLET);
        addRecipe(MekanismItems.GAUGE_DROPPER);
        addRecipe(MekanismItems.HDPE_ROD);
        addRecipe(MekanismItems.HDPE_STICK);
        addRecipe(MekanismItems.HDPE_REINFORCED_ELYTRA);
        addRecipe(MekanismItems.NETWORK_READER);
        addRecipe(MekanismItems.PORTABLE_TELEPORTER);
        addRecipe(MekanismItems.ROBIT);
        addRecipe(MekanismItems.SEISMIC_READER);
        addRecipe(MekanismItems.TELEPORTATION_CORE);
        addRecipe(MekanismItems.BASE_QIO_DRIVE);
        addRecipe(MekanismItems.HYPER_DENSE_QIO_DRIVE);
        addRecipe(MekanismItems.TIME_DILATING_QIO_DRIVE);
        addRecipe(MekanismItems.SUPERMASSIVE_QIO_DRIVE);
        addRecipe(MekanismItems.PORTABLE_QIO_DASHBOARD);
        addRecipe(MekanismItems.ATOMIC_DISASSEMBLER);
        addRecipe(MekanismItems.FLAMETHROWER);
        addRecipe(MekanismItems.FREE_RUNNERS);
        addRecipe(MekanismItems.ARMORED_FREE_RUNNERS);
        addRecipe(MekanismItems.SCUBA_MASK);
        addRecipe(MekanismItems.SCUBA_TANK);
        addRecipe(MekanismItems.JETPACK);
        addRecipe(MekanismItems.ARMORED_JETPACK);
        addRecipe(MekanismItems.HAZMAT_MASK);
        addRecipe(MekanismItems.HAZMAT_GOWN);
        addRecipe(MekanismItems.HAZMAT_PANTS);
        addRecipe(MekanismItems.HAZMAT_BOOTS);
        addRecipe(MekanismItems.MEKA_TOOL);
        addRecipe(MekanismItems.MEKASUIT_HELMET);
        addRecipe(MekanismItems.MEKASUIT_BODYARMOR);
        addRecipe(MekanismItems.MEKASUIT_PANTS);
        addRecipe(MekanismItems.MEKASUIT_BOOTS);

        addRecipe(MekanismBlocks.BOILER_CASING);
        addRecipe(MekanismBlocks.BOILER_VALVE);
        addRecipe(MekanismBlocks.CARDBOARD_BOX);
        addRecipe(MekanismBlocks.CHARGEPAD);
        addRecipe(MekanismBlocks.CHEMICAL_CRYSTALLIZER);
        addRecipe(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER);
        addRecipe(MekanismBlocks.CHEMICAL_INFUSER);
        addRecipe(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER);
        addRecipe(MekanismBlocks.CHEMICAL_OXIDIZER);
        addRecipe(MekanismBlocks.CHEMICAL_WASHER);
        addRecipe(MekanismBlocks.COMBINER);
        addRecipe(MekanismBlocks.CRUSHER);
        addRecipe(MekanismBlocks.DIGITAL_MINER);
        addRecipe(MekanismBlocks.DYNAMIC_TANK);
        addRecipe(MekanismBlocks.DYNAMIC_VALVE);
        addRecipe(MekanismBlocks.ELECTRIC_PUMP);
        addRecipe(MekanismBlocks.ELECTROLYTIC_SEPARATOR);
        addRecipe(MekanismBlocks.ENERGIZED_SMELTER);
        addRecipe(MekanismBlocks.ENRICHMENT_CHAMBER);
        addRecipe(MekanismBlocks.FLUIDIC_PLENISHER);
        addRecipe(MekanismBlocks.FORMULAIC_ASSEMBLICATOR);
        addRecipe(MekanismBlocks.FUELWOOD_HEATER);
        addRecipe(MekanismBlocks.INDUSTRIAL_ALARM);
        addRecipe(MekanismBlocks.ISOTOPIC_CENTRIFUGE);
        addRecipe(MekanismBlocks.LASER);
        addRecipe(MekanismBlocks.LASER_AMPLIFIER);
        addRecipe(MekanismBlocks.LASER_TRACTOR_BEAM);
        addRecipe(MekanismBlocks.LOGISTICAL_SORTER);
        addRecipe(MekanismBlocks.METALLURGIC_INFUSER);
        addRecipe(MekanismBlocks.OREDICTIONIFICATOR);
        addRecipe(MekanismBlocks.OSMIUM_COMPRESSOR);
        addRecipe(MekanismBlocks.PERSONAL_BARREL);
        addRecipe(MekanismBlocks.PERSONAL_CHEST);
        addRecipe(MekanismBlocks.PRECISION_SAWMILL);
        addRecipe(MekanismBlocks.PRESSURE_DISPERSER);
        addRecipe(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER);
        addRecipe(MekanismBlocks.PURIFICATION_CHAMBER);
        addRecipe(MekanismBlocks.QUANTUM_ENTANGLOPORTER);
        addRecipe(MekanismBlocks.RESISTIVE_HEATER);
        addRecipe(MekanismBlocks.ROTARY_CONDENSENTRATOR);
        addRecipe(MekanismBlocks.SECURITY_DESK);
        addRecipe(MekanismBlocks.SEISMIC_VIBRATOR);
        addRecipe(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
        addRecipe(MekanismBlocks.STEEL_CASING);
        addRecipe(MekanismBlocks.STRUCTURAL_GLASS);
        addRecipe(MekanismBlocks.SUPERHEATING_ELEMENT);
        addRecipe(MekanismBlocks.TELEPORTER);
        addRecipe(MekanismBlocks.TELEPORTER_FRAME);
        addRecipe(MekanismBlocks.QIO_DRIVE_ARRAY);
        addRecipe(MekanismBlocks.QIO_REDSTONE_ADAPTER);
        addRecipe(MekanismBlocks.QIO_EXPORTER);
        addRecipe(MekanismBlocks.QIO_IMPORTER);
        addRecipe(MekanismBlocks.QIO_DASHBOARD);
        addRecipe(MekanismBlocks.SPS_CASING);
        addRecipe(MekanismBlocks.SPS_PORT);
        addRecipe(MekanismBlocks.SUPERCHARGED_COIL);
        addRecipe(MekanismBlocks.NUTRITIONAL_LIQUIFIER);
        addRecipe(MekanismBlocks.PIGMENT_EXTRACTOR);
        addRecipe(MekanismBlocks.PIGMENT_MIXER);
        addRecipe(MekanismBlocks.PAINTING_MACHINE);
        addRecipe(MekanismBlocks.MODIFICATION_STATION);
        addRecipe(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER);
        addRecipe(MekanismBlocks.RADIOACTIVE_WASTE_BARREL);
        addRecipe(MekanismBlocks.DIMENSIONAL_STABILIZER);
    }

    private void addGearModuleRecipes() {
        addRecipe(MekanismItems.MODULE_BASE);
        for (DeferredHolder<Item, ?> entry : MekanismItems.ITEMS.getEntries()) {
            if (entry.value() instanceof ItemModule) {
                addRecipe(entry);
            }
        }
    }

    private void addLateGameRecipes() {
        String basePath = "processing/lategame/";
        addRecipe(basePath + "plutonium");
        addRecipe(basePath + "polonium");
        addRecipe(basePath + "plutonium_pellet/from_reaction");
        addRecipe(basePath + "polonium_pellet/from_reaction");
        addRecipe(basePath + "antimatter_pellet/from_gas");
        for (SPSRecipeViewerRecipe recipe : SPSRecipeViewerRecipe.getSPSRecipes()) {
            addUncheckedRecipe(recipe.id());
        }
    }
}