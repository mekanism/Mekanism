package mekanism.additions.common.loot;

import mekanism.common.loot.BaseLootGenerator;
import net.minecraft.data.DataGenerator;

public class AdditionsLootGenerator extends BaseLootGenerator {

    public AdditionsLootGenerator(DataGenerator gen) {
        super(gen);
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