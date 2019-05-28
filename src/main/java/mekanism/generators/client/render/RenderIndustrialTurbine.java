package mekanism.generators.client.render;

import mekanism.api.Coord4D;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderIndustrialTurbine extends TileEntitySpecialRenderer<TileEntityTurbineCasing> {

    private FluidStack STEAM = new FluidStack(FluidRegistry.getFluid("steam"), 1);

    @Override
    public void render(TileEntityTurbineCasing tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        renderAModelAt(tileEntity, x, y, z, partialTick, destroyStage);
    }

    public void renderAModelAt(TileEntityTurbineCasing tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        if (tileEntity.clientHasStructure && tileEntity.isRendering && tileEntity.structure != null
            && tileEntity.structure.complex != null) {
            RenderTurbineRotor.internalRender = true;
            Coord4D coord = tileEntity.structure.complex;

            while (true) {
                coord = coord.offset(EnumFacing.DOWN);
                TileEntity tile = coord.getTileEntity(tileEntity.getWorld());
                if (!(tile instanceof TileEntityTurbineRotor)) {
                    break;
                }
                TileEntityRendererDispatcher.instance.render(tile, partialTick, destroyStage);
            }

            RenderTurbineRotor.internalRender = false;

            if (tileEntity.structure.fluidStored != null && tileEntity.structure.fluidStored.amount != 0 && tileEntity.structure.volLength > 0) {
                RenderData data = new RenderData();

                data.location = tileEntity.structure.renderLocation;
                data.height = tileEntity.structure.lowerVolume / (tileEntity.structure.volLength * tileEntity.structure.volWidth);
                data.length = tileEntity.structure.volLength;
                data.width = tileEntity.structure.volWidth;
                data.fluidType = STEAM;

                bindTexture(MekanismRenderer.getBlocksTexture());

                if (data.location != null && data.height >= 1 && tileEntity.structure.fluidStored.getFluid() != null) {
                    MekanismRenderHelper renderHelper = FluidRenderer.initHelper();
                    FluidRenderer.translateToOrigin(data.location);
                    MekanismRenderer.glowOn(tileEntity.structure.fluidStored.getFluid().getLuminosity());
                    renderHelper.color(tileEntity.structure.fluidStored);
                    renderHelper.colorAlpha(Math.min(1, ((float) tileEntity.structure.fluidStored.amount / (float) tileEntity.structure.getFluidCapacity()) + MekanismRenderer.GAS_RENDER_BASE));
                    FluidRenderer.getTankDisplay(data).render();
                    MekanismRenderer.glowOff();
                    renderHelper.cleanup();
                }
            }
        }
    }

    private int getStages(int height) {
        return TankUpdateProtocol.FLUID_PER_TANK / 10;
    }
}