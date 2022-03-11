package mekanism.common.tag;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IEntityTypeProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.providers.IPigmentProvider;
import mekanism.api.providers.ISlurryProvider;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class BaseTagProvider implements DataProvider {

    private final Map<TagType<?>, Map<TagKey<?>, Tag.Builder>> supportedTagTypes = new Object2ObjectLinkedOpenHashMap<>();
    private final Set<Block> knownHarvestRequirements = new HashSet<>();
    private final ExistingFileHelper existingFileHelper;
    private final DataGenerator gen;
    private final String modid;

    protected BaseTagProvider(DataGenerator gen, String modid, @Nullable ExistingFileHelper existingFileHelper) {
        this.gen = gen;
        this.modid = modid;
        this.existingFileHelper = existingFileHelper;
        addTagType(TagType.ITEM);
        addTagType(TagType.BLOCK);
        addTagType(TagType.ENTITY_TYPE);
        addTagType(TagType.FLUID);
        addTagType(TagType.BLOCK_ENTITY_TYPE);
        addTagType(TagType.GAS);
        addTagType(TagType.INFUSE_TYPE);
        addTagType(TagType.PIGMENT);
        addTagType(TagType.SLURRY);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Tags: " + modid;
    }

    //Protected to allow for extensions to add their own supported types if they have one
    protected <TYPE extends IForgeRegistryEntry<TYPE>> void addTagType(TagType<TYPE> tagType) {
        supportedTagTypes.computeIfAbsent(tagType, type -> new Object2ObjectLinkedOpenHashMap<>());
    }

    protected abstract void registerTags();

    protected List<IBlockProvider> getAllBlocks() {
        return Collections.emptyList();
    }

    protected void hasHarvestData(Block block) {
        knownHarvestRequirements.add(block);
    }

    @Override
    public void run(@Nonnull HashCache cache) {
        supportedTagTypes.values().forEach(Map::clear);
        registerTags();
        for (IBlockProvider blockProvider : getAllBlocks()) {
            Block block = blockProvider.getBlock();
            if (block.defaultBlockState().requiresCorrectToolForDrops() && !knownHarvestRequirements.contains(block)) {
                throw new IllegalStateException("Missing harvest tool type for block '" + block.getRegistryName() +
                                                "' that requires the correct tool for drops.");
            }
        }
        supportedTagTypes.forEach((tagType, tagTypeMap) -> act(cache, tagType, tagTypeMap));
    }

    private <TYPE extends IForgeRegistryEntry<TYPE>> void act(@Nonnull HashCache cache, TagType<TYPE> tagType, Map<TagKey<?>, Tag.Builder> tagTypeMap) {
        if (!tagTypeMap.isEmpty()) {
            //Create a dummy forge registry tags provider and pass all our collected data through to it
            new ForgeRegistryTagsProvider<>(gen, tagType.getRegistry(), modid, existingFileHelper) {
                @Override
                protected void addTags() {
                    //Add each tag builder to the wrapped provider's builder, but wrap the builder so that we
                    // make sure to first cleanup and remove excess/unused json components
                    // Note: We only override the methods used by the TagsProvider rather than proxying everything back to the original tag builder
                    tagTypeMap.forEach((tag, tagBuilder) -> builders.put(tag.location(), new Tag.Builder() {
                        @Nonnull
                        @Override
                        public JsonObject serializeToJson() {
                            return cleanJsonTag(tagBuilder.serializeToJson());
                        }

                        @Nonnull
                        @Override
                        public Stream<Tag.BuilderEntry> getEntries() {
                            return tagBuilder.getEntries();
                        }
                    }));
                }

                @Nonnull
                @Override
                public String getName() {
                    return tagType.name() + " Tags: " + modid;
                }
            }.run(cache);
        }
    }

    private JsonObject cleanJsonTag(JsonObject tagAsJson) {
        if (tagAsJson.has(DataGenJsonConstants.REPLACE)) {
            //Strip out the optional "replace" entry from the tag if it is the default value
            JsonPrimitive replace = tagAsJson.getAsJsonPrimitive(DataGenJsonConstants.REPLACE);
            if (replace.isBoolean() && !replace.getAsBoolean()) {
                tagAsJson.remove(DataGenJsonConstants.REPLACE);
            }
        }
        return tagAsJson;
    }

    //Protected to allow for extensions to add retrieve their own supported types if they have any
    protected <TYPE extends IForgeRegistryEntry<TYPE>> ForgeRegistryTagBuilder<TYPE> getBuilder(TagType<TYPE> tagType, TagKey<TYPE> tag) {
        return new ForgeRegistryTagBuilder<>(supportedTagTypes.get(tagType).computeIfAbsent(tag, ignored -> Tag.Builder.tag()), modid);
    }

    protected ForgeRegistryTagBuilder<Item> getItemBuilder(TagKey<Item> tag) {
        return getBuilder(TagType.ITEM, tag);
    }

    protected ForgeRegistryTagBuilder<Block> getBlockBuilder(TagKey<Block> tag) {
        return getBuilder(TagType.BLOCK, tag);
    }

    protected ForgeRegistryTagBuilder<EntityType<?>> getEntityTypeBuilder(TagKey<EntityType<?>> tag) {
        return getBuilder(TagType.ENTITY_TYPE, tag);
    }

    protected ForgeRegistryTagBuilder<Fluid> getFluidBuilder(TagKey<Fluid> tag) {
        return getBuilder(TagType.FLUID, tag);
    }

    protected ForgeRegistryTagBuilder<BlockEntityType<?>> getTileEntityTypeBuilder(TagKey<BlockEntityType<?>> tag) {
        return getBuilder(TagType.BLOCK_ENTITY_TYPE, tag);
    }

    protected ForgeRegistryTagBuilder<Gas> getGasBuilder(TagKey<Gas> tag) {
        return getBuilder(TagType.GAS, tag);
    }

    protected ForgeRegistryTagBuilder<InfuseType> getInfuseTypeBuilder(TagKey<InfuseType> tag) {
        return getBuilder(TagType.INFUSE_TYPE, tag);
    }

    protected ForgeRegistryTagBuilder<Pigment> getPigmentBuilder(TagKey<Pigment> tag) {
        return getBuilder(TagType.PIGMENT, tag);
    }

    protected ForgeRegistryTagBuilder<Slurry> getSlurryBuilder(TagKey<Slurry> tag) {
        return getBuilder(TagType.SLURRY, tag);
    }

    protected void addToTag(TagKey<Item> tag, ItemLike... itemProviders) {
        getItemBuilder(tag).addTyped(ItemLike::asItem, itemProviders);
    }

    protected void addToTag(TagKey<Block> tag, IBlockProvider... blockProviders) {
        getBlockBuilder(tag).addTyped(IBlockProvider::getBlock, blockProviders);
    }

    @SafeVarargs
    protected final void addToTag(TagKey<Block> blockTag, Map<?, ? extends IBlockProvider>... blockProviders) {
        ForgeRegistryTagBuilder<Block> tagBuilder = getBlockBuilder(blockTag);
        for (Map<?, ? extends IBlockProvider> blockProvider : blockProviders) {
            for (IBlockProvider value : blockProvider.values()) {
                tagBuilder.add(value.getBlock());
            }
        }
    }

    protected void addToHarvestTag(TagKey<Block> tag, IBlockProvider... blockProviders) {
        ForgeRegistryTagBuilder<Block> tagBuilder = getBlockBuilder(tag);
        for (IBlockProvider blockProvider : blockProviders) {
            Block block = blockProvider.getBlock();
            tagBuilder.add(block);
            hasHarvestData(block);
        }
    }

    @SafeVarargs
    protected final void addToHarvestTag(TagKey<Block> blockTag, Map<?, ? extends IBlockProvider>... blockProviders) {
        ForgeRegistryTagBuilder<Block> tagBuilder = getBlockBuilder(blockTag);
        for (Map<?, ? extends IBlockProvider> blockProvider : blockProviders) {
            for (IBlockProvider value : blockProvider.values()) {
                Block block = value.getBlock();
                tagBuilder.add(block);
                hasHarvestData(block);
            }
        }
    }

    protected void addToTags(TagKey<Item> itemTag, TagKey<Block> blockTag, IBlockProvider... blockProviders) {
        ForgeRegistryTagBuilder<Item> itemTagBuilder = getItemBuilder(itemTag);
        ForgeRegistryTagBuilder<Block> blockTagBuilder = getBlockBuilder(blockTag);
        for (IBlockProvider blockProvider : blockProviders) {
            itemTagBuilder.add(blockProvider.asItem());
            blockTagBuilder.add(blockProvider.getBlock());
        }
    }

    protected void addToTag(TagKey<EntityType<?>> tag, IEntityTypeProvider... entityTypeProviders) {
        getEntityTypeBuilder(tag).addTyped(IEntityTypeProvider::getEntityType, entityTypeProviders);
    }

    protected void addToTag(TagKey<Fluid> tag, FluidRegistryObject<?, ?, ?, ?>... fluidRegistryObjects) {
        ForgeRegistryTagBuilder<Fluid> tagBuilder = getFluidBuilder(tag);
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : fluidRegistryObjects) {
            tagBuilder.add(fluidRO.getStillFluid(), fluidRO.getFlowingFluid());
        }
    }

    protected void addToTag(TagKey<BlockEntityType<?>> tag, TileEntityTypeRegistryObject<?>... tileEntityTypeRegistryObjects) {
        getTileEntityTypeBuilder(tag).add(tileEntityTypeRegistryObjects);
    }

    protected void addToTag(TagKey<Gas> tag, IGasProvider... gasProviders) {
        addToTag(getGasBuilder(tag), gasProviders);
    }

    protected void addToTag(TagKey<InfuseType> tag, IInfuseTypeProvider... infuseTypeProviders) {
        addToTag(getInfuseTypeBuilder(tag), infuseTypeProviders);
    }

    protected void addToTag(TagKey<Pigment> tag, IPigmentProvider... pigmentProviders) {
        addToTag(getPigmentBuilder(tag), pigmentProviders);
    }

    protected void addToTag(TagKey<Slurry> tag, ISlurryProvider... slurryProviders) {
        addToTag(getSlurryBuilder(tag), slurryProviders);
    }

    @SafeVarargs
    protected final <CHEMICAL extends Chemical<CHEMICAL>> void addToTag(ForgeRegistryTagBuilder<CHEMICAL> tagBuilder, IChemicalProvider<CHEMICAL>... providers) {
        tagBuilder.addTyped(IChemicalProvider::getChemical, providers);
    }
}