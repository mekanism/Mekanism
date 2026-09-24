package mekanism.client.integration.emi;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.client.recipe_viewer.recipe.SPSRecipeViewerRecipe;
import mekanism.common.Mekanism;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MekanismEmiDefaults extends BaseEmiDefaults {

    public MekanismEmiDefaults(PackOutput output, CompletableFuture<HolderLookup.Provider> reloadableLookupProvider) {
        super(output, reloadableLookupProvider, Mekanism.MODID);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider reloadableLookupProvider) {
        addMiscRecipes(reloadableLookupProvider);
        addGearModuleRecipes(reloadableLookupProvider);
        addLateGameRecipes(reloadableLookupProvider);
        addInfusingRecipes(reloadableLookupProvider);
        addCrushingRecipes(reloadableLookupProvider);
        addEnrichingRecipes(reloadableLookupProvider);
        addEvaporationRecipes(reloadableLookupProvider);
        addInductionRecipes(reloadableLookupProvider);
        addRotaryRecipes();
        addFactoryRecipes(reloadableLookupProvider);
        addTransmitterRecipes(reloadableLookupProvider);
        addStorageRecipes(reloadableLookupProvider);
        addTieredRecipes(reloadableLookupProvider, "bin/");
        addTieredRecipes(reloadableLookupProvider, "chemical_tank/");
        addTieredRecipes(reloadableLookupProvider, "energy_cube/");
        addTieredRecipes(reloadableLookupProvider, "fluid_tank/");
        addTieredRecipes(reloadableLookupProvider, "tier_installer/");
        reloadableLookupProvider.lookupOrThrow(MekanismRegistries.Keys.UPGRADES).listElementIds().forEach(id -> addRecipe(reloadableLookupProvider, id.identifier().withPrefix("upgrade/")));
        //Note: We intentionally skip basic circuits as they are considered a "base" material
        addRecipe(reloadableLookupProvider, "control_circuit/" + BaseTier.ADVANCED.getLowerName());
        addRecipe(reloadableLookupProvider, "control_circuit/" + BaseTier.ELITE.getLowerName());
        addRecipe(reloadableLookupProvider, "control_circuit/" + BaseTier.ULTIMATE.getLowerName());

        addRecipe(reloadableLookupProvider, "crystallizing/lithium");
        addRecipe(reloadableLookupProvider, "separator/brine");
        //Note: We intentionally don't add the water -> hydrogen and oxygen as they are "base" enough materials
        // that it is probably more beneficial to users to default by showing how much of that they need than how much water
        addRecipe(reloadableLookupProvider, "chemical_infusing/hydrogen_chloride");
        addRecipe(reloadableLookupProvider, "chemical_infusing/sulfur_trioxide");
        addRecipe(reloadableLookupProvider, "chemical_infusing/sulfuric_acid");
        addRecipe(reloadableLookupProvider, "reaction/substrate/water_hydrogen");
        addRecipe(reloadableLookupProvider, "reaction/substrate/ethene_oxygen");
        //Note: We intentionally don't cover the other gas conversions as there are better defaults for them
        addRecipe(reloadableLookupProvider, "chemical_conversion/osmium_from_ingot");

        //Custom pigments that only exist by mixing
        addPigmentMix(reloadableLookupProvider, EnumColor.DARK_AQUA, EnumColor.WHITE, EnumColor.AQUA);
        addPigmentMix(reloadableLookupProvider, EnumColor.BLACK, EnumColor.RED, EnumColor.DARK_RED);
    }

    private void addPigmentMix(HolderLookup.Provider reloadableLookupProvider, EnumColor leftInput, EnumColor rightInput, EnumColor output) {
        addRecipe(reloadableLookupProvider, "pigment_mixing/" + leftInput.getRegistryPrefix() + "_" + rightInput.getRegistryPrefix() + "_to_" + output.getRegistryPrefix());
    }

    private void addStorageRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String nuggetPath = "nuggets/";
        addRecipe(reloadableLookupProvider, nuggetPath + "bronze");
        addRecipe(reloadableLookupProvider, nuggetPath + "refined_glowstone");
        addRecipe(reloadableLookupProvider, nuggetPath + "refined_obsidian");
        addRecipe(reloadableLookupProvider, nuggetPath + "steel");

        String storagePath = "storage_blocks/";
        addStorageBlockRecipe(reloadableLookupProvider, storagePath, MekanismBlocks.BRONZE_BLOCK);
        addStorageBlockRecipe(reloadableLookupProvider, storagePath, MekanismBlocks.REFINED_GLOWSTONE_BLOCK);
        addStorageBlockRecipe(reloadableLookupProvider, storagePath, MekanismBlocks.REFINED_OBSIDIAN_BLOCK);
        addStorageBlockRecipe(reloadableLookupProvider, storagePath, MekanismBlocks.STEEL_BLOCK);
        addStorageBlockRecipe(reloadableLookupProvider, storagePath, MekanismBlocks.FLUORITE_BLOCK);
        addStorageBlockRecipe(reloadableLookupProvider, storagePath, MekanismBlocks.CHARCOAL_BLOCK);
        addRecipe(reloadableLookupProvider, storagePath + "bio_fuel");
        addRecipe(reloadableLookupProvider, storagePath + "salt");

    }

    private void addStorageBlockRecipe(HolderLookup.Provider reloadableLookupProvider, String basePath, DeferredHolder<Block, BlockResource> block) {
        addRecipe(reloadableLookupProvider, basePath + block.value().getResourceInfo().getRegistrySuffix());
    }

    private void addFactoryRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String basePath = "factory/";
        for (FactoryTier factoryTier : EnumUtils.FACTORY_TIERS) {
            String tieredPath = basePath + factoryTier.getBaseTier().getLowerName() + "/";
            for (FactoryType type : EnumUtils.FACTORY_TYPES) {
                addRecipe(reloadableLookupProvider, tieredPath + type.getSerializedName());
            }
        }
    }

    private void addTransmitterRecipes(HolderLookup.Provider reloadableLookupProvider) {
        addTieredRecipes(reloadableLookupProvider, "transmitter/logistical_transporter/");
        addTieredRecipes(reloadableLookupProvider, "transmitter/mechanical_pipe/");
        addTieredRecipes(reloadableLookupProvider, "transmitter/pressurized_tube/");
        addTieredRecipes(reloadableLookupProvider, "transmitter/thermodynamic_conductor/");
        addTieredRecipes(reloadableLookupProvider, "transmitter/universal_cable/");
        addRecipe(reloadableLookupProvider, "transmitter/diversion_transporter");
        addRecipe(reloadableLookupProvider, "transmitter/restrictive_transporter");
    }

    private void addInfusingRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String conversionPath = "chemical_conversion/";
        addRecipe(reloadableLookupProvider, conversionPath + "bio/from_bio_fuel");
        addRecipe(reloadableLookupProvider, conversionPath + "carbon/from_enriched");
        addRecipe(reloadableLookupProvider, conversionPath + "diamond/from_enriched");
        addRecipe(reloadableLookupProvider, conversionPath + "fungi/from_mushrooms");
        addRecipe(reloadableLookupProvider, conversionPath + "redstone/from_enriched");
        addRecipe(reloadableLookupProvider, conversionPath + "refined_obsidian/from_enriched");
        addRecipe(reloadableLookupProvider, conversionPath + "gold/from_enriched");
        addRecipe(reloadableLookupProvider, conversionPath + "tin/from_enriched");
    }

    private void addCrushingRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String basePath = "crushing/";
        addRecipe(reloadableLookupProvider, basePath + "pointed_dripstone_from_block");
        addRecipe(reloadableLookupProvider, basePath + "charcoal_dust");
        addRecipe(reloadableLookupProvider, basePath + "obsidian_to_dust");
    }

    private void addEnrichingRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String basePath = "enriching/";
        addRecipe(reloadableLookupProvider, basePath + "hdpe_sheet");
    }

    private void addEvaporationRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String basePath = "thermal_evaporation/";
        addRecipe(reloadableLookupProvider, basePath + "block");
        addRecipe(reloadableLookupProvider, basePath + "controller");
        addRecipe(reloadableLookupProvider, basePath + "valve");
        //Note: We intentionally don't bother converting brine to water for showing amounts by default
        addRecipe(reloadableLookupProvider, "evaporating/lithium");
    }

    private void addInductionRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String basePath = "induction/";
        addTieredRecipes(reloadableLookupProvider, basePath + "cell/");
        addTieredRecipes(reloadableLookupProvider, basePath + "provider/");
        addRecipe(reloadableLookupProvider, basePath + "casing");
        addRecipe(reloadableLookupProvider, basePath + "port");
    }

    private void addRotaryRecipes() {
        addRotaryRecipe(ChemicalIds.BRINE);
        addRotaryRecipe(ChemicalIds.CHLORINE);
        addRotaryRecipe(ChemicalIds.ETHENE);
        addRotaryRecipe(ChemicalIds.HYDROGEN);
        addRotaryRecipe(ChemicalIds.HYDROGEN_CHLORIDE);
        addRotaryRecipe(ChemicalIds.LITHIUM);
        addRotaryRecipe(ChemicalIds.OXYGEN);
        addRotaryRecipe(ChemicalIds.SODIUM);
        addRotaryRecipe(ChemicalIds.SUPERHEATED_SODIUM);
        addRotaryRecipe(ChemicalIds.STEAM);
        addRotaryRecipe(ChemicalIds.SULFUR_DIOXIDE);
        addRotaryRecipe(ChemicalIds.SULFUR_TRIOXIDE);
        addRotaryRecipe(ChemicalIds.SULFURIC_ACID);
        addRotaryRecipe(ChemicalIds.HYDROFLUORIC_ACID);
        addRotaryRecipe(ChemicalIds.URANIUM_OXIDE);
        addRotaryRecipe(ChemicalIds.URANIUM_HEXAFLUORIDE);
    }

    private void addMiscRecipes(HolderLookup.Provider reloadableLookupProvider) {
        addRecipe(reloadableLookupProvider, MekanismItems.CANTEEN);
        addRecipe(reloadableLookupProvider, MekanismItems.CONFIGURATION_CARD);
        addRecipe(reloadableLookupProvider, MekanismItems.CONFIGURATOR);
        addRecipe(reloadableLookupProvider, MekanismItems.CRAFTING_FORMULA);
        addRecipe(reloadableLookupProvider, MekanismItems.DICTIONARY);
        addRecipe(reloadableLookupProvider, MekanismItems.DOSIMETER);
        addRecipe(reloadableLookupProvider, MekanismItems.GEIGER_COUNTER);
        addRecipe(reloadableLookupProvider, MekanismItems.DYE_BASE);
        addRecipe(reloadableLookupProvider, MekanismItems.ELECTRIC_BOW);
        addRecipe(reloadableLookupProvider, MekanismItems.ELECTROLYTIC_CORE);
        addRecipe(reloadableLookupProvider, MekanismItems.ENERGY_TABLET);
        addRecipe(reloadableLookupProvider, MekanismItems.GAUGE_DROPPER);
        addRecipe(reloadableLookupProvider, MekanismItems.HDPE_ROD);
        addRecipe(reloadableLookupProvider, MekanismItems.HDPE_STICK);
        addRecipe(reloadableLookupProvider, MekanismItems.HDPE_REINFORCED_ELYTRA);
        addRecipe(reloadableLookupProvider, MekanismItems.NETWORK_READER);
        addRecipe(reloadableLookupProvider, MekanismItems.PORTABLE_TELEPORTER);
        addRecipe(reloadableLookupProvider, MekanismItems.ROBIT);
        addRecipe(reloadableLookupProvider, MekanismItems.SEISMIC_READER);
        addRecipe(reloadableLookupProvider, MekanismItems.TELEPORTATION_CORE);
        addRecipe(reloadableLookupProvider, MekanismItems.BASE_QIO_DRIVE);
        addRecipe(reloadableLookupProvider, MekanismItems.HYPER_DENSE_QIO_DRIVE);
        addRecipe(reloadableLookupProvider, MekanismItems.TIME_DILATING_QIO_DRIVE);
        addRecipe(reloadableLookupProvider, MekanismItems.SUPERMASSIVE_QIO_DRIVE);
        addRecipe(reloadableLookupProvider, MekanismItems.PORTABLE_QIO_DASHBOARD);
        addRecipe(reloadableLookupProvider, MekanismItems.ATOMIC_DISASSEMBLER);
        addRecipe(reloadableLookupProvider, MekanismItems.FLAMETHROWER);
        addRecipe(reloadableLookupProvider, MekanismItems.FREE_RUNNERS);
        addRecipe(reloadableLookupProvider, MekanismItems.ARMORED_FREE_RUNNERS);
        addRecipe(reloadableLookupProvider, MekanismItems.SCUBA_MASK);
        addRecipe(reloadableLookupProvider, MekanismItems.SCUBA_TANK);
        addRecipe(reloadableLookupProvider, MekanismItems.JETPACK);
        addRecipe(reloadableLookupProvider, MekanismItems.ARMORED_JETPACK);
        addRecipe(reloadableLookupProvider, MekanismItems.HAZMAT_MASK);
        addRecipe(reloadableLookupProvider, MekanismItems.HAZMAT_GOWN);
        addRecipe(reloadableLookupProvider, MekanismItems.HAZMAT_PANTS);
        addRecipe(reloadableLookupProvider, MekanismItems.HAZMAT_BOOTS);
        addRecipe(reloadableLookupProvider, MekanismItems.MEKA_TOOL);
        addRecipe(reloadableLookupProvider, MekanismItems.MEKASUIT_HELMET);
        addRecipe(reloadableLookupProvider, MekanismItems.MEKASUIT_BODYARMOR);
        addRecipe(reloadableLookupProvider, MekanismItems.MEKASUIT_PANTS);
        addRecipe(reloadableLookupProvider, MekanismItems.MEKASUIT_BOOTS);

        addRecipe(reloadableLookupProvider, MekanismBlocks.BOILER_CASING);
        addRecipe(reloadableLookupProvider, MekanismBlocks.BOILER_VALVE);
        addRecipe(reloadableLookupProvider, MekanismBlocks.CARDBOARD_BOX);
        addRecipe(reloadableLookupProvider, MekanismBlocks.CHARGEPAD);
        addRecipe(reloadableLookupProvider, MekanismBlocks.CHEMICAL_CRYSTALLIZER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.CHEMICAL_INFUSER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.CHEMICAL_OXIDIZER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.CHEMICAL_WASHER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.COMBINER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.CRUSHER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.DIGITAL_MINER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.DYNAMIC_TANK);
        addRecipe(reloadableLookupProvider, MekanismBlocks.DYNAMIC_VALVE);
        addRecipe(reloadableLookupProvider, MekanismBlocks.ELECTRIC_PUMP);
        addRecipe(reloadableLookupProvider, MekanismBlocks.ELECTROLYTIC_SEPARATOR);
        addRecipe(reloadableLookupProvider, MekanismBlocks.ENERGIZED_SMELTER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.ENRICHMENT_CHAMBER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.FLUIDIC_PLENISHER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.FORMULAIC_ASSEMBLICATOR);
        addRecipe(reloadableLookupProvider, MekanismBlocks.FUELWOOD_HEATER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.INDUSTRIAL_ALARM);
        addRecipe(reloadableLookupProvider, MekanismBlocks.ISOTOPIC_CENTRIFUGE);
        addRecipe(reloadableLookupProvider, MekanismBlocks.LASER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.LASER_AMPLIFIER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.LASER_TRACTOR_BEAM);
        addRecipe(reloadableLookupProvider, MekanismBlocks.LOGISTICAL_SORTER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.METALLURGIC_INFUSER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.OREDICTIONIFICATOR);
        addRecipe(reloadableLookupProvider, MekanismBlocks.OSMIUM_COMPRESSOR);
        addRecipe(reloadableLookupProvider, MekanismBlocks.PERSONAL_BARREL);
        addRecipe(reloadableLookupProvider, MekanismBlocks.PERSONAL_CHEST);
        addRecipe(reloadableLookupProvider, MekanismBlocks.PRECISION_SAWMILL);
        addRecipe(reloadableLookupProvider, MekanismBlocks.PRESSURE_DISPERSER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.PURIFICATION_CHAMBER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.QUANTUM_ENTANGLOPORTER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.RESISTIVE_HEATER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.ROTARY_CONDENSENTRATOR);
        addRecipe(reloadableLookupProvider, MekanismBlocks.SECURITY_DESK);
        addRecipe(reloadableLookupProvider, MekanismBlocks.SEISMIC_VIBRATOR);
        addRecipe(reloadableLookupProvider, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
        addRecipe(reloadableLookupProvider, MekanismBlocks.STEEL_CASING);
        addRecipe(reloadableLookupProvider, MekanismBlocks.STRUCTURAL_GLASS);
        addRecipe(reloadableLookupProvider, MekanismBlocks.SUPERHEATING_ELEMENT);
        addRecipe(reloadableLookupProvider, MekanismBlocks.TELEPORTER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.TELEPORTER_FRAME);
        addRecipe(reloadableLookupProvider, MekanismBlocks.QIO_DRIVE_ARRAY);
        addRecipe(reloadableLookupProvider, MekanismBlocks.QIO_REDSTONE_ADAPTER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.QIO_EXPORTER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.QIO_IMPORTER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.QIO_DASHBOARD);
        addRecipe(reloadableLookupProvider, MekanismBlocks.SPS_CASING);
        addRecipe(reloadableLookupProvider, MekanismBlocks.SPS_PORT);
        addRecipe(reloadableLookupProvider, MekanismBlocks.SUPERCHARGED_COIL);
        addRecipe(reloadableLookupProvider, MekanismBlocks.NUTRITIONAL_LIQUIFIER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.PIGMENT_EXTRACTOR);
        addRecipe(reloadableLookupProvider, MekanismBlocks.PIGMENT_MIXER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.PAINTING_MACHINE);
        addRecipe(reloadableLookupProvider, MekanismBlocks.MODIFICATION_STATION);
        addRecipe(reloadableLookupProvider, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER);
        addRecipe(reloadableLookupProvider, MekanismBlocks.RADIOACTIVE_WASTE_BARREL);
        addRecipe(reloadableLookupProvider, MekanismBlocks.DIMENSIONAL_STABILIZER);
    }

    private void addGearModuleRecipes(HolderLookup.Provider reloadableLookupProvider) {
        addRecipe(reloadableLookupProvider, MekanismItems.MODULE_BASE);
        for (DeferredHolder<ModuleData<?>, ? extends ModuleData<?>> module : MekanismModules.MODULES.getEntries()) {
            addRecipe(reloadableLookupProvider, module);
        }
    }

    private void addLateGameRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String basePath = "processing/lategame/";
        addRecipe(reloadableLookupProvider, basePath + "plutonium");
        addRecipe(reloadableLookupProvider, basePath + "polonium");
        addRecipe(reloadableLookupProvider, basePath + "plutonium_pellet/from_reaction");
        addRecipe(reloadableLookupProvider, basePath + "polonium_pellet/from_reaction");
        addRecipe(reloadableLookupProvider, basePath + "antimatter_pellet/from_gas");
        for (SPSRecipeViewerRecipe recipe : SPSRecipeViewerRecipe.getSPSRecipes(reloadableLookupProvider)) {
            addUncheckedRecipe(recipe.id());
        }
    }
}