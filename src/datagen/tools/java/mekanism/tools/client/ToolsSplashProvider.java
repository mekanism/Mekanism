package mekanism.tools.client;

import java.util.function.Consumer;
import mekanism.client.splash.BaseSplashProvider;
import mekanism.tools.common.MekanismTools;
import net.minecraft.data.PackOutput;

public class ToolsSplashProvider extends BaseSplashProvider {

    public ToolsSplashProvider(PackOutput output) {
        super(output, MekanismTools.MODID);
    }

    @Override
    protected void collectSplashes(Consumer<String> consumer) {
        //Like vanilla's splash line: And my pickaxe!
        consumer.accept("And my paxel!");
    }
}