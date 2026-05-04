package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.tier.BaseTier;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.RenderResizableCuboid.TexturePicker;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RenderFluidTankItem implements SpecialModelRenderer<RenderFluidTankItem.TankRenderState> {

    private final BlockStateModelPart fluidTankmodel;
    private final int tierTint;

    public RenderFluidTankItem(BlockStateModelPart fluidTankmodel, int tierTint) {
        this.fluidTankmodel = fluidTankmodel;
        this.tierTint = tierTint;
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
        if (fluidTankmodel instanceof SimpleModelWrapper simpleModelWrapper) {
            CuboidItemModelWrapper.computeExtents(simpleModelWrapper.getQuads(null));
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
            FluidStack fluid = attachment.getFluidInTank(0);
            if (!fluid.isEmpty()) {
                fluidScale = (float) fluid.amount() / attachment.getTankCapacity(0);
                fluidModel = RenderFluidTank.getFluidModel(fluid, fluidScale);
                fluidLight = fluid.getFluidType().getLightLevel(fluid);
                fluidColor = MekanismRenderer.getColorARGB(fluid, fluidScale);
                fluidTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL));
            }
        }
        BlockModelRenderState blockModel = new BlockModelRenderState();
        blockModel.setupModel(new Matrix4f(), true).add(fluidTankmodel);
        blockModel.tintLayers().add(tierTint);
        return new TankRenderState(fluidScale, fluidLight, fluidColor, fluidModel, fluidTexture, blockModel);
    }

    public record TankRenderState(float fluidScale, int fluidLight, int fluidColor, @Nullable Model3D fluidModel, @Nullable TexturePicker fluidTexture,
                                  BlockModelRenderState blockModelRenderState) {
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<TankRenderState> {

        public static final MapCodec<Unbaked> MAP_CODEC = BaseTier.CODEC.fieldOf("tank_tier").xmap(Unbaked::new, Unbaked::getTier);

        private final BaseTier tier;

        public Unbaked(BaseTier tier) {
            this.tier = tier;
        }

        public BaseTier getTier() {
            return tier;
        }

        @Override
        @Nullable
        public RenderFluidTankItem bake(BakingContext context) {
            if (!(context instanceof ItemModel.BakingContext itemBaking)) {
                Mekanism.logger.error("RenderFluidTankItem called with non-item baking context: {}", context);
                return null;
            }
            ModelBaker modelBaker = itemBaking.blockModelBaker();
            ResolvedModel model = modelBaker.getModel(Mekanism.rl("block/fluid_tank"));
            BlockStateModelPart fluidTank = SimpleModelWrapper.bake(modelBaker, model, BlockModelRotation.get(OctahedralGroup.IDENTITY));
            return new RenderFluidTankItem(fluidTank, tier.getPackedColor());
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}