package mekanism.common.loot.table;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
import net.minecraft.loot.IRandomRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.MatchTool;
import net.minecraft.loot.functions.ApplyBonus;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.loot.functions.CopyNbt.Source;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.items.IItemHandler;

public abstract class BaseBlockLootTables extends BlockLootTables {

    //Copy of BlockLootTables#SILK_TOUCH
    private static final ILootCondition.IBuilder SILK_TOUCH = MatchTool.builder(ItemPredicate.Builder.create()
          .enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.IntBound.atLeast(1))));

    private final Set<Block> knownBlocks = new ObjectOpenHashSet<>();
    private final Set<Block> toSkip = new ObjectOpenHashSet<>();

    @Override
    protected abstract void addTables();

    @Override
    protected void registerLootTable(@Nonnull Block block, @Nonnull LootTable.Builder table) {
        //Overwrite the core register method to add to our list of known blocks
        super.registerLootTable(block, table);
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
        return droppingWithSilkTouch(block, withExplosionDecay(block, ItemLootEntry.builder(item.asItem())
              .acceptFunction(SetCount.builder(range))
              .acceptFunction(ApplyBonus.oreDrops(Enchantments.FORTUNE))
        ));
    }

    //IBlockProvider versions of BlockLootTable methods, modified to support varargs
    protected void registerDropSelfLootTable(List<IBlockProvider> blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            Block block = blockProvider.getBlock();
            if (!skipBlock(block)) {
                registerDropSelfLootTable(block);
            }
        }
    }

    protected void registerLootTable(Function<Block, Builder> factory, IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            registerLootTable(blockProvider.getBlock(), factory);
        }
    }

    protected void registerDropSelfWithContentsLootTable(List<IBlockProvider> blockProviders) {
        //TODO: See if there is other stuff we want to be transferring which we currently do not
        // For example, when writing this we added dump mode for chemical tanks to getting transferred to the item
        for (IBlockProvider blockProvider : blockProviders) {
            Block block = blockProvider.getBlock();
            if (skipBlock(block)) {
                continue;
            }
            CopyNbt.Builder nbtBuilder = CopyNbt.builder(Source.BLOCK_ENTITY);
            boolean hasData = false;
            @Nullable
            TileEntity tile = null;
            if (block instanceof IHasTileEntity) {
                tile = ((IHasTileEntity<?>) block).getTileType().create();
            }
            if (Attribute.has(block, AttributeSecurity.class)) {
                //TODO: Should we just save the entire security component?
                nbtBuilder.replaceOperation(NBTConstants.COMPONENT_SECURITY + "." + NBTConstants.OWNER_UUID, NBTConstants.MEK_DATA + "." + NBTConstants.OWNER_UUID);
                nbtBuilder.replaceOperation(NBTConstants.COMPONENT_SECURITY + "." + NBTConstants.SECURITY_MODE, NBTConstants.MEK_DATA + "." + NBTConstants.SECURITY_MODE);
                hasData = true;
            }
            if (Attribute.has(block, AttributeUpgradeSupport.class)) {
                nbtBuilder.replaceOperation(NBTConstants.COMPONENT_UPGRADE, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_UPGRADE);
                hasData = true;
            }
            if (tile instanceof ISideConfiguration) {
                nbtBuilder.replaceOperation(NBTConstants.COMPONENT_CONFIG, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_CONFIG);
                nbtBuilder.replaceOperation(NBTConstants.COMPONENT_EJECTOR, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_EJECTOR);
                hasData = true;
            }
            if (tile instanceof ISustainedData) {
                Set<Entry<String, String>> remapEntries = ((ISustainedData) tile).getTileDataRemap().entrySet();
                for (Entry<String, String> remapEntry : remapEntries) {
                    nbtBuilder.replaceOperation(remapEntry.getKey(), NBTConstants.MEK_DATA + "." + remapEntry.getValue());
                }
                if (!remapEntries.isEmpty()) {
                    hasData = true;
                }
            }
            if (Attribute.has(block, AttributeRedstone.class)) {
                nbtBuilder.replaceOperation(NBTConstants.CONTROL_TYPE, NBTConstants.MEK_DATA + "." + NBTConstants.CONTROL_TYPE);
                hasData = true;
            }
            if (tile instanceof TileEntityMekanism) {
                TileEntityMekanism tileEntity = (TileEntityMekanism) tile;
                for (SubstanceType type : EnumUtils.SUBSTANCES) {
                    if (tileEntity.handles(type) && !type.getContainers(tileEntity).isEmpty()) {
                        nbtBuilder.replaceOperation(type.getContainerTag(), NBTConstants.MEK_DATA + "." + type.getContainerTag());
                        hasData = true;
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
                        nbtBuilder.replaceOperation(NBTConstants.ITEMS, NBTConstants.MEK_DATA + "." + NBTConstants.ITEMS);
                        hasData = true;
                    }
                }
            }
            if (block instanceof BlockCardboardBox) {
                //TODO: Do this better so that it doesn't have to be as hard coded to being a cardboard box
                nbtBuilder.replaceOperation(NBTConstants.DATA, NBTConstants.MEK_DATA + "." + NBTConstants.DATA);
                hasData = true;
            }
            if (!hasData) {
                //To keep the json as clean as possible don't bother even registering a blank accept function if we have no
                // persistent data that we want to copy. Also log a warning so that we don't have to attempt to check against
                // that block
                registerDropSelfLootTable(block);
            } else {
                registerLootTable(block, LootTable.builder().addLootPool(withSurvivesExplosion(block, LootPool.builder()
                      .name("main")
                      .rolls(ConstantRange.of(1))
                      .addEntry(ItemLootEntry.builder(block).acceptFunction(nbtBuilder))
                )));
            }
        }
    }

    /**
     * Like vanilla's droppingSlab except with a named pool
     */
    protected static LootTable.Builder droppingSlab(Block slab) {
        return LootTable.builder().addLootPool(LootPool.builder()
              .name("main")
              .rolls(ConstantRange.of(1))
              .addEntry(withExplosionDecay(slab, ItemLootEntry.builder(slab)
                          .acceptFunction(SetCount.builder(ConstantRange.of(2))
                                .acceptCondition(BlockStateProperty.builder(slab)
                                      .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withProp(SlabBlock.TYPE, SlabType.DOUBLE)))
                          )
                    )
              )
        );
    }

    /**
     * Like vanilla's registerDropping except with a named pool
     */
    @Override
    public void registerDropping(@Nonnull Block block, @Nonnull IItemProvider drop) {
        registerLootTable(block, dropping(drop));
    }

    /**
     * Like vanilla's dropping except with a named pool
     */
    protected static LootTable.Builder dropping(IItemProvider item) {
        return LootTable.builder().addLootPool(withSurvivesExplosion(item, LootPool.builder()
              .name("main")
              .rolls(ConstantRange.of(1))
              .addEntry(ItemLootEntry.builder(item))
        ));
    }

    /**
     * Like vanilla's droppingWithSilkTouchOrRandomly except with a named pool
     */
    protected static LootTable.Builder droppingWithSilkTouchOrRandomly(@Nonnull Block block, @Nonnull IItemProvider item, @Nonnull IRandomRange range) {
        return droppingWithSilkTouch(block, withExplosionDecay(block, ItemLootEntry.builder(item).acceptFunction(SetCount.builder(range))));
    }

    /**
     * Like vanilla's droppingWithSilkTouch except with a named pool
     */
    protected static LootTable.Builder droppingWithSilkTouch(@Nonnull Block block, @Nonnull LootEntry.Builder<?> builder) {
        return dropping(block, SILK_TOUCH, builder);
    }

    /**
     * Like vanilla's dropping except with a named pool
     */
    protected static LootTable.Builder dropping(@Nonnull Block block, @Nonnull ILootCondition.IBuilder conditionBuilder, @Nonnull LootEntry.Builder<?> entry) {
        return LootTable.builder().addLootPool(LootPool.builder()
              .name("main")
              .rolls(ConstantRange.of(1))
              .addEntry(ItemLootEntry.builder(block)
                    .acceptCondition(conditionBuilder)
                    .alternatively(entry)
              )
        );
    }
}