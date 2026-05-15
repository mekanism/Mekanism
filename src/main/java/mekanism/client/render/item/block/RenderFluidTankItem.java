package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.client.ModelUtil;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.RenderResizableCuboid.TexturePicker;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.joml.Vector3fc;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RenderFluidTankItem implements SpecialModelRenderer<RenderFluidTankItem.TankRenderState> {

    private final Lazy<Vector3fc[]> extents = Lazy.of(() -> ModelUtil.computeExtents(MekanismBlocks.CREATIVE_FLUID_TANK));

    public RenderFluidTankItem() {
    }

    @Override
    public void submit(@Nullable TankRenderState argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (argument == null) {
            return;
        }
        if (argument.fluidScale > 0) {
            int lightToUse = MekanismRenderer.calculateGlowLight(lightCoords, argument.fluidLight);
            RenderResizableCuboid.renderCube(argument.fluidModel(), poseStack, Sheets.translucentBlockSheet(), submitNodeCollector,
                  argument.fluidColor, lightToUse, overlayCoords, RenderResizableCuboid.FaceDisplay.FRONT,
                  Minecraft.getInstance().gameRenderer.getMainCamera().position(), null, argument.fluidTexture);
        }
        argument.blockModelRenderState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (Vector3fc extent : extents.get()) {
            output.accept(extent);
        }
    }

    @Nullable
    @Override
    public TankRenderState extractArgument(ItemStack stack) {
        IMekanismFluidHandler attachment = ContainerType.FLUID.createHandler(stack);
        float fluidScale = 0;
        int fluidLight = 0;
        int fluidColor = 0;
        Model3D fluidModel = null;
        TexturePicker fluidTexture = null;
        if (attachment != null) {
            IFluidTank container = attachment.getContainer(0);
            if (container != null) {
                FluidResource fluidType = container.getResource();
                if (!fluidType.isEmpty()) {
                    FluidStack fluid = fluidType.toStack(container.amountAsInt());
                    fluidScale = (float) container.amountAsInt() / container.capacityAsInt(fluidType);
                    fluidModel = RenderFluidTank.getFluidModel(fluid, fluidScale);
                    fluidLight = fluidType.getFluidType().getLightLevel(fluid);
                    fluidColor = MekanismRenderer.getColorARGB(fluid, fluidScale);
                    fluidTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluidType, MekanismRenderer.FluidTextureType.STILL));
                }
            }
        }
        //todo - 26.1: do this with the block model from model manager (copy Energy cube item)
        BlockState blockState = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
        BlockModelRenderState blockModel = new BlockModelRenderState();
        mc().getBlockModelResolver().update(blockModel, blockState, ModelUtil.BLOCK_DISPLAY_NO_CONTEXT);
        //blockModel.tintLayers().add(tierTint);
        return new TankRenderState(fluidScale, fluidLight, fluidColor, fluidModel, fluidTexture, blockModel);
    }

    public record TankRenderState(float fluidScale, int fluidLight, int fluidColor, @Nullable Model3D fluidModel, @Nullable TexturePicker fluidTexture,
                                  BlockModelRenderState blockModelRenderState) {
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<TankRenderState> {

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        @Nullable
        public RenderFluidTankItem bake(BakingContext context) {
            return new RenderFluidTankItem();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}