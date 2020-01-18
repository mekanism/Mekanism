package mekanism.common.loot;

import mekanism.common.Mekanism;
import mekanism.common.loot.table.BaseBlockLootTables;
import mekanism.common.loot.table.BaseEntityLootTables;
import mekanism.common.loot.table.MekanismBlockLootTables;
import mekanism.common.loot.table.MekanismEntityLootTables;
import net.minecraft.data.DataGenerator;

public class MekanismLootProvider extends BaseLootProvider {

    public MekanismLootProvider(DataGenerator gen) {
        super(gen, Mekanism.MODID);
    }

    @Override
    protected BaseBlockLootTables getBlockLootTable() {
        return new MekanismBlockLootTables();
    }

    @Override
    protected BaseEntityLootTables getEntityLootTable() {
        return new MekanismEntityLootTables();
    }
}