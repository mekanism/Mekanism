package mekanism.client;

import mekanism.client.render.obj.TransmitterLoader;
import mekanism.common.Mekanism;
import net.minecraftforge.client.model.ModelLoaderRegistry;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class ModelLoaderRegisterHelper {

    public static Runnable registerModelLoader() {
        return () -> ModelLoaderRegistry.registerLoader(Mekanism.rl("transmitter"), TransmitterLoader.INSTANCE);
    }
}