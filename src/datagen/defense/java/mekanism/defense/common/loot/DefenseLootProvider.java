package mekanism.defense.common.loot;

import java.util.List;
import mekanism.common.loot.BaseLootProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class DefenseLootProvider extends BaseLootProvider {

    public DefenseLootProvider(PackOutput output) {
        super(output, List.of(
              new SubProviderEntry(DefenseBlockLootTables::new, LootContextParamSets.BLOCK)
        ));
    }
}