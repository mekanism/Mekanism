package mekanism.common.loot.table;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreType;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class MekanismBlockLootTables extends BaseBlockLootTables {

    public MekanismBlockLootTables(HolderLookup.Provider provider) {
        super(provider);
    }

    @Override
    protected void generate() {
        skip(MekanismBlocks.BOUNDING_BLOCK);
        add(block -> createSingleItemTableWithSilkTouch(block, MekanismItems.SALT, ConstantValue.exactly(4)), MekanismBlocks.SALT_BLOCK);
        add(block -> droppingWithFortuneOrRandomly(block, MekanismItems.FLUORITE_GEM, UniformGenerator.between(2, 4)), MekanismBlocks.ORES.get(OreType.FLUORITE));
        add(block -> createOreDrop(block, MekanismItems.PROCESSED_RESOURCES.get(ResourceType.RAW, PrimaryResource.OSMIUM)), MekanismBlocks.ORES.get(OreType.OSMIUM));
        add(block -> createOreDrop(block, MekanismItems.PROCESSED_RESOURCES.get(ResourceType.RAW, PrimaryResource.TIN)), MekanismBlocks.ORES.get(OreType.TIN));
        add(block -> createOreDrop(block, MekanismItems.PROCESSED_RESOURCES.get(ResourceType.RAW, PrimaryResource.LEAD)), MekanismBlocks.ORES.get(OreType.LEAD));
        add(block -> createOreDrop(block, MekanismItems.PROCESSED_RESOURCES.get(ResourceType.RAW, PrimaryResource.URANIUM)), MekanismBlocks.ORES.get(OreType.URANIUM));
        //Register the remaining blocks as dropping themselves with any contents they may have stored
        dropSelfWithContents(MekanismBlocks.BLOCKS.getPrimaryEntries());
    }
}