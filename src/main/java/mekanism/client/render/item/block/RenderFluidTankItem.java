package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.client.ModelUtil;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.RenderResizableCuboid.TexturePicker;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.transfer.ResourceHandler;
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
        if (argument.contentsMaxY > 0) {
            int lightToUse = MekanismRenderer.calculateGlowLight(lightCoords, argument.fluidLight);
            RenderResizableCuboid.renderCube(RenderResizableCuboid.SideRender.NOT_DOWN, RenderFluidTank.CONTENTS_MIN_XZ, RenderFluidTank.CONTENTS_MIN_Y, RenderFluidTank.CONTENTS_MIN_XZ, RenderFluidTank.CONTENTS_MAX_XZ, argument.contentsMaxY, RenderFluidTank.CONTENTS_MAX_XZ, poseStack, Sheets.translucentBlockSheet(), submitNodeCollector,
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
        ResourceHandler<FluidResource> handler = Capabilities.FLUID.getCapability(ItemAccessUtils.sideEffectFreeAccess(stack));
        int fluidLight = 0;
        int fluidColor = 0;
        float contentsMaxY = 0;
        TexturePicker fluidTexture = null;
        if (handler != null) {
            FluidResource fluid = handler.getResource(0);
            if (!fluid.isEmpty()) {
                float fluidScale = (float) handler.getAmountAsLong(0) / handler.getCapacityAsLong(0, fluid);
                contentsMaxY = fluidScale > 0 ? RenderFluidTank.contentsMaxY(fluidScale, MekanismUtils.lighterThanAirGas(fluid)) : 0;
                fluidLight = fluid.getFluidType().getLightLevel();//todo - 26.1: used to be stack, is that important anywhere?
                fluidColor = MekanismRenderer.getColorARGB(fluid, fluidScale);
                fluidTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL));
            }
        }
        //todo - 26.1: do this with the block model from model manager (copy Energy cube item)
        BlockState blockState = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
        BlockModelRenderState blockModel = new BlockModelRenderState();
        mc().getBlockModelResolver().update(blockModel, blockState, ModelUtil.BLOCK_DISPLAY_NO_CONTEXT);
        //blockModel.tintLayers().add(tierTint);
        return new TankRenderState(fluidLight, fluidColor, contentsMaxY, fluidTexture, blockModel);
    }

    public record TankRenderState(int fluidLight, int fluidColor, float contentsMaxY, @Nullable TexturePicker fluidTexture,
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