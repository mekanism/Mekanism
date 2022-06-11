package mekanism.common.world_modifier;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.util.Lazy;

public abstract class BaseModifierProvider<MODIFIER> implements DataProvider {

    private static final Lazy<RegistryOps<JsonElement>> OPS = Lazy.of(() -> RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy()));
    private final Codec<MODIFIER> modifierCodec;
    protected final String modid;
    private final Path outputFolder;

    protected BaseModifierProvider(DataGenerator gen, String modid, ResourceKey<Registry<MODIFIER>> registry, Codec<MODIFIER> modifierCodec) {
        this.modid = modid;
        this.modifierCodec = modifierCodec;
        ResourceLocation registryId = registry.location();
        this.outputFolder = gen.getOutputFolder().resolve(String.join("/", PackType.SERVER_DATA.getDirectory(), this.modid, registryId.getNamespace(),
              registryId.getPath()));
    }

    @Override
    public void run(@Nonnull CachedOutput cache) {
        RegistryOps<JsonElement> ops = OPS.get();
        getModifiers(new RegistryGetter() {
            @Override
            public <E> Registry<E> get(ResourceKey<? extends Registry<? extends E>> key) {
                return ops.registry(key).orElseThrow();
            }
        }, LamdbaExceptionUtils.rethrowBiConsumer((modifier, name) -> {
                  JsonElement encoded = modifierCodec.encodeStart(ops, modifier)
                        //Log  an error message if it fails
                        .getOrThrow(false, msg -> Mekanism.logger.error("Failed to encode {}: {}", name, msg));
                  String outputName = name.getNamespace().equals("minecraft") ? name.getPath() : name.getNamespace() + "/" + name.getPath();
                  DataProvider.saveStable(cache, encoded, outputFolder.resolve(outputName + ".json"));
              }
        ));
    }

    protected abstract void getModifiers(RegistryGetter registryGetter, BiConsumer<MODIFIER, ResourceLocation> consumer);

    @Nonnull
    @Override
    public abstract String getName();

    public interface RegistryGetter {

        <E> Registry<E> get(ResourceKey<? extends Registry<? extends E>> key);
    }
}