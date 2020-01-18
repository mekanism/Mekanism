package mekanism.generators.common.loot;

import mekanism.common.loot.BaseLootProvider;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.data.DataGenerator;

public class GeneratorsLootProvider extends BaseLootProvider {

    public GeneratorsLootProvider(DataGenerator gen) {
        super(gen, MekanismGenerators.MODID);
    }

    @Override
    protected GeneratorsBlockLootTables getBlockLootTable() {
        return new GeneratorsBlockLootTables();
    }
}