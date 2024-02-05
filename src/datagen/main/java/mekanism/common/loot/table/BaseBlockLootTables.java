package mekanism.common.loot.table;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.AttachedContainers;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.attribute.Attributes.AttributeRedstone;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.item.loot.CopyContainersLootFunction;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.util.RegistryUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction.MergeStrategy;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseBlockLootTables extends BlockLootSubProvider {

    private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item()
          .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));

    private final Set<Block> knownBlocks = new ReferenceOpenHashSet<>();
    //Note: We use an array set as we never expect this to have more than a few elements (in reality it only ever has one)
    private final Set<Block> toSkip = new ReferenceArraySet<>();

    protected BaseBlockLootTables() {
        //Note: We manually handle explosion resistance on a case by case basis dynamically
        super(Collections.emptySet(), FeatureFlags.VANILLA_SET);
    }

    @Override
    protected void add(@NotNull Block block, @NotNull LootTable.Builder table) {
        //Overwrite the core register method to add to our list of known blocks
        super.add(block, table);
        knownBlocks.add(block);
    }

    @NotNull
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

    protected LootTable.Builder createOreDrop(Block block, ItemLike item) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(item.asItem())
              .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
        ));
    }

    protected LootTable.Builder droppingWithFortuneOrRandomly(Block block, ItemLike item, UniformGenerator range) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(item.asItem())
              .apply(SetItemCountFunction.setCount(range))
              .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
        ));
    }

    //IBlockProvider versions of BlockLootTable methods, modified to support varargs
    protected void dropSelf(Collection<? extends Holder<Block>> blockProviders) {
        for (Holder<Block> blockProvider : blockProviders) {
            Block block = blockProvider.value();
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

    protected void add(Function<Block, Builder> factory, OreBlockType... oreTypes) {
        for (OreBlockType oreType : oreTypes) {
            add(oreType.stoneBlock(), factory);
            add(oreType.deepslateBlock(), factory);
        }
    }

    protected void dropSelfWithContents(Collection<? extends Holder<Block>> blockProviders) {
        //TODO: See if there is other stuff we want to be transferring which we currently do not
        // For example, when writing this we added dump mode for chemical tanks to getting transferred to the item
        for (Holder<Block> blockProvider : blockProviders) {
            Block block = blockProvider.value();
            if (skipBlock(block)) {
                continue;
            }
            TrackingNbtBuilder nbtBuilder = new TrackingNbtBuilder(ContextNbtProvider.BLOCK_ENTITY);
            TrackingContainerBuilder containerBuilder = new TrackingContainerBuilder();
            boolean hasContents = false;
            LootItem.Builder<?> itemLootPool = LootItem.lootTableItem(block);
            //delayed items until after NBT copy is added
            DelayedLootItemBuilder delayedPool = new DelayedLootItemBuilder();
            @Nullable
            BlockEntity tile = null;
            if (block instanceof IHasTileEntity<?> hasTileEntity) {
                tile = hasTileEntity.createDummyBlockEntity();
            }
            if (tile instanceof IFrequencyHandler frequencyHandler && frequencyHandler.getFrequencyComponent().hasCustomFrequencies()) {
                nbtBuilder.copy(NBTConstants.COMPONENT_FREQUENCY, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_FREQUENCY);
            }
            if (Attribute.has(block, AttributeSecurity.class)) {
                //TODO: Should we just save the entire security component?
                nbtBuilder.copy(NBTConstants.COMPONENT_SECURITY + "." + NBTConstants.OWNER_UUID, NBTConstants.MEK_DATA + "." + NBTConstants.OWNER_UUID);
                nbtBuilder.copy(NBTConstants.COMPONENT_SECURITY + "." + NBTConstants.SECURITY_MODE, NBTConstants.MEK_DATA + "." + NBTConstants.SECURITY_MODE);
            }
            if (Attribute.has(block, AttributeUpgradeSupport.class)) {
                nbtBuilder.copy(NBTConstants.COMPONENT_UPGRADE, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_UPGRADE);
            }
            if (tile instanceof ISideConfiguration) {
                nbtBuilder.copy(NBTConstants.COMPONENT_CONFIG, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_CONFIG);
                nbtBuilder.copy(NBTConstants.COMPONENT_EJECTOR, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_EJECTOR);
            }
            if (tile instanceof ISustainedData sustainedData) {
                Set<Entry<String, String>> remapEntries = sustainedData.getTileDataRemap().entrySet();
                for (Entry<String, String> remapEntry : remapEntries) {
                    nbtBuilder.copy(remapEntry.getKey(), NBTConstants.MEK_DATA + "." + remapEntry.getValue());
                }
            }
            if (Attribute.has(block, AttributeRedstone.class)) {
                nbtBuilder.copy(NBTConstants.CONTROL_TYPE, NBTConstants.MEK_DATA + "." + NBTConstants.CONTROL_TYPE);
            }
            if (tile instanceof TileEntityMekanism tileEntity) {
                if (tileEntity.isNameable()) {
                    itemLootPool.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY));
                }
                ItemStack stack = new ItemStack(block);
                for (ContainerType<?, ?, ?> type : ContainerType.SUBSTANCES) {
                    AttachedContainers<?> attachment = type.getAttachment(stack);
                    List<?> containers = tileEntity.handles(type) ? type.getContainers(tileEntity) : List.of();
                    List<?> attachmentContainers = attachment == null ? List.of() : attachment.getContainers();
                    if (containers.size() == attachmentContainers.size()) {
                        if (!containers.isEmpty()) {
                            containerBuilder.copy(type);
                            if (type != ContainerType.ENERGY && type != ContainerType.HEAT) {
                                hasContents = true;
                            }
                        }
                    } else if (attachmentContainers.isEmpty()) {
                        Mekanism.logger.warn("Substance type: {}, item missing attachments: {}", type.getAttachmentName(), RegistryUtils.getName(block));
                    } else if (containers.isEmpty()) {
                        Mekanism.logger.warn("Substance type: {}, item has attachments but block doesn't have containers: {}", type.getAttachmentName(), RegistryUtils.getName(block));
                    } else {
                        Mekanism.logger.warn("Substance type: {}, has {} item attachments and block has {} containers: {}", type.getAttachmentName(), attachmentContainers.size(),
                              containers.size(), RegistryUtils.getName(block));
                    }
                }
            }
            @SuppressWarnings("unchecked")
            AttributeInventory<DelayedLootItemBuilder> attributeInventory = Attribute.get(block, AttributeInventory.class);
            if (attributeInventory != null) {
                if (attributeInventory.hasCustomLoot()) {
                    hasContents = attributeInventory.applyLoot(delayedPool, nbtBuilder);
                }
                //If the block has an inventory and no custom loot function, copy the inventory slots,
                // but if it is an IItemHandler, which for most cases of ours it will be,
                // then only copy the slots if we actually have any slots because otherwise maybe something just went wrong
                else if (!(tile instanceof IItemHandler handler) || handler.getSlots() > 0) {
                    //If we don't actually handle saving an inventory (such as the quantum entangloporter, don't actually add it as something to copy)
                    if (!(tile instanceof TileEntityMekanism tileMek) || tileMek.persistInventory()) {
                        //TODO - 1.20.4: IMPLEMENT VIA ATTACHMENTS??
                        nbtBuilder.copy(NBTConstants.ITEMS, NBTConstants.MEK_DATA + "." + NBTConstants.ITEMS);
                        hasContents = true;
                    }
                }
            }
            if (block instanceof BlockCardboardBox) {
                //TODO: Do this better so that it doesn't have to be as hard coded to being a cardboard box
                nbtBuilder.copy(NBTConstants.DATA, NBTConstants.MEK_DATA + "." + NBTConstants.DATA);
            }
            if (nbtBuilder.hasData()) {
                itemLootPool.apply(nbtBuilder);
            }
            if (containerBuilder.hasData()) {
                itemLootPool.apply(containerBuilder);
            }
            //apply the delayed ones last, so that NBT funcs have happened first
            for (LootItemFunction.Builder function : delayedPool.functions) {
                itemLootPool.apply(function);
            }
            for (LootItemCondition.Builder condition : delayedPool.conditions) {
                itemLootPool.when(condition);
            }
            add(block, LootTable.lootTable().withPool(applyExplosionCondition(hasContents, LootPool.lootPool()
                  .name("main")
                  .setRolls(ConstantValue.exactly(1))
                  .add(itemLootPool)
            )));
        }
    }

    /**
     * Like vanilla's {@link BlockLootSubProvider#applyExplosionCondition(ItemLike, ConditionUserBuilder)} except with a boolean for if it is explosion resistant.
     */
    private static <T extends ConditionUserBuilder<T>> T applyExplosionCondition(boolean explosionResistant, ConditionUserBuilder<T> condition) {
        return explosionResistant ? condition.unwrap() : condition.when(ExplosionCondition.survivesExplosion());
    }

    /**
     * Like vanilla's {@link BlockLootSubProvider#createSlabItemTable(Block)} except with a named pool
     */
    @NotNull
    @Override
    protected LootTable.Builder createSlabItemTable(@NotNull Block slab) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
              .name("main")
              .setRolls(ConstantValue.exactly(1))
              .add(applyExplosionDecay(slab, LootItem.lootTableItem(slab)
                          .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(slab)
                                      .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)))
                          )
                    )
              )
        );
    }

    /**
     * Like vanilla's {@link BlockLootSubProvider#dropOther(Block, ItemLike)} except with a named pool
     */
    @Override
    public void dropOther(@NotNull Block block, @NotNull ItemLike drop) {
        add(block, createSingleItemTable(drop));
    }

    /**
     * Like vanilla's {@link BlockLootSubProvider#createSingleItemTable(ItemLike)} except with a named pool
     */
    @NotNull
    @Override
    public LootTable.Builder createSingleItemTable(@NotNull ItemLike item) {
        return LootTable.lootTable().withPool(applyExplosionCondition(item, LootPool.lootPool()
              .name("main")
              .setRolls(ConstantValue.exactly(1))
              .add(LootItem.lootTableItem(item))
        ));
    }

    /**
     * Like vanilla's {@link BlockLootSubProvider#createSingleItemTableWithSilkTouch(Block, ItemLike, NumberProvider)} except with a named pool
     */
    @NotNull
    @Override
    protected LootTable.Builder createSingleItemTableWithSilkTouch(@NotNull Block block, @NotNull ItemLike item, @NotNull NumberProvider range) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(range))));
    }

    /**
     * Like vanilla's {@link BlockLootSubProvider#createSilkTouchDispatchTable(Block, LootPoolEntryContainer.Builder)} except with a named pool
     */
    @NotNull
    protected static LootTable.Builder createSilkTouchDispatchTable(@NotNull Block block, @NotNull LootPoolEntryContainer.Builder<?> builder) {
        return createSelfDropDispatchTable(block, HAS_SILK_TOUCH, builder);
    }

    /**
     * Like vanilla's {@link BlockLootSubProvider#createSelfDropDispatchTable(Block, LootItemCondition.Builder, LootPoolEntryContainer.Builder)} except with a named pool
     */
    @NotNull
    protected static LootTable.Builder createSelfDropDispatchTable(@NotNull Block block, @NotNull LootItemCondition.Builder conditionBuilder,
          @NotNull LootPoolEntryContainer.Builder<?> entry) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
              .name("main")
              .setRolls(ConstantValue.exactly(1))
              .add(LootItem.lootTableItem(block)
                    .when(conditionBuilder)
                    .otherwise(entry)
              )
        );
    }

    @MethodsReturnNonnullByDefault
    @ParametersAreNotNullByDefault
    public static class TrackingNbtBuilder extends CopyNbtFunction.Builder {

        private boolean hasData = false;

        public TrackingNbtBuilder(NbtProvider source) {
            super(source);
        }

        public boolean hasData() {
            return this.hasData;
        }

        @Override
        public CopyNbtFunction.Builder copy(String sourcePath, String targetPath, MergeStrategy copyAction) {
            this.hasData = true;
            return super.copy(sourcePath, targetPath, copyAction);
        }
    }

    @MethodsReturnNonnullByDefault
    @ParametersAreNotNullByDefault
    public static class TrackingContainerBuilder extends CopyContainersLootFunction.Builder {

        private boolean hasData = false;

        public boolean hasData() {
            return this.hasData;
        }

        @Override
        public CopyContainersLootFunction.Builder copy(ContainerType<?, ?, ?> containerType) {
            this.hasData = true;
            return super.copy(containerType);
        }
    }

    @NothingNullByDefault
    public static class DelayedLootItemBuilder implements ConditionUserBuilder<DelayedLootItemBuilder>, FunctionUserBuilder<DelayedLootItemBuilder> {

        private final List<LootItemFunction.Builder> functions = new ArrayList<>();
        private final List<LootItemCondition.Builder> conditions = new ArrayList<>();

        @Override
        public DelayedLootItemBuilder apply(LootItemFunction.Builder pFunctionBuilder) {
            functions.add(pFunctionBuilder);
            return this;
        }

        @Override
        public DelayedLootItemBuilder when(LootItemCondition.Builder pConditionBuilder) {
            conditions.add(pConditionBuilder);
            return this;
        }

        @Override
        public DelayedLootItemBuilder unwrap() {
            return this;
        }
    }
}