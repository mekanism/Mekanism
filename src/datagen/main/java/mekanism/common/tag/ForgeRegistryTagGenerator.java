package mekanism.common.tag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//TODO: We either should remove this (given we use BaseTagGenerator), or move some form of this to the API package
// for basic API support for allowing registering tags for gas and infusion types
public abstract class ForgeRegistryTagGenerator<TYPE extends IForgeRegistryEntry<TYPE>> implements IDataProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final DataGenerator gen;
    private final Map<Tag<TYPE>, Tag.Builder<TYPE>> tagToBuilder = new LinkedHashMap<>();
    private final String modid;
    private final TagType<TYPE> tagType;

    protected ForgeRegistryTagGenerator(DataGenerator gen, String modid, TagType<TYPE> tagType) {
        this.gen = gen;
        this.modid = modid;
        this.tagType = tagType;
    }

    protected abstract void registerTags();

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void act(@Nonnull DirectoryCache cache) {
        tagToBuilder.clear();
        registerTags();
        TagCollection<TYPE> tagCollection = new TagCollection<>(id -> Optional.empty(), "", false, "generated");
        tagCollection.registerAll(tagToBuilder.entrySet().stream().collect(Collectors.toMap(tag -> tag.getKey().getId(), Entry::getValue)));
        IForgeRegistry<TYPE> registry = tagType.getRegistry();
        tagCollection.getTagMap().forEach((id, tag) -> {
            Path path = gen.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/" + tagType.getPath() + "/" + id.getPath() + ".json");
            try {
                String json = GSON.toJson(tag.serialize(registry::getKey));
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
        });
        tagType.setCollection(tagCollection);
    }

    protected Tag.Builder<TYPE> getBuilder(Tag<TYPE> tag) {
        return tagToBuilder.computeIfAbsent(tag, ignored -> Tag.Builder.create());
    }

    @Nonnull
    @Override
    public String getName() {
        return tagType.getName() + " Tags: " + modid;
    }
}