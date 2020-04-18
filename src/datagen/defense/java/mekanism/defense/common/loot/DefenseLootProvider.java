package mekanism.defense.common.loot;

import mekanism.common.loot.BaseLootProvider;
import mekanism.defense.common.MekanismDefense;
import net.minecraft.data.DataGenerator;

public class DefenseLootProvider extends BaseLootProvider {

    public DefenseLootProvider(DataGenerator gen) {
        super(gen, MekanismDefense.MODID);
    }

    @Override
    protected DefenseBlockLootTables getBlockLootTable() {
        return new DefenseBlockLootTables();
    }
}