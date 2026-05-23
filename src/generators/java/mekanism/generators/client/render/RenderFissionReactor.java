package mekanism.generators.client.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.data.RenderData.ScaledRenderData;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.generators.client.render.RenderFissionReactor.FissionRenderState;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fission.FissionReactorValidator.FormedAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
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

    private static final Map<RenderData, Model3D> cachedHeatedCoolantModels = new Object2ObjectOpenHashMap<>();
    private static final Cache<ScaledRenderData, Model3D> cachedCoolantModels = CacheBuilder.newBuilder().maximumSize(10).expireAfterAccess(5, TimeUnit.MINUTES).build();
    private static final int GLOW_ARGB = ARGB.color(0.6F, 0x76E0EC);
    //TODO: Replace using a model for glow with using FuelAssemblyBakedModel as it should provide a performance boost
    // The issue and reason it doesn't use it yet is because rendering the coolant hides the FuelAssemblyBakedModel due to
    // transparency sort ordering

    private static Model3D getCoolantModel(ScaledRenderData renderData) {
        Model3D model = cachedCoolantModels.getIfPresent(renderData);
        if (model == null) {
            model = ModelRenderer.getModel(renderData.asRenderData(), renderData.scale());
            cachedCoolantModels.put(renderData, model);
        }
        return model;
    }

    public static void resetCachedModels() {
        cachedHeatedCoolantModels.clear();
    }

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
        state.heatedCoolantScale = multiblock.prevHeatedCoolantScale;
        state.coolantScale = multiblock.prevCoolantScale;

        state.coolantTexture = null;
        state.heatedCoolantTexture = null;
        if (multiblock.coolantTank.getCurrentType() == CurrentType.FLUID) {
            FluidStack fluid = multiblock.coolantTank.getFluidTank().getFluid();
            state.coolantData = RenderData.Builder.create(fluid).of(multiblock).buildScaled(state.coolantScale);
            state.coolantTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL));
        } else if (multiblock.coolantTank.getCurrentType() == CurrentType.CHEMICAL) {
            ChemicalStack chemicalStack = multiblock.coolantTank.getChemicalTank().getStack();
            state.coolantData = RenderData.Builder.create(chemicalStack).of(multiblock).buildScaled(state.coolantScale);
            state.coolantTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getChemicalTexture(chemicalStack));
        }
        if (state.coolantData != null) {
            state.coolantModel = getCoolantModel(state.coolantData);
        }
        if (!multiblock.heatedCoolantTank.isEmpty()) {
            ChemicalStack chemicalStack = multiblock.heatedCoolantTank.getStack();
            state.heatedCoolantData = RenderData.Builder.create(chemicalStack).of(multiblock).build();
            //Create a slightly shrunken version of the model if it is missing to prevent z-fighting
            state.heatedCoolantModel = cachedHeatedCoolantModels.computeIfAbsent(state.heatedCoolantData, d -> ModelRenderer.getModel(d, 1).copy().shrink(0.01F));
            state.heatedCoolantTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getChemicalTexture(chemicalStack));
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
                RenderResizableCuboid.renderCube(MekanismRenderer.TMP_SideRenderCheck.HORIZONTAL, 0.05F, 0.01F, 0.05F, 0.95F, 0.99F, 0.95F, poseStack, Sheets.translucentBlockSheet(), nodeCollector, GLOW_ARGB, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(assemblyPos), MekanismRenderer.WHITE_ICON_GETTER);
                poseStack.popPose();
            }
            //profiler.pop();
        }
        if (state.coolantData != null && state.coolantModel != null) {
            RenderResizableCuboid.renderObject(camera.pos, poseStack, Sheets.translucentBlockSheet(), nodeCollector, state.coolantModel, state.coolantModel.minX, state.coolantModel.minY, state.coolantModel.minZ, state.coolantModel.maxX, state.coolantModel.maxY, state.coolantModel.maxZ, state.coolantTexture, OverlayTexture.NO_OVERLAY, state.coolantData.asRenderData().calculateGlowLight(LightCoordsUtil.FULL_SKY), state.coolantData.asRenderData().getColorARGB(state.coolantScale), state.blockPos, state.coolantData.asRenderData().location, state.coolantData.asRenderData().length, state.coolantData.asRenderData().width, state.coolantData.asRenderData().height);
        }
        if (state.heatedCoolantData != null && state.heatedCoolantModel != null) {
            RenderResizableCuboid.renderObject(camera.pos, poseStack, Sheets.translucentBlockSheet(), nodeCollector, state.heatedCoolantModel, state.heatedCoolantModel.minX, state.heatedCoolantModel.minY, state.heatedCoolantModel.minZ, state.heatedCoolantModel.maxX, state.heatedCoolantModel.maxY, state.heatedCoolantModel.maxZ, state.heatedCoolantTexture, OverlayTexture.NO_OVERLAY, state.heatedCoolantData.calculateGlowLight(LightCoordsUtil.FULL_SKY), state.heatedCoolantData.getColorARGB(state.heatedCoolantScale), state.blockPos, state.heatedCoolantData.location, state.heatedCoolantData.length, state.heatedCoolantData.width, state.heatedCoolantData.height);
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.FISSION_REACTOR;
    }

    public static class FissionRenderState extends BlockEntityRenderState {

        public List<FormedAssembly> assemblies = new ArrayList<>();
        @Nullable
        public ScaledRenderData coolantData;
        @Nullable
        public RenderResizableCuboid.TexturePicker coolantTexture;
        @Nullable
        public Model3D coolantModel;
        public float coolantScale;
        @Nullable
        public RenderData heatedCoolantData;
        @Nullable
        public RenderResizableCuboid.TexturePicker heatedCoolantTexture;
        @Nullable
        public Model3D heatedCoolantModel;
        public float heatedCoolantScale;
    }
}