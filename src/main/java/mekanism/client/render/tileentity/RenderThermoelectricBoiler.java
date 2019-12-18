package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class RenderThermoelectricBoiler extends MekanismTileEntityRenderer<TileEntityBoilerCasing> {

    @Nonnull
    private static FluidStack STEAM = FluidStack.EMPTY;
    @Nonnull
    private static final FluidStack WATER = new FluidStack(Fluids.WATER, 1);

    @Override
    public void func_225616_a_(@Nonnull TileEntityBoilerCasing tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        /*if (tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.renderLocation != null &&
            tile.structure.upperRenderLocation != null) {
            FluidStack waterStored = tile.structure.waterStored;
            boolean glChanged = false;
            if (waterStored.getAmount() > 0) {
                RenderData data = new RenderData();
                data.location = tile.structure.renderLocation;
                data.height = tile.structure.upperRenderLocation.y - 1 - tile.structure.renderLocation.y;
                data.length = tile.structure.volLength;
                data.width = tile.structure.volWidth;
                data.fluidType = WATER;

                if (data.height >= 1 && waterStored.getFluid() != Fluids.EMPTY) {
                    field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
                    RenderSystem.pushMatrix();
                    glChanged = makeGLChanges(glChanged);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(waterStored);
                    MekanismRenderer.color(waterStored, (float) waterStored.getAmount() / (float) tile.clientWaterCapacity);
                    if (waterStored.getFluid().getAttributes().isGaseous(waterStored)) {
                        FluidRenderer.getTankDisplay(data).render();
                    } else {
                        FluidRenderer.getTankDisplay(data, tile.prevWaterScale).render();
                    }
                    MekanismRenderer.resetColor();
                    MekanismRenderer.disableGlow(glowInfo);
                    RenderSystem.popMatrix();

                    for (ValveData valveData : tile.valveViewing) {
                        RenderSystem.pushMatrix();
                        FluidRenderer.translateToOrigin(valveData.location);
                        GlowInfo valveGlowInfo = MekanismRenderer.enableGlow(waterStored);
                        FluidRenderer.getValveDisplay(ValveRenderData.get(data, valveData)).render();
                        MekanismRenderer.disableGlow(valveGlowInfo);
                        RenderSystem.popMatrix();
                    }
                }
            }

            if (tile.structure.steamStored.getAmount() > 0) {
                if (STEAM.isEmpty()) {
                    STEAM = MekanismFluids.STEAM.getFluidStack(1);
                }
                RenderData data = new RenderData();
                data.location = tile.structure.upperRenderLocation;
                data.height = tile.structure.renderLocation.y + tile.structure.volHeight - 2 - tile.structure.upperRenderLocation.y;
                data.length = tile.structure.volLength;
                data.width = tile.structure.volWidth;
                data.fluidType = STEAM;

                if (data.height >= 1 && tile.structure.steamStored.getFluid() != Fluids.EMPTY) {
                    field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
                    RenderSystem.pushMatrix();
                    glChanged = makeGLChanges(glChanged);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(tile.structure.steamStored);

                    DisplayInteger display = FluidRenderer.getTankDisplay(data);
                    MekanismRenderer.color(tile.structure.steamStored, (float) tile.structure.steamStored.getAmount() / (float) tile.clientSteamCapacity);
                    display.render();
                    MekanismRenderer.resetColor();
                    MekanismRenderer.disableGlow(glowInfo);
                    RenderSystem.popMatrix();
                }
            }
            if (glChanged) {
                setLightmapDisabled(false);
                RenderSystem.enableLighting();
                RenderSystem.disableBlend();
                RenderSystem.disableCull();
            }
        }*/
    }

    //TODO: 1.15
    /*private boolean makeGLChanges(boolean glChanged) {
        if (!glChanged) {
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.disableLighting();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            setLightmapDisabled(true);
        }
        return true;
    }*/

    @Override
    public boolean isGlobalRenderer(TileEntityBoilerCasing tile) {
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.renderLocation != null && tile.structure.upperRenderLocation != null;
    }
}