package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.MultiblockContentsRenderState;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.client.render.RenderFissionReactor.FissionRenderState;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fission.FissionReactorValidator.FormedAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderFissionReactor extends MultiblockTileEntityRenderer<FissionReactorMultiblockData, TileEntityFissionReactorCasing, FissionRenderState> {

    private static final int GLOW_ARGB = ARGB.color(0.6F, 0x76E0EC);
    //TODO: Replace using a model for glow with using FuelAssemblyBakedModel as it should provide a performance boost
    // The issue and reason it doesn't use it yet is because rendering the coolant hides the FuelAssemblyBakedModel due to
    // transparency sort ordering

    public RenderFissionReactor(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FissionRenderState createRenderState() {
        return new FissionRenderState();
    }

    @Override
    public void extractRenderState(TileEntityFissionReactorCasing reactor, FissionRenderState state, float partialTick, Vec3 cameraPosition,
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(reactor, state, partialTick, cameraPosition, breakProgress);
        FissionReactorMultiblockData multiblock = reactor.getMultiblock();
        state.gather(multiblock);
        float heatedCoolantScale = multiblock.prevHeatedCoolantScale;
        float coolantScale = multiblock.prevCoolantScale;

        state.coolantTexture = null;
        state.heatedCoolantTexture = null;
        boolean isGaseous = false;
        if (multiblock.coolantTank.getCurrentType() == CurrentType.FLUID) {
            FluidStack fluid = multiblock.coolantTank.getFluidTank().getFluid();
            state.coolantTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL));
            isGaseous = MekanismUtils.lighterThanAirGas(fluid);
            state.coolantGlow = MekanismRenderer.calculateGlowLight(LightCoordsUtil.FULL_SKY, fluid);
            state.coolantColor = MekanismRenderer.getColorARGB(fluid, coolantScale);
        } else if (multiblock.coolantTank.getCurrentType() == CurrentType.CHEMICAL) {
            ChemicalStack chemicalStack = multiblock.coolantTank.getChemicalTank().getStack();
            state.coolantTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getChemicalTexture(chemicalStack));
            isGaseous = chemicalStack.is(MekanismAPITags.Chemicals.GASEOUS);
            state.coolantGlow = LightCoordsUtil.FULL_SKY;//todo not fullbright chemicals?
            state.coolantColor = MekanismRenderer.getColorARGB(chemicalStack, coolantScale);
        }
        if (state.coolantTexture != null) {
            state.coolantMaxY = ModelRenderer.getMaxY(state.height, coolantScale, isGaseous);
        }
        if (!multiblock.heatedCoolantTank.isEmpty()) {
            ChemicalStack chemicalStack = multiblock.heatedCoolantTank.getStack();
            state.heatedCoolantTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getChemicalTexture(chemicalStack));
            state.heatedCoolantMaxY = ModelRenderer.getMaxY(state.height, heatedCoolantScale, chemicalStack.is(MekanismAPITags.Chemicals.GASEOUS));
            state.heatedCoolantColor = MekanismRenderer.getColorARGB(chemicalStack, heatedCoolantScale);
        }

        if (multiblock.isBurning()) {
            //TODO - 26.1: Do we need to copy this like this?
            state.assemblies.addAll(multiblock.assemblies);
        }
    }

    @Override
    public void submit(FissionRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        BlockPos pos = state.blockPos;
        if (!state.assemblies.isEmpty()) {
            //TODO - 26.1: Profiler?
            //profiler.push(GeneratorsProfilerConstants.FISSION_FUEL_ASSEMBLY);
            for (FormedAssembly assembly : state.assemblies) {
                BlockPos assemblyPos = assembly.pos();
                poseStack.pushPose();
                poseStack.translate(assemblyPos.getX() - pos.getX(), assemblyPos.getY() - pos.getY(), assemblyPos.getZ() - pos.getZ());
                //Add a bit of extra distance so that it includes the lower part of the control rod
                poseStack.scale(1, assembly.height() + 0.625F, 1);
                RenderResizableCuboid.renderCube(RenderResizableCuboid.SideRender.HORIZONTAL, 0.05F, 0.01F, 0.05F, 0.95F, 0.99F, 0.95F, poseStack, Sheets.translucentBlockSheet(), nodeCollector, GLOW_ARGB, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(assemblyPos), MekanismRenderer.WHITE_ICON_GETTER);
                poseStack.popPose();
            }
            //profiler.pop();
        }
        if (state.coolantTexture != null) {
            RenderResizableCuboid.renderObject(camera.pos, poseStack, Sheets.translucentBlockSheet(), nodeCollector, RenderResizableCuboid.SideRender.ALL_FACES, 0.01F, 0.01F, 0.01F, state.length - 0.02F, state.coolantMaxY, state.width - 0.02F, state.coolantTexture, OverlayTexture.NO_OVERLAY, state.coolantGlow, state.coolantColor, state.blockPos, state.renderLocation, state.length, state.width, state.height);
        }
        if (state.heatedCoolantTexture != null) {
            //uses a slightly shrunken version of the model to prevent z-fighting
            RenderResizableCuboid.renderObject(camera.pos, poseStack, Sheets.translucentBlockSheet(), nodeCollector, RenderResizableCuboid.SideRender.ALL_FACES, 0.02F, 0.02F, 0.02F, state.length - 0.03F, state.heatedCoolantMaxY, state.width - 0.03F, state.heatedCoolantTexture, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_SKY, state.heatedCoolantColor, state.blockPos, state.renderLocation, state.length, state.width, state.height);
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.FISSION_REACTOR;
    }

    public static class FissionRenderState extends MultiblockContentsRenderState {

        public List<FormedAssembly> assemblies = new ArrayList<>();

        @Nullable
        public RenderResizableCuboid.TexturePicker coolantTexture;
        public float coolantMaxY;
        public int coolantGlow;
        public int coolantColor;

        @Nullable
        public RenderResizableCuboid.TexturePicker heatedCoolantTexture;
        public float heatedCoolantMaxY;
        public int heatedCoolantColor;
    }
}