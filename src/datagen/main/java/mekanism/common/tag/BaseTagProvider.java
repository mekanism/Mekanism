package mekanism.common.tag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
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
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ITag.Proxy;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseTagProvider implements IDataProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<TagType<?>, TagTypeMap<?>> supportedTagTypes = new Object2ObjectLinkedOpenHashMap<>();
    private final DataGenerator gen;
    private final String modid;

    protected BaseTagProvider(DataGenerator gen, String modid) {
        this.gen = gen;
        this.modid = modid;
        addTagType(TagType.ITEM);
        addTagType(TagType.BLOCK);
        addTagType(TagType.ENTITY_TYPE);
        addTagType(TagType.FLUID);
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
        supportedTagTypes.putIfAbsent(tagType, new TagTypeMap<>(tagType));
    }

    protected abstract void registerTags();

    @Override
    public void act(@Nonnull DirectoryCache cache) {
        supportedTagTypes.values().forEach(TagTypeMap::clear);
        registerTags();
        supportedTagTypes.values().forEach(tagTypeMap -> act(cache, tagTypeMap));
    }

    @SuppressWarnings("UnstableApiUsage")
    private <TYPE extends IForgeRegistryEntry<TYPE>> void act(@Nonnull DirectoryCache cache, TagTypeMap<TYPE> tagTypeMap) {
        if (!tagTypeMap.isEmpty()) {
            TagType<TYPE> tagType = tagTypeMap.getTagType();
            IForgeRegistry<TYPE> registry = tagType.getRegistry();
            ITag<TYPE> emptyTag = Tag.func_241284_a_();
            Map<ResourceLocation, ITag.Builder> tagToBuilder = tagTypeMap.getBuilders();
            //TODO - 1.16: Give this function a better name
            Function<ResourceLocation, ITag<TYPE>> function = id -> tagToBuilder.containsKey(id) ? emptyTag : null;
            for (Entry<ResourceLocation, ITag.Builder> entry : tagToBuilder.entrySet()) {
                ResourceLocation id = entry.getKey();
                ITag.Builder tagBuilder = entry.getValue();
                List<Proxy> list = tagBuilder.func_232963_b_(function, registry::getValue).collect(Collectors.toList());
                if (!list.isEmpty()) {
                    throw new IllegalArgumentException(String.format("Couldn't define tag %s as it is missing following references: %s", id,
                          list.stream().map(Objects::toString).collect(Collectors.joining(","))));
                } else {
                    Path path = gen.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/" + tagType.getPath() + "/" + id.getPath() + ".json");
                    try {
                        String json = GSON.toJson(cleanJsonTag(tagBuilder.func_232965_c_()));
                        String hash = HASH_FUNCTION.hashUnencodedChars(json).toString();
                        if (!Objects.equals(cache.getPreviousHash(path), hash) || !Files.exists(path)) {
                            Files.createDirectories(path.getParent());
                            try (BufferedWriter bufferedwriter = Files.newBufferedWriter(path)) {
                                bufferedwriter.write(json);
                            }
                        }
                        cache.recordHash(path, hash);
                    } catch (IOException exception) {
                        LOGGER.error("Couldn't save tags to {}", path, exception);
                    }
                }
            }
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
    protected <TYPE extends IForgeRegistryEntry<TYPE>> TagTypeMap<TYPE> getTagTypeMap(TagType<TYPE> tagType) {
        return (TagTypeMap<TYPE>) supportedTagTypes.get(tagType);
    }

    protected <TYPE extends IForgeRegistryEntry<TYPE>> ForgeRegistryTagBuilder<TYPE> getBuilder(TagType<TYPE> tagType, INamedTag<TYPE> tag) {
        return getTagTypeMap(tagType).getBuilder(tag, modid);
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