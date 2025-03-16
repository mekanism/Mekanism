package mekanism.common.loot.table;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.BlockPersonalStorage;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.TileEntityUpdateable;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseBlockLootTables extends BlockLootSubProvider {

    private final Set<Block> knownBlocks = new ReferenceOpenHashSet<>();
    //Note: We use an array set as we never expect this to have more than a few elements (in reality it only ever has one)
    private final Set<Block> toSkip = new ReferenceArraySet<>();

    protected BaseBlockLootTables(HolderLookup.Provider provider) {
        //Note: We manually handle explosion resistance on a case by case basis dynamically
        super(Collections.emptySet(), FeatureFlags.VANILLA_SET, provider);
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

    @SafeVarargs
    protected final void skip(Holder<Block>... blockProviders) {
        for (Holder<Block> blockProvider : blockProviders) {
            toSkip.add(blockProvider.value());
        }
    }

    protected boolean skipBlock(Holder<Block> holder) {
        Block block = holder.value();
        //Skip any blocks that we already registered a table for or have marked to skip
        return knownBlocks.contains(block) || toSkip.contains(block);
    }

    protected void add(Holder<Block> block, Function<Block, LootTable.Builder> factory) {
        add(block.value(), factory);
    }

    protected LootTable.Builder createOreDrop(Block block, Holder<Item> item) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(item.value())
              .apply(ApplyBonusCount.addOreBonusCount(this.registries.holderOrThrow(Enchantments.FORTUNE)))
        ));
    }

    protected LootTable.Builder droppingWithFortuneOrRandomly(Block block, Holder<Item> item, UniformGenerator range) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(item.value())
              .apply(SetItemCountFunction.setCount(range))
              .apply(ApplyBonusCount.addOreBonusCount(this.registries.holderOrThrow(Enchantments.FORTUNE)))
        ));
    }

    //Holder versions of BlockLootTable methods, modified to support varargs/lists
    protected void dropSelf(Collection<? extends Holder<Block>> blocks) {
        for (Holder<Block> block : blocks) {
            if (!skipBlock(block)) {
                dropSelf(block.value());
            }
        }
    }

    protected void add(Function<Block, Builder> factory, Collection<? extends Holder<Block>> blockProviders) {
        for (Holder<Block> blockProvider : blockProviders) {
            add(blockProvider.value(), factory);
        }
    }

    @SafeVarargs
    protected final void add(Function<Block, Builder> factory, Holder<Block>... blockProviders) {
        for (Holder<Block> blockProvider : blockProviders) {
            add(blockProvider.value(), factory);
        }
    }

    protected void add(Function<Block, Builder> factory, OreBlockType... oreTypes) {
        for (OreBlockType oreType : oreTypes) {
            add(oreType.stone(), factory);
            add(oreType.deepslate(), factory);
        }
    }

    protected void dropSelfWithContents(Collection<? extends DeferredHolder<Block, ?>> blockProviders) {
        //TODO: See if there is other stuff we want to be transferring which we currently do not
        // For example, when writing this we added dump mode for chemical tanks to getting transferred to the item
        for (DeferredHolder<Block, ?> blockProvider : blockProviders) {
            if (skipBlock(blockProvider)) {
                continue;
            }
            Block block = blockProvider.value();
            boolean hasComponents = false;
            CopyComponentsFunction.Builder componentsBuilder = CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY);
            boolean hasContents = false;
            ItemStack stack = new ItemStack(block);
            LootItem.Builder<?> itemLootPool = LootItem.lootTableItem(block);
            //delayed items until after other copies are added, for cases like referencing the owner
            DelayedLootItemBuilder delayedPool = new DelayedLootItemBuilder();
            @Nullable
            BlockEntity tile = null;
            if (block instanceof EntityBlock entityBlock) {
                tile = entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            }
            if (tile instanceof IFrequencyHandler frequencyHandler) {
                Set<FrequencyType<?>> customFrequencies = frequencyHandler.getFrequencyComponent().getCustomFrequencies();
                if (!customFrequencies.isEmpty() && stack.getItem() instanceof IFrequencyItem frequencyItem) {
                    FrequencyType<?> frequencyType = frequencyItem.getFrequencyType();
                    if (!customFrequencies.contains(frequencyType)) {
                        Mekanism.logger.warn("Block missing frequency type '{}' expected by item: {}", frequencyType.getName(), blockProvider.getId());
                    }
                }
            }
            if (tile instanceof TileEntityUpdateable tileEntity) {
                List<DataComponentType<?>> components = tileEntity.getRemapEntries();
                if (!components.isEmpty()) {
                    Set<DataComponentType<?>> skipTypes = ContainerType.TYPES.stream().map(type -> type.getComponentType().get()).collect(Collectors.toSet());
                    hasComponents = true;
                    //Sort the components so that the order is consistent when writing to json
                    components.sort(Comparator.comparing(BuiltInRegistries.DATA_COMPONENT_TYPE::getKey, ResourceLocation::compareNamespaced));
                    for (DataComponentType<?> remapEntry : components) {
                        //Allow containers to be handled below where we do extra validation that the stack actually supports it
                        if (!skipTypes.contains(remapEntry)) {
                            componentsBuilder.include(remapEntry);
                        }
                    }
                }
            }
            if (tile instanceof TileEntityMekanism tileEntity) {
                if (tileEntity.isNameable()) {
                    itemLootPool.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY));
                }
                for (ContainerType<?, ?, ?> type : ContainerType.TYPES) {
                    List<?> containers = tileEntity.persists(type) ? type.getContainers(tileEntity) : Collections.emptyList();
                    int attachmentContainers = type.getContainerCount(stack);
                    if (containers.size() == attachmentContainers) {
                        if (!containers.isEmpty()) {
                            componentsBuilder.include(type.getComponentType().get());
                            hasComponents = true;
                            if (type != ContainerType.ENERGY && type != ContainerType.HEAT) {
                                hasContents = true;
                            }
                        }
                    } else if (attachmentContainers == 0) {
                        //TODO: Improve how we handle skipping warnings for known missing types
                        if (type == ContainerType.ITEM && block instanceof BlockPersonalStorage<?, ?>) {
                            //We don't want explosions causing personal storage items to be directly destroyed. It is also known that the attachment is missing
                            hasContents = true;
                        } else if (type != ContainerType.CHEMICAL || !MekanismBlocks.RADIOACTIVE_WASTE_BARREL.keyMatches(blockProvider)) {
                            Mekanism.logger.warn("Container type: {}, item missing attachments: {}", type.getComponentName(), blockProvider.getId());
                        }
                    } else if (containers.isEmpty()) {
                        Mekanism.logger.warn("Container type: {}, item has attachments but block doesn't have containers: {}", type.getComponentName(), blockProvider.getId());
                    } else {
                        Mekanism.logger.warn("Container type: {}, has {} item attachments and block has {} containers: {}", type.getComponentName(), attachmentContainers,
                              containers.size(), blockProvider.getId());
                    }
                }
            }
            @SuppressWarnings("unchecked")
            AttributeInventory<DelayedLootItemBuilder> attributeInventory = Attribute.get(blockProvider, AttributeInventory.class);
            if (attributeInventory != null) {
                hasContents |= attributeInventory.applyLoot(delayedPool);
            }
            if (hasComponents) {
                itemLootPool.apply(componentsBuilder);
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
    @Override
    protected LootTable.Builder createSilkTouchDispatchTable(@NotNull Block block, @NotNull LootPoolEntryContainer.Builder<?> builder) {
        return createSelfDropDispatchTable(block, hasSilkTouch(), builder);
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