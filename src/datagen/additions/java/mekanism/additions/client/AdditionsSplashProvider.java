package mekanism.additions.client;

import java.util.function.Consumer;
import mekanism.additions.common.MekanismAdditions;
import mekanism.client.splash.BaseSplashProvider;
import net.minecraft.data.PackOutput;

public class AdditionsSplashProvider extends BaseSplashProvider {

    public AdditionsSplashProvider(PackOutput output) {
        super(output, MekanismAdditions.MODID);
    }

    @Override
    protected void collectSplashes(Consumer<String> consumer) {
        consumer.accept("Even more babies!");
    }
}