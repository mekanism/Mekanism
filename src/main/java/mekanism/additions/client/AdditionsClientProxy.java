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
    /*@SubscribeEvent
    public void onStitch(TextureStitchEvent.Pre event) {
        //if(event.map.getTextureType() == 0) {
        FluidRegistry.getFluid("enrichedwater").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedWater"));
        Mekanism.logger.debug(FluidRegistry.getFluid("enrichedwater").getIcon().toString());
        FluidRegistry.getFluid("enrichedwatersnd").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedWaterSnd"));
        Mekanism.logger.debug(FluidRegistry.getFluid("enrichedwatersnd").getIcon().toString());
        FluidRegistry.getFluid("enrichedwaterrd").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedWaterRd"));
        Mekanism.logger.debug(FluidRegistry.getFluid("enrichedwaterrd").getIcon().toString());

        FluidRegistry.getFluid("enricheddihydrogensulfidGas").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedDihydrogenSulfidGas"));
        Mekanism.logger.debug(FluidRegistry.getFluid("enricheddihydrogensulfidGas").getIcon().toString());
        FluidRegistry.getFluid("enricheddihydrogensulfidsnd").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedDihydrogenSulfidSnd"));
        Mekanism.logger.debug(FluidRegistry.getFluid("enricheddihydrogensulfidsnd").getIcon().toString());
        FluidRegistry.getFluid("enricheddihydrogensulfidrd").setIcons(event.map.registerIcon("mekanismadditions:textures/blocks/liquid/LiquidEnrichedDihydrogenSulfidRd"));
        Mekanism.logger.debug(FluidRegistry.getFluid("enricheddihydrogensulfidrd").getIcon().toString());
        //}
    }*/
}
