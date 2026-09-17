package mekanism.additions.common.loot;

import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.common.loot.BaseBlockLootTables;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public class AdditionsBlockLootTables extends BaseBlockLootTables {

    public AdditionsBlockLootTables(LootTableSubProvider.Context context) {
        super(context);
    }

    @Override
    protected void generate() {
        //Obsidian TNT
        registerObsidianTNT();
        //Plastic slabs
        add(this::createSlabItemTable, AdditionsBlocks.PLASTIC_SLABS.asList());
        add(this::createSlabItemTable, AdditionsBlocks.PLASTIC_GLOW_SLABS.asList());
        add(this::createSlabItemTable, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS.asList());
        //Register all remaining blocks as just dropping themselves
        dropSelf(AdditionsBlocks.BLOCKS.getPrimaryEntries());
    }

    private void registerObsidianTNT() {
        add(AdditionsBlocks.OBSIDIAN_TNT, tnt -> LootTable.lootTable().withPool(applyExplosionCondition(tnt, LootPool.lootPool()
              .name("main")
              .setRolls(ContextIntProviders.exactly(1))
              .add(LootItem.lootTableItem(tnt)
                    .when(MatchBlock.blockMatches(this.blocks, tnt, StatePropertiesPredicate.Builder.properties().hasProperty(TntBlock.UNSTABLE, false)))
              )
        )));
    }
}