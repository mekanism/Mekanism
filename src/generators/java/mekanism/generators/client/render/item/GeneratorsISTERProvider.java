package mekanism.generators.client.render.item;

import java.util.concurrent.Callable;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class GeneratorsISTERProvider {

    private GeneratorsISTERProvider() {
    }

    public static Callable<ItemStackTileEntityRenderer> wind() {
        return RenderWindGeneratorItem::new;
    }
}