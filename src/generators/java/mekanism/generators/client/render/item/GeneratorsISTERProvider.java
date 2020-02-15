package mekanism.generators.client.render.item;

import java.util.concurrent.Callable;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class GeneratorsISTERProvider {

    public static Callable<ItemStackTileEntityRenderer> advancedSolar() {
        return RenderAdvancedSolarGeneratorItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> bio() {
        return RenderBioGeneratorItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> gasBurning() {
        return RenderGasGeneratorItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> heat() {
        return RenderHeatGeneratorItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> wind() {
        return RenderWindGeneratorItem::new;
    }
}