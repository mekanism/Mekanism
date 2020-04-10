package mekanism.api.datagen.tag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ForgeRegistryTagProvider<TYPE extends IForgeRegistryEntry<TYPE>> implements IDataProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final DataGenerator gen;
    private final Map<Tag<TYPE>, Tag.Builder<TYPE>> tagToBuilder = new Object2ObjectLinkedOpenHashMap<>();
    private final IForgeRegistry<TYPE> registry;
    protected final String modid;

    protected ForgeRegistryTagProvider(DataGenerator gen, String modid, IForgeRegistry<TYPE> registry) {
        this.gen = gen;
        this.modid = modid;
        this.registry = registry;
    }

    protected abstract void registerTags();

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void act(@Nonnull DirectoryCache cache) {
        tagToBuilder.clear();
        registerTags();
        TagCollection<TYPE> tagCollection = new TagCollection<>(id -> Optional.empty(), "", false, "generated");
        tagCollection.registerAll(tagToBuilder.entrySet().stream().collect(Collectors.toMap(tag -> tag.getKey().getId(), Entry::getValue)));
        tagCollection.getTagMap().forEach((id, tag) -> {
            Path path = makePath(id);
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
        setCollection(tagCollection);
    }

    protected Tag.Builder<TYPE> getBuilder(Tag<TYPE> tag) {
        return tagToBuilder.computeIfAbsent(tag, ignored -> Tag.Builder.create());
    }

    protected abstract void setCollection(TagCollection<TYPE> collection);

    @Nonnull
    protected abstract Path makePath(ResourceLocation id);
}