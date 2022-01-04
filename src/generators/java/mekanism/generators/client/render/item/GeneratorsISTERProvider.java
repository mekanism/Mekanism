package mekanism.generators.client.render.item;

import mekanism.client.render.item.ISTERProvider.MekRenderProperties;
import net.minecraftforge.client.IItemRenderProperties;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class GeneratorsISTERProvider {

    private GeneratorsISTERProvider() {
    }

    public static IItemRenderProperties wind() {
        return new MekRenderProperties(RenderWindGeneratorItem.RENDERER);
    }
}