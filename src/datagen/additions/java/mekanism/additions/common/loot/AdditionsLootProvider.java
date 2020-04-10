package mekanism.additions.common.loot;

import mekanism.additions.common.MekanismAdditions;
import mekanism.common.loot.BaseLootProvider;
import net.minecraft.data.DataGenerator;

public class AdditionsLootProvider extends BaseLootProvider {

    public AdditionsLootProvider(DataGenerator gen) {
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