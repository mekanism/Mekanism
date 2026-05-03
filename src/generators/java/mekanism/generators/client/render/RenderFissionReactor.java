package mekanism.generators.client.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.LazyModel;
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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    //TODO: Replace using a model here for the glow with using FuelAssemblyBakedModel as it should provide a performance boost
    // The issue and reason it doesn't use it yet is because rendering the coolant hides the FuelAssemblyBakedModel due to
    // transparency sort ordering
    private static final MekanismRenderer.LazyModel glowModel = new LazyModel(() -> new Model3D()
          .xBounds(0.05F, 0.95F)
          .yBounds(0.01F, 0.99F)
          .zBounds(0.05F, 0.95F)
          .setSideRender(direction -> direction.getAxis().isHorizontal())
    );

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
        glowModel.reset();
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
            state.coolantTexture = MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL);
        } else if (multiblock.coolantTank.getCurrentType() == CurrentType.CHEMICAL) {
            ChemicalStack chemicalStack = multiblock.coolantTank.getChemicalTank().getStack();
            state.coolantData = RenderData.Builder.create(chemicalStack).of(multiblock).buildScaled(state.coolantScale);
            state.coolantTexture = MekanismRenderer.getChemicalTexture(chemicalStack);
        }
        if (state.coolantData != null) {
            state.coolantModel = getCoolantModel(state.coolantData);
        }
        if (!multiblock.heatedCoolantTank.isEmpty()) {
            ChemicalStack chemicalStack = multiblock.heatedCoolantTank.getStack();
            state.heatedCoolantData = RenderData.Builder.create(chemicalStack).of(multiblock).build();
            //Create a slightly shrunken version of the model if it is missing to prevent z-fighting
            state.heatedCoolantModel = cachedHeatedCoolantModels.computeIfAbsent(state.heatedCoolantData, d -> ModelRenderer.getModel(d, 1).copy().shrink(0.01F));
            state.heatedCoolantTexture = MekanismRenderer.getChemicalTexture(chemicalStack);
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
            Model3D model = glowModel.get();
            for (FormedAssembly assembly : state.assemblies) {
                BlockPos assemblyPos = assembly.pos();
                poseStack.pushPose();
                poseStack.translate(assemblyPos.getX() - pos.getX(), assemblyPos.getY() - pos.getY(), assemblyPos.getZ() - pos.getZ());
                //Add a bit of extra distance so that it includes the lower part of the control rod
                poseStack.scale(1, assembly.height() + 0.625F, 1);
                RenderResizableCuboid.renderCube(model, poseStack, Sheets.translucentBlockSheet(), nodeCollector, GLOW_ARGB, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(assemblyPos), MekanismRenderer.WHITE_ICON_GETTER);
                poseStack.popPose();
            }
            //profiler.pop();
        }
        //TODO - 26.1 - renderObject
        if (state.coolantData != null && state.coolantModel != null) {
            RenderResizableCuboid.renderObject(camera.pos, state.coolantData.asRenderData(), state.blockPos, state.coolantModel, poseStack, Sheets.translucentBlockSheet(), nodeCollector, OverlayTexture.NO_OVERLAY, state.coolantScale, state.coolantGetter);
        }
        if (state.heatedCoolantData != null && state.heatedCoolantModel != null) {
            RenderResizableCuboid.renderObject(camera.pos, state.heatedCoolantData, state.blockPos, state.heatedCoolantModel, poseStack, Sheets.translucentBlockSheet(), nodeCollector, OverlayTexture.NO_OVERLAY, state.heatedCoolantScale, state.heatedCoolantGetter);
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
        public TextureAtlasSprite coolantTexture;
        public Function<Direction, TextureAtlasSprite> coolantGetter = _ -> coolantTexture;
        @Nullable
        public Model3D coolantModel;
        public float coolantScale;
        @Nullable
        public RenderData heatedCoolantData;
        @Nullable
        public TextureAtlasSprite heatedCoolantTexture;
        public Function<Direction, TextureAtlasSprite> heatedCoolantGetter = _ -> heatedCoolantTexture;
        @Nullable
        public Model3D heatedCoolantModel;
        public float heatedCoolantScale;
    }
}