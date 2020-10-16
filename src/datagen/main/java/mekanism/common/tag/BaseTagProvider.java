package mekanism.common.tag;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Map;
import java.util.function.Function;
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
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IPigmentProvider;
import mekanism.api.providers.ISlurryProvider;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class BaseTagProvider implements IDataProvider {

    private final Map<TagType<?>, Map<INamedTag<?>, ITag.Builder>> supportedTagTypes = new Object2ObjectLinkedOpenHashMap<>();
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
        addTagType(TagType.ENCHANTMENT);
        addTagType(TagType.POTION);
        addTagType(TagType.TILE_ENTITY_TYPE);
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

    @Override
    public void act(@Nonnull DirectoryCache cache) {
        supportedTagTypes.values().forEach(Map::clear);
        registerTags();
        supportedTagTypes.forEach((tagType, tagTypeMap) -> act(cache, tagType, tagTypeMap));
    }

    private <TYPE extends IForgeRegistryEntry<TYPE>> void act(@Nonnull DirectoryCache cache, TagType<TYPE> tagType, Map<INamedTag<?>, ITag.Builder> tagTypeMap) {
        if (!tagTypeMap.isEmpty()) {
            //Create a dummy forge registry tags provider and pass all our collected data through to it
            new ForgeRegistryTagsProvider<TYPE>(gen, tagType.getRegistry(), modid, existingFileHelper) {
                @Override
                protected void registerTags() {
                    //Add each tag builder to the wrapped provider's builder, but wrap the builder so that we
                    // make sure to first cleanup and remove excess/unused json components
                    // Note: We only override the methods used by the TagsProvider rather than proxying everything back to the original tag builder
                    tagTypeMap.forEach((tag, tagBuilder) -> tagToBuilder.put(tag.getName(), new ITag.Builder() {
                        @Nonnull
                        @Override
                        public JsonObject serialize() {
                            return cleanJsonTag(tagBuilder.serialize());
                        }

                        @Nonnull
                        @Override
                        public <T> Stream<ITag.Proxy> getProxyTags(@Nonnull Function<ResourceLocation, ITag<T>> resourceTagFunction,
                              @Nonnull Function<ResourceLocation, T> resourceElementFunction) {
                            return tagBuilder.getProxyTags(resourceTagFunction, resourceElementFunction);
                        }
                    }));
                }

                @Nonnull
                @Override
                public String getName() {
                    return tagType.getName() + " Tags: " + modid;
                }
            }.act(cache);
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
    protected <TYPE extends IForgeRegistryEntry<TYPE>> ForgeRegistryTagBuilder<TYPE> getBuilder(TagType<TYPE> tagType, INamedTag<TYPE> tag) {
        return new ForgeRegistryTagBuilder<>(supportedTagTypes.get(tagType).computeIfAbsent(tag, ignored -> ITag.Builder.create()), modid);
    }

    protected ForgeRegistryTagBuilder<Item> getItemBuilder(INamedTag<Item> tag) {
        return getBuilder(TagType.ITEM, tag);
    }

    protected ForgeRegistryTagBuilder<Block> getBlockBuilder(INamedTag<Block> tag) {
        return getBuilder(TagType.BLOCK, tag);
    }

    protected ForgeRegistryTagBuilder<EntityType<?>> getEntityTypeBuilder(INamedTag<EntityType<?>> tag) {
        return getBuilder(TagType.ENTITY_TYPE, tag);
    }

    protected ForgeRegistryTagBuilder<Fluid> getFluidBuilder(INamedTag<Fluid> tag) {
        return getBuilder(TagType.FLUID, tag);
    }

    protected ForgeRegistryTagBuilder<Enchantment> getEnchantmentBuilder(INamedTag<Enchantment> tag) {
        return getBuilder(TagType.ENCHANTMENT, tag);
    }

    protected ForgeRegistryTagBuilder<Potion> getPotionBuilder(INamedTag<Potion> tag) {
        return getBuilder(TagType.POTION, tag);
    }

    protected ForgeRegistryTagBuilder<TileEntityType<?>> getTileEntityTypeBuilder(INamedTag<TileEntityType<?>> tag) {
        return getBuilder(TagType.TILE_ENTITY_TYPE, tag);
    }

    protected ForgeRegistryTagBuilder<Gas> getGasBuilder(INamedTag<Gas> tag) {
        return getBuilder(TagType.GAS, tag);
    }

    protected ForgeRegistryTagBuilder<InfuseType> getInfuseTypeBuilder(INamedTag<InfuseType> tag) {
        return getBuilder(TagType.INFUSE_TYPE, tag);
    }

    protected ForgeRegistryTagBuilder<Pigment> getPigmentBuilder(INamedTag<Pigment> tag) {
        return getBuilder(TagType.PIGMENT, tag);
    }

    protected ForgeRegistryTagBuilder<Slurry> getSlurryBuilder(INamedTag<Slurry> tag) {
        return getBuilder(TagType.SLURRY, tag);
    }

    protected void addToTag(INamedTag<Item> tag, IItemProvider... itemProviders) {
        ForgeRegistryTagBuilder<Item> tagBuilder = getItemBuilder(tag);
        for (IItemProvider itemProvider : itemProviders) {
            tagBuilder.add(itemProvider.getItem());
        }
    }

    protected void addToTag(INamedTag<Block> tag, IBlockProvider... blockProviders) {
        ForgeRegistryTagBuilder<Block> tagBuilder = getBlockBuilder(tag);
        for (IBlockProvider blockProvider : blockProviders) {
            tagBuilder.add(blockProvider.getBlock());
        }
    }

    protected void addToTags(INamedTag<Item> itemTag, INamedTag<Block> blockTag, IBlockProvider... blockProviders) {
        ForgeRegistryTagBuilder<Item> itemTagBuilder = getItemBuilder(itemTag);
        ForgeRegistryTagBuilder<Block> blockTagBuilder = getBlockBuilder(blockTag);
        for (IBlockProvider blockProvider : blockProviders) {
            itemTagBuilder.add(blockProvider.getItem());
            blockTagBuilder.add(blockProvider.getBlock());
        }
    }

    protected void addToTag(INamedTag<EntityType<?>> tag, IEntityTypeProvider... entityTypeProviders) {
        ForgeRegistryTagBuilder<EntityType<?>> tagBuilder = getEntityTypeBuilder(tag);
        for (IEntityTypeProvider entityTypeProvider : entityTypeProviders) {
            tagBuilder.add(entityTypeProvider.getEntityType());
        }
    }

    protected void addToTag(INamedTag<Fluid> tag, FluidRegistryObject<?, ?, ?, ?>... fluidRegistryObjects) {
        ForgeRegistryTagBuilder<Fluid> tagBuilder = getFluidBuilder(tag);
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : fluidRegistryObjects) {
            tagBuilder.add(fluidRO.getStillFluid(), fluidRO.getFlowingFluid());
        }
    }

    protected void addToTag(INamedTag<TileEntityType<?>> tag, TileEntityTypeRegistryObject<?>... tileEntityTypeRegistryObjects) {
        ForgeRegistryTagBuilder<TileEntityType<?>> tagBuilder = getTileEntityTypeBuilder(tag);
        for (TileEntityTypeRegistryObject<?> tileEntityTypeRO : tileEntityTypeRegistryObjects) {
            tagBuilder.add(tileEntityTypeRO.get());
        }
    }

    protected void addToTag(INamedTag<Gas> tag, IGasProvider... gasProviders) {
        addToTag(getGasBuilder(tag), gasProviders);
    }

    protected void addToTag(INamedTag<InfuseType> tag, IInfuseTypeProvider... infuseTypeProviders) {
        addToTag(getInfuseTypeBuilder(tag), infuseTypeProviders);
    }

    protected void addToTag(INamedTag<Pigment> tag, IPigmentProvider... pigmentProviders) {
        addToTag(getPigmentBuilder(tag), pigmentProviders);
    }

    protected void addToTag(INamedTag<Slurry> tag, ISlurryProvider... slurryProviders) {
        addToTag(getSlurryBuilder(tag), slurryProviders);
    }

    @SafeVarargs
    protected final <CHEMICAL extends Chemical<CHEMICAL>> void addToTag(ForgeRegistryTagBuilder<CHEMICAL> tagBuilder, IChemicalProvider<CHEMICAL>... providers) {
        for (IChemicalProvider<CHEMICAL> provider : providers) {
            tagBuilder.add(provider.getChemical());
        }
    }
}