package mekanism.common.loot.table;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.attribute.Attributes.AttributeRedstone;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.util.EnumUtils;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ILootConditionConsumer;
import net.minecraft.loot.IRandomRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.ILootCondition.IBuilder;
import net.minecraft.loot.conditions.MatchTool;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.ApplyBonus;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.loot.functions.CopyNbt.Source;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.items.IItemHandler;

public abstract class BaseBlockLootTables extends BlockLootTables {

    private static final ILootCondition.IBuilder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item()
          .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.IntBound.atLeast(1))));

    private final Set<Block> knownBlocks = new ObjectOpenHashSet<>();
    private final Set<Block> toSkip = new ObjectOpenHashSet<>();

    @Override
    protected abstract void addTables();

    @Override
    protected void add(@Nonnull Block block, @Nonnull LootTable.Builder table) {
        //Overwrite the core register method to add to our list of known blocks
        super.add(block, table);
        knownBlocks.add(block);
    }

    @Nonnull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }

    protected void skip(IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            toSkip.add(blockProvider.getBlock());
        }
    }

    protected boolean skipBlock(Block block) {
        //Skip any blocks that we already registered a table for or have marked to skip
        return knownBlocks.contains(block) || toSkip.contains(block);
    }

    protected static LootTable.Builder droppingWithFortuneOrRandomly(Block block, IItemProvider item, IRandomRange range) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, ItemLootEntry.lootTableItem(item.asItem())
              .apply(SetCount.setCount(range))
              .apply(ApplyBonus.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
        ));
    }

    //IBlockProvider versions of BlockLootTable methods, modified to support varargs
    protected void dropSelf(List<IBlockProvider> blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            Block block = blockProvider.getBlock();
            if (!skipBlock(block)) {
                dropSelf(block);
            }
        }
    }

    protected void add(Function<Block, Builder> factory, Collection<? extends IBlockProvider> blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            add(blockProvider.getBlock(), factory);
        }
    }

    protected void add(Function<Block, Builder> factory, IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            add(blockProvider.getBlock(), factory);
        }
    }

    protected void dropSelfWithContents(List<IBlockProvider> blockProviders) {
        //TODO: See if there is other stuff we want to be transferring which we currently do not
        // For example, when writing this we added dump mode for chemical tanks to getting transferred to the item
        for (IBlockProvider blockProvider : blockProviders) {
            Block block = blockProvider.getBlock();
            if (skipBlock(block)) {
                continue;
            }
            CopyNbt.Builder nbtBuilder = CopyNbt.copyData(Source.BLOCK_ENTITY);
            boolean hasData = false;
            boolean hasContents = false;
            @Nullable
            TileEntity tile = null;
            if (block instanceof IHasTileEntity) {
                tile = ((IHasTileEntity<?>) block).getTileType().create();
            }
            if (tile instanceof IFrequencyHandler && ((IFrequencyHandler) tile).getFrequencyComponent().hasCustomFrequencies()) {
                nbtBuilder.copy(NBTConstants.COMPONENT_FREQUENCY, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_FREQUENCY);
                hasData = true;
            }
            if (Attribute.has(block, AttributeSecurity.class)) {
                //TODO: Should we just save the entire security component?
                nbtBuilder.copy(NBTConstants.COMPONENT_SECURITY + "." + NBTConstants.OWNER_UUID, NBTConstants.MEK_DATA + "." + NBTConstants.OWNER_UUID);
                nbtBuilder.copy(NBTConstants.COMPONENT_SECURITY + "." + NBTConstants.SECURITY_MODE, NBTConstants.MEK_DATA + "." + NBTConstants.SECURITY_MODE);
                hasData = true;
            }
            if (Attribute.has(block, AttributeUpgradeSupport.class)) {
                nbtBuilder.copy(NBTConstants.COMPONENT_UPGRADE, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_UPGRADE);
                hasData = true;
            }
            if (tile instanceof ISideConfiguration) {
                nbtBuilder.copy(NBTConstants.COMPONENT_CONFIG, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_CONFIG);
                nbtBuilder.copy(NBTConstants.COMPONENT_EJECTOR, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_EJECTOR);
                hasData = true;
            }
            if (tile instanceof ISustainedData) {
                Set<Entry<String, String>> remapEntries = ((ISustainedData) tile).getTileDataRemap().entrySet();
                for (Entry<String, String> remapEntry : remapEntries) {
                    nbtBuilder.copy(remapEntry.getKey(), NBTConstants.MEK_DATA + "." + remapEntry.getValue());
                }
                if (!remapEntries.isEmpty()) {
                    hasData = true;
                }
            }
            if (Attribute.has(block, AttributeRedstone.class)) {
                nbtBuilder.copy(NBTConstants.CONTROL_TYPE, NBTConstants.MEK_DATA + "." + NBTConstants.CONTROL_TYPE);
                hasData = true;
            }
            if (tile instanceof TileEntityMekanism) {
                TileEntityMekanism tileEntity = (TileEntityMekanism) tile;
                for (SubstanceType type : EnumUtils.SUBSTANCES) {
                    if (tileEntity.handles(type) && !type.getContainers(tileEntity).isEmpty()) {
                        nbtBuilder.copy(type.getContainerTag(), NBTConstants.MEK_DATA + "." + type.getContainerTag());
                        hasData = true;
                        if (type != SubstanceType.ENERGY && type != SubstanceType.HEAT) {
                            hasContents = true;
                        }
                    }
                }
            }
            if (Attribute.has(block, AttributeInventory.class)) {
                //If the block has an inventory, copy the inventory slots,
                // but if it is an IItemHandler, which for most cases of ours it will be,
                // then only copy the slots if we actually have any slots because otherwise maybe something just went wrong
                if (!(tile instanceof IItemHandler) || ((IItemHandler) tile).getSlots() > 0) {
                    //If we don't actually handle saving an inventory (such as the quantum entangloporter, don't actually add it as something to copy)
                    if (!(tile instanceof TileEntityMekanism) || ((TileEntityMekanism) tile).persistInventory()) {
                        nbtBuilder.copy(NBTConstants.ITEMS, NBTConstants.MEK_DATA + "." + NBTConstants.ITEMS);
                        hasData = true;
                        hasContents = true;
                    }
                }
            }
            if (block instanceof BlockCardboardBox) {
                //TODO: Do this better so that it doesn't have to be as hard coded to being a cardboard box
                nbtBuilder.copy(NBTConstants.DATA, NBTConstants.MEK_DATA + "." + NBTConstants.DATA);
                hasData = true;
            }
            if (!hasData) {
                //To keep the json as clean as possible don't bother even registering a blank accept function if we have no
                // persistent data that we want to copy. Also log a warning so that we don't have to attempt to check against
                // that block
                dropSelf(block);
            } else {
                add(block, LootTable.lootTable().withPool(applyExplosionCondition(hasContents, LootPool.lootPool()
                      .name("main")
                      .setRolls(ConstantRange.exactly(1))
                      .add(ItemLootEntry.lootTableItem(block).apply(nbtBuilder))
                )));
            }
        }
    }

    /**
     * Like vanilla's {@link BlockLootTables#applyExplosionCondition(IItemProvider, ILootConditionConsumer)} except with a boolean for if it is explosion resistant.
     */
    private static <T> T applyExplosionCondition(boolean explosionResistant, ILootConditionConsumer<T> condition) {
        return explosionResistant ? condition.unwrap() : condition.when(SurvivesExplosion.survivesExplosion());
    }

    /**
     * Like vanilla's {@link BlockLootTables#createSlabItemTable(Block)} except with a named pool
     */
    @Nonnull
    protected static LootTable.Builder createSlabItemTable(Block slab) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
              .name("main")
              .setRolls(ConstantRange.exactly(1))
              .add(applyExplosionDecay(slab, ItemLootEntry.lootTableItem(slab)
                          .apply(SetCount.setCount(ConstantRange.exactly(2))
                                .when(BlockStateProperty.hasBlockStateProperties(slab)
                                      .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)))
                          )
                    )
              )
        );
    }

    /**
     * Like vanilla's {@link BlockLootTables#dropOther(Block, IItemProvider)} except with a named pool
     */
    @Override
    public void dropOther(@Nonnull Block block, @Nonnull IItemProvider drop) {
        add(block, createSingleItemTable(drop));
    }

    /**
     * Like vanilla's {@link BlockLootTables#createSingleItemTable(IItemProvider)} except with a named pool
     */
    @Nonnull
    protected static LootTable.Builder createSingleItemTable(IItemProvider item) {
        return LootTable.lootTable().withPool(applyExplosionCondition(item, LootPool.lootPool()
              .name("main")
              .setRolls(ConstantRange.exactly(1))
              .add(ItemLootEntry.lootTableItem(item))
        ));
    }

    /**
     * Like vanilla's {@link BlockLootTables#createSingleItemTableWithSilkTouch(Block, IItemProvider, IRandomRange)} except with a named pool
     */
    @Nonnull
    protected static LootTable.Builder createSingleItemTableWithSilkTouch(@Nonnull Block block, @Nonnull IItemProvider item, @Nonnull IRandomRange range) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, ItemLootEntry.lootTableItem(item).apply(SetCount.setCount(range))));
    }

    /**
     * Like vanilla's {@link BlockLootTables#createSilkTouchDispatchTable(Block, LootEntry.Builder)} except with a named pool
     */
    @Nonnull
    protected static LootTable.Builder createSilkTouchDispatchTable(@Nonnull Block block, @Nonnull LootEntry.Builder<?> builder) {
        return createSelfDropDispatchTable(block, HAS_SILK_TOUCH, builder);
    }

    /**
     * Like vanilla's {@link BlockLootTables#createSelfDropDispatchTable(Block, IBuilder, LootEntry.Builder)} except with a named pool
     */
    @Nonnull
    protected static LootTable.Builder createSelfDropDispatchTable(@Nonnull Block block, @Nonnull ILootCondition.IBuilder conditionBuilder,
          @Nonnull LootEntry.Builder<?> entry) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
              .name("main")
              .setRolls(ConstantRange.exactly(1))
              .add(ItemLootEntry.lootTableItem(block)
                    .when(conditionBuilder)
                    .otherwise(entry)
              )
        );
    }
}