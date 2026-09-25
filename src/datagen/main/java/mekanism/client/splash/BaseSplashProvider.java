package mekanism.client.splash;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import mekanism.common.MekanismDataGenerator;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;

public abstract class BaseSplashProvider implements DataProvider {

    private final Path path;
    private final String modid;

    protected BaseSplashProvider(PackOutput output, String modid) {
        this.path = output.createPathProvider(Target.RESOURCE_PACK, "texts").file(Identifier.fromNamespaceAndPath(modid, "splashes"), "txt");
        this.modid = modid;
    }

    protected abstract void collectSplashes(Consumer<String> consumer);

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return MekanismDataGenerator.save(output, stream -> {
            Set<String> splashes = new LinkedHashSet<>();
            collectSplashes(splash -> {
                if (!splashes.add(splash)) {
                    throw new IllegalArgumentException("Duplicate splash: " + splash);
                }
            });
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8))) {
                for (String splash : splashes) {
                    //Mirror CsvOutput#writeLine
                    writer.write(splash + "\r\n");
                }
            }
        }, path);
    }

    @Override
    public String getName() {
        return "Splash Provider: " + modid;
    }
}