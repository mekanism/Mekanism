package mekanism.generators.common.loot;

import mekanism.common.loot.BaseLootGenerator;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.data.DataGenerator;

public class GeneratorsLootGenerator extends BaseLootGenerator {

    public GeneratorsLootGenerator(DataGenerator gen) {
        super(gen, MekanismGenerators.MODID);
    }

    @Override
    protected GeneratorsBlockLootTables getBlockLootTable() {
        return new GeneratorsBlockLootTables();
    }
}