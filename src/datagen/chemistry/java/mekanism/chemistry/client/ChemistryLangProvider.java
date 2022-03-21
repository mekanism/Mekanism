package mekanism.chemistry.client;

import mekanism.chemistry.common.MekanismChemistry;
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
    }

    private void addFluids() {
        addFluid(ChemistryFluids.AMMONIA, "Liquid Ammonia");
    }

    private void addGases() {
        add(ChemistryGases.AMMONIA, "Ammonia");
    }

    private void addSubtitles() {
    }

    private void addMisc() {
    }
}
