package mekanism.common.biome_modifier;

import com.google.gson.JsonElement;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class BaseBiomeModifierProvider implements DataProvider {

    private final DataGenerator gen;
    private final String modid;

    protected BaseBiomeModifierProvider(DataGenerator gen, String modid) {
        this.gen = gen;
        this.modid = modid;
    }

    @Override
    public void run(@Nonnull CachedOutput cache) {
        ResourceLocation biomeModifiersRegistryID = ForgeRegistries.Keys.BIOME_MODIFIERS.location();
        Path outputFolder = gen.getOutputFolder().resolve(String.join("/", PackType.SERVER_DATA.getDirectory(), modid,
              biomeModifiersRegistryID.getNamespace(), biomeModifiersRegistryID.getPath()));
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());
        Registry<Biome> biomeRegistry = ops.registry(Registry.BIOME_REGISTRY).get();
        getModifiers(biomeRegistry, LamdbaExceptionUtils.rethrowBiConsumer((modifier, name) -> {
                  JsonElement encoded = BiomeModifier.DIRECT_CODEC.encodeStart(ops, modifier)
                        //Log  an error message if it fails
                        .getOrThrow(false, msg -> Mekanism.logger.error("Failed to encode {}: {}", name, msg));
                  DataProvider.saveStable(cache, encoded, outputFolder.resolve(name.getNamespace() + "/" + name.getPath() + ".json"));
              }
        ));
    }

    protected abstract void getModifiers(Registry<Biome> biomeRegistry, BiConsumer<BiomeModifier, ResourceLocation> consumer);

    @Nonnull
    @Override
    public String getName() {
        return "Biome modifiers: " + modid;
    }
}