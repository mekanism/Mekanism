package mekanism.common.world_modifier;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import java.util.function.BiConsumer;
import mekanism.common.Mekanism;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataGenerator.Target;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

public abstract class BaseModifierProvider<MODIFIER> implements DataProvider {

    private static final Lazy<RegistryOps<JsonElement>> OPS = Lazy.of(() -> RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy()));
    private final DataGenerator.PathProvider pathProvider;
    private final Codec<MODIFIER> modifierCodec;
    protected final String modid;

    protected BaseModifierProvider(DataGenerator gen, String modid, ResourceKey<Registry<MODIFIER>> registry) {
        this.modid = modid;
        this.modifierCodec = (Codec<MODIFIER>) RegistryAccess.REGISTRIES.get(registry).codec();
        this.pathProvider = gen.createPathProvider(Target.DATA_PACK, getPath(registry.location()));
    }

    @Override
    public void run(@NotNull CachedOutput cache) {
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
                  DataProvider.saveStable(cache, encoded, pathProvider.json(new ResourceLocation(modid, getPath(name))));
              }
        ));
    }

    private static String getPath(ResourceLocation rl) {
        return rl.getNamespace().equals("minecraft") ? rl.getPath() : rl.getNamespace() + "/" + rl.getPath();
    }

    protected abstract void getModifiers(RegistryGetter registryGetter, BiConsumer<MODIFIER, ResourceLocation> consumer);

    @NotNull
    @Override
    public abstract String getName();

    public interface RegistryGetter {

        <E> Registry<E> get(ResourceKey<? extends Registry<? extends E>> key);
    }
}