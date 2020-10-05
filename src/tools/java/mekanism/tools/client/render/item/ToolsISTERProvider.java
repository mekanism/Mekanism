package mekanism.tools.client.render.item;

import java.util.concurrent.Callable;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class ToolsISTERProvider {

    private ToolsISTERProvider() {
    }

    public static Callable<ItemStackTileEntityRenderer> shield() {
        return RenderMekanismShieldItem::new;
    }
}