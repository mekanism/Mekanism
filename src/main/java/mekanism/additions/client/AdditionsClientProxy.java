package mekanism.additions.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.additions.common.AdditionsCommonProxy;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.FluidRegistry;

@SideOnly(Side.CLIENT)
public class AdditionsClientProxy extends AdditionsCommonProxy {
    @SubscribeEvent
    public void onStitch(TextureStitchEvent.Pre event) {
        if(event.map.getTextureType() == 0) {
            FluidRegistry.getFluid("enrichedwater").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedWater"));
            FluidRegistry.getFluid("enrichedwatersnd").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedWaterSnd"));
            FluidRegistry.getFluid("enrichedwaterrd").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedWaterRd"));

            //FluidRegistry.getFluid("enricheddihydrogensulfidgas").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedDihydrogenSulfidGas"));
            FluidRegistry.getFluid("enricheddihydrogensulfidsnd").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedDihydrogenSulfidSnd"));
            FluidRegistry.getFluid("enricheddihydrogensulfidrd").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedDihydrogenSulfidRd"));
        }
    }
}
