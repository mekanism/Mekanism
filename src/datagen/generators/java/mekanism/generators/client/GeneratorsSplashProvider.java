package mekanism.generators.client;

import java.util.function.Consumer;
import mekanism.client.splash.BaseSplashProvider;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.data.PackOutput;

public class GeneratorsSplashProvider extends BaseSplashProvider {

    public GeneratorsSplashProvider(PackOutput output) {
        super(output, MekanismGenerators.MODID);
    }

    @Override
    protected void collectSplashes(Consumer<String> consumer) {
        consumer.accept("Generating power!");
    }
}