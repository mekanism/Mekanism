package mekanism.chemistry.client;

import mekanism.chemistry.common.ChemistryLang;
import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.registries.ChemistryBlocks;
import mekanism.chemistry.common.registries.ChemistryFluids;
import mekanism.chemistry.common.registries.ChemistryGases;
import mekanism.chemistry.common.registries.ChemistryItems;
import mekanism.client.lang.BaseLanguageProvider;
import net.minecraft.data.DataGenerator;

public class ChemistryLangProvider extends BaseLanguageProvider {
    public ChemistryLangProvider(DataGenerator gen) {
        super(gen, MekanismChemistry.MODID);
    }

    @Override
    protected void addTranslations() {
        addItems();
        addBlocks();
        addFluids();
        addGases();
        addSubtitles();
        addMisc();
    }

    private void addItems() {
        add(ChemistryItems.AMMONIUM, "Ammonium");
        add(ChemistryItems.FERTILIZER, "Fertilizer");
    }

    private void addBlocks() {
        add(ChemistryBlocks.AIR_COMPRESSOR, "Air Compressor");
        add(ChemistryBlocks.FRACTIONATING_DISTILLER_CONTROLLER, "Fractionating Distiller Controller");
        add(ChemistryBlocks.FRACTIONATING_DISTILLER_VALVE, "Fractionating Distiller Valve");
        add(ChemistryBlocks.FRACTIONATING_DISTILLER_BLOCK, "Fractionating Distiller Block");
    }

    private void addFluids() {
        addFluid(ChemistryFluids.AMMONIA, "Liquid Ammonia");
        addFluid(ChemistryFluids.NITROGEN, "Liquid Nitrogen");
        addFluid(ChemistryFluids.AIR, "Liquid Air");
    }

    private void addGases() {
        add(ChemistryGases.AMMONIA, "Ammonia");
        add(ChemistryGases.NITROGEN, "Nitrogen");
        add(ChemistryGases.AIR, "Air");
    }

    private void addSubtitles() {
    }

    private void addMisc() {
        add(ChemistryLang.DISTILLER, "Fractionating Distiller");
        add(ChemistryLang.DESCRIPTION_FRACTIONATING_DISTILLER_BLOCK, "Pressure-resistant casing used to construct a fractionating distiller.");
        add(ChemistryLang.DESCRIPTION_FRACTIONATING_DISTILLER_VALVE, "Ports used by the fractionating distiller to consume or produce fluids.");
        add(ChemistryLang.DESCRIPTION_FRACTIONATING_DISTILLER_CONTROLLER, "The controller for a Fractionating Distiller, acting as the master block of the structure. Only one of these should be placed on a multiblock.");
        add(ChemistryLang.DESCRIPTION_AIR_COMPRESSOR, "An advanced, upgradeable air compressor.");
    }
}
