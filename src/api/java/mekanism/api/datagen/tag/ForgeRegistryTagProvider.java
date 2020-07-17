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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ForgeRegistryTagProvider<TYPE extends IForgeRegistryEntry<TYPE>> implements IDataProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final DataGenerator gen;
    private final Map<INamedTag<TYPE>, Tag.Builder> tagToBuilder = new Object2ObjectLinkedOpenHashMap<>();
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
        if (!tagToBuilder.isEmpty()) {
            Map<ResourceLocation, ITag.Builder> builders = tagToBuilder.entrySet().stream().collect(Collectors.toMap(tag -> tag.getKey().getName(), Entry::getValue));
            builders.forEach((id, tagBuilder) -> {
                Path path = makePath(id);
                try {
                    String json = GSON.toJson(tagBuilder.serialize());
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
        }
    }

    protected ForgeRegistryTagBuilder<TYPE> getBuilder(INamedTag<TYPE> tag) {
        return new ForgeRegistryTagBuilder<>(tagToBuilder.computeIfAbsent(tag, ignored -> Tag.Builder.create()), modid);
    }

    @Nonnull
    protected abstract Path makePath(ResourceLocation id);
}