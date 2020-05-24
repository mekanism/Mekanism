package mekanism.common.tag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.providers.IBlockProvider;
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
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
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
            TagCollection<TYPE> tagCollection = new TagCollection<>(id -> Optional.empty(), "", false, "generated");
            tagCollection.registerAll(tagTypeMap.getBuilders());
            TagType<TYPE> tagType = tagTypeMap.getTagType();
            IForgeRegistry<TYPE> registry = tagType.getRegistry();
            for (Entry<ResourceLocation, Tag<TYPE>> entry : tagCollection.getTagMap().entrySet()) {
                ResourceLocation id = entry.getKey();
                Path path = gen.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/" + tagType.getPath() + "/" + id.getPath() + ".json");
                try {
                    String json = GSON.toJson(cleanJsonTag(entry.getValue().serialize(registry::getKey)));
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
            tagType.setCollection(tagCollection);
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
        if (tagAsJson.has(DataGenJsonConstants.OPTIONAL)) {
            //Strip out the forge added "optional" list from the tag json if it is empty, as the param itself is optional
            JsonArray optionalTags = tagAsJson.getAsJsonArray(DataGenJsonConstants.OPTIONAL);
            if (optionalTags.size() == 0) {
                tagAsJson.remove(DataGenJsonConstants.OPTIONAL);
            }
        }
        return tagAsJson;
    }

    //Protected to allow for extensions to add retrieve their own supported types if they have any
    protected <TYPE extends IForgeRegistryEntry<TYPE>> TagTypeMap<TYPE> getTagTypeMap(TagType<TYPE> tagType) {
        return (TagTypeMap<TYPE>) supportedTagTypes.get(tagType);
    }

    protected <TYPE extends IForgeRegistryEntry<TYPE>> Tag.Builder<TYPE> getBuilder(TagType<TYPE> tagType, Tag<TYPE> tag) {
        return getTagTypeMap(tagType).getBuilder(tag);
    }

    protected Tag.Builder<Item> getItemBuilder(Tag<Item> tag) {
        return getBuilder(TagType.ITEM, tag);
    }

    protected Tag.Builder<Block> getBlockBuilder(Tag<Block> tag) {
        return getBuilder(TagType.BLOCK, tag);
    }

    protected Tag.Builder<EntityType<?>> getEntityTypeBuilder(Tag<EntityType<?>> tag) {
        return getBuilder(TagType.ENTITY_TYPE, tag);
    }

    protected Tag.Builder<Fluid> getFluidBuilder(Tag<Fluid> tag) {
        return getBuilder(TagType.FLUID, tag);
    }

    protected Tag.Builder<Gas> getGasBuilder(Tag<Gas> tag) {
        return getBuilder(TagType.GAS, tag);
    }

    protected Tag.Builder<InfuseType> getInfuseTypeBuilder(Tag<InfuseType> tag) {
        return getBuilder(TagType.INFUSE_TYPE, tag);
    }

    protected Tag.Builder<Pigment> getPigmentBuilder(Tag<Pigment> tag) {
        return getBuilder(TagType.PIGMENT, tag);
    }

    protected Tag.Builder<Slurry> getSlurryBuilder(Tag<Slurry> tag) {
        return getBuilder(TagType.SLURRY, tag);
    }

    protected void addToTag(Tag<Item> tag, IItemProvider... itemProviders) {
        Tag.Builder<Item> tagBuilder = getItemBuilder(tag);
        for (IItemProvider itemProvider : itemProviders) {
            tagBuilder.add(itemProvider.getItem());
        }
    }

    protected void addToTag(Tag<Block> tag, IBlockProvider... blockProviders) {
        Tag.Builder<Block> tagBuilder = getBlockBuilder(tag);
        for (IBlockProvider blockProvider : blockProviders) {
            tagBuilder.add(blockProvider.getBlock());
        }
    }

    protected void addToTags(Tag<Item> itemTag, Tag<Block> blockTag, IBlockProvider... blockProviders) {
        Tag.Builder<Item> itemTagBuilder = getItemBuilder(itemTag);
        Tag.Builder<Block> blockTagBuilder = getBlockBuilder(blockTag);
        for (IBlockProvider blockProvider : blockProviders) {
            itemTagBuilder.add(blockProvider.getItem());
            blockTagBuilder.add(blockProvider.getBlock());
        }
    }

    protected void addToTag(Tag<EntityType<?>> tag, IEntityTypeProvider... entityTypeProviders) {
        Tag.Builder<EntityType<?>> tagBuilder = getEntityTypeBuilder(tag);
        for (IEntityTypeProvider entityTypeProvider : entityTypeProviders) {
            tagBuilder.add(entityTypeProvider.getEntityType());
        }
    }

    protected void addToTag(Tag<Fluid> tag, FluidRegistryObject<?, ?, ?, ?>... fluidRegistryObjects) {
        Tag.Builder<Fluid> tagBuilder = getFluidBuilder(tag);
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : fluidRegistryObjects) {
            tagBuilder.add(fluidRO.getStillFluid(), fluidRO.getFlowingFluid());
        }
    }

    protected void addToTag(Tag<Gas> tag, IGasProvider... gasProviders) {
        Tag.Builder<Gas> tagBuilder = getGasBuilder(tag);
        for (IGasProvider gasProvider : gasProviders) {
            tagBuilder.add(gasProvider.getGas());
        }
    }

    protected void addToTag(Tag<InfuseType> tag, IInfuseTypeProvider... infuseTypeProviders) {
        Tag.Builder<InfuseType> tagBuilder = getInfuseTypeBuilder(tag);
        for (IInfuseTypeProvider infuseTypeProvider : infuseTypeProviders) {
            tagBuilder.add(infuseTypeProvider.getInfuseType());
        }
    }

    protected void addToTag(Tag<Pigment> tag, IPigmentProvider... pigmentProviders) {
        Tag.Builder<Pigment> tagBuilder = getPigmentBuilder(tag);
        for (IPigmentProvider pigmentProvider : pigmentProviders) {
            tagBuilder.add(pigmentProvider.getPigment());
        }
    }

    protected void addToTag(Tag<Slurry> tag, ISlurryProvider... slurryProviders) {
        Tag.Builder<Slurry> tagBuilder = getSlurryBuilder(tag);
        for (ISlurryProvider slurryProvider : slurryProviders) {
            tagBuilder.add(slurryProvider.getSlurry());
        }
    }
}