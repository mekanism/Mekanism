package mekanism.chemistry.client;

import mekanism.chemistry.common.ChemistryLang;
import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.registries.ChemistryBlocks;
import mekanism.chemistry.common.registries.ChemistryFluids;
import mekanism.chemistry.common.registries.ChemistryGases;
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
    }

    private void addBlocks() {
        add(ChemistryBlocks.AIR_COMPRESSOR, "Air Compressor");
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
        add(ChemistryLang.DESCRIPTION_AIR_COMPRESSOR, "An advanced, upgradeable air compressor.");
    }
}
