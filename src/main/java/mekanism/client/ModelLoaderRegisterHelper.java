package mekanism.client;

import mekanism.client.render.MekanismRenderer;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class ModelLoaderRegisterHelper {

    public static Runnable registerModelLoader() {
        return MekanismRenderer::registerModelLoader;
    }
}