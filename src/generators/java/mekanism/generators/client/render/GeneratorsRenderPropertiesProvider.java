package mekanism.generators.client.render;

import mekanism.client.render.RenderPropertiesProvider.MekRenderProperties;
import mekanism.generators.client.render.item.RenderWindGeneratorItem;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class GeneratorsRenderPropertiesProvider {

    private GeneratorsRenderPropertiesProvider() {
    }

    public static IClientItemExtensions wind() {
        return new MekRenderProperties(RenderWindGeneratorItem.RENDERER);
    }
}