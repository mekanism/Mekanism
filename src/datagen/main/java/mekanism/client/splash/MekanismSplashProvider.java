package mekanism.client.splash;

import java.util.function.Consumer;
import mekanism.common.Mekanism;
import net.minecraft.data.PackOutput;

public class MekanismSplashProvider extends BaseSplashProvider {

    public MekanismSplashProvider(PackOutput output) {
        super(output, Mekanism.MODID);
    }

    @Override
    protected void collectSplashes(Consumer<String> consumer) {
        consumer.accept("Cardboard Boxes for Cats!");
        //Like vanilla's splash line: Minecraft!
        consumer.accept(Mekanism.MOD_NAME + "!");
        //Like vanilla's splash line: Ingots!
        consumer.accept("Chemicals!");
        //Like vanilla's splash line: RIBBIT!
        consumer.accept("ROBIT!");
    }
}