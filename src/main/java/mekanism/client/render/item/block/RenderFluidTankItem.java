package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RenderFluidTankItem implements SpecialModelRenderer<RenderFluidTankItem.TankRenderState> {

    public static final RenderFluidTankItem RENDERER = new RenderFluidTankItem();

    @Override
    public void submit(@Nullable TankRenderState argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        //TODO - 26.1: rendering
        if (argument == null) {
            return;
        }
        if (argument.fluidScale > 0) {
            int lightToUse = MekanismRenderer.calculateGlowLight(lightCoords, argument.fluidLight);
            /*RenderResizableCuboid.renderCube(argument.fluidModel(), poseStack, renderer.getBuffer(Sheets.translucentCullBlockSheet()),
                  argument.fluidColor, lightToUse, overlayCoords, FaceDisplay.FRONT,
                  camera, null);*/
        }
        if (argument.blockModelRenderState != null) {//remove this after we have it for sure
            argument.blockModelRenderState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        //TODO - 26.1: rendering
    }

    @Nullable
    @Override
    public TankRenderState extractArgument(ItemStack stack) {
        IMekanismFluidHandler attachment = ContainerType.FLUID.createHandler(stack);
        float fluidScale = 0;
        int fluidLight = 0;
        int fluidColor = 0;
        Model3D fluidModel = null;
        if (attachment != null) {
            FluidStack fluid = attachment.getFluidInTank(0);
            if (!fluid.isEmpty()) {
                fluidScale = (float) fluid.getAmount() / attachment.getTankCapacity(0);
                fluidModel = RenderFluidTank.getFluidModel(fluid, fluidScale);
                fluidLight = fluid.getFluidType().getLightLevel(fluid);
                fluidColor = MekanismRenderer.getColorARGB(fluid, fluidScale);
            }
        }
        //TODO - 26.1: block model render extract
        //renderBlockItem(stack, displayContext, matrix, renderer, light, overlayLight, ModelData.EMPTY);
        BlockModelRenderState blockModel = null;
        return new TankRenderState(fluidScale, fluidLight, fluidColor, fluidModel, blockModel);
    }

    public record TankRenderState(float fluidScale, int fluidLight, int fluidColor, @Nullable Model3D fluidModel, BlockModelRenderState blockModelRenderState) {}
}