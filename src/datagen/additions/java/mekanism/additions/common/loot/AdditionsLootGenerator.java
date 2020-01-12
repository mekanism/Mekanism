package mekanism.additions.common.loot;

import mekanism.additions.common.MekanismAdditions;
import mekanism.common.loot.BaseLootGenerator;
import net.minecraft.data.DataGenerator;

public class AdditionsLootGenerator extends BaseLootGenerator {

    public AdditionsLootGenerator(DataGenerator gen) {
        super(gen, MekanismAdditions.MODID);
    }

    @Override
    protected AdditionsBlockLootTables getBlockLootTable() {
        return new AdditionsBlockLootTables();
    }

    @Override
    protected AdditionsEntityLootTables getEntityLootTable() {
        return new AdditionsEntityLootTables();
    }
}