package mekanism.chemistry.common.loot;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.common.loot.BaseLootProvider;
import net.minecraft.data.DataGenerator;

public class ChemistryLootProvider extends BaseLootProvider {

    public ChemistryLootProvider(DataGenerator gen) {
        super(gen, MekanismChemistry.MODID);
    }

    @Override
    protected ChemistryBlockLootTables getBlockLootTable() {
        return new ChemistryBlockLootTables();
    }
}
