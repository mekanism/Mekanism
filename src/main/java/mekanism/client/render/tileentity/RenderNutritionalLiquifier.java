package mekanism.client.render.tileentity;

import com.google.common.primitives.Ints;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.tileentity.RenderNutritionalLiquifier.LiquifierRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.SingleQuadParticle.FacingCameraMode;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NothingNullByDefault
public class RenderNutritionalLiquifier extends MekanismTileEntityRenderer<TileEntityNutritionalLiquifier, LiquifierRenderState> {

    private static final Int2ObjectMap<Model3D> cachedModels = new Int2ObjectOpenHashMap<>();
    private static final Map<TileEntityNutritionalLiquifier, PseudoParticleData> particles = new WeakHashMap<>();
    private static final int stages = 40;
    private static final float BLADE_SPEED = 25F;
    private static final float ROTATE_SPEED = 10F;

    public static void resetCachedModels() {
        cachedModels.clear();
    }

    private final ItemModelResolver itemModelResolver;

    public RenderNutritionalLiquifier(Context context) {
        super(context);
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public LiquifierRenderState createRenderState() {
        return new LiquifierRenderState();
    }

    @Override
    public void extractRenderState(TileEntityNutritionalLiquifier liquifier, LiquifierRenderState state, float partialTick, Vec3 cameraPosition,
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(liquifier, state, partialTick, cameraPosition, breakProgress);
        Level level = liquifier.getLevel();
        if (!liquifier.fluidTank.isEmpty()) {
            FluidResource paste = liquifier.fluidTank.getResource();
            FluidStack pasteStack = paste.toStack(liquifier.fluidTank.amountAsInt());
            float fluidScale = liquifier.fluidTank.amountAsInt() / (float) liquifier.fluidTank.capacityAsInt(paste);
            state.pasteTint = MekanismRenderer.getColorARGB(pasteStack, fluidScale);
            state.pasteModel = getPasteModel(pasteStack, fluidScale);
            state.pasteTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(paste, FluidTextureType.STILL));
        } else {
            state.pasteModel = null;
        }
        state.active = liquifier.getActive();
        if (state.active) {
            long gameTime = level.getGameTime();
            state.bladeRotation = ((gameTime + partialTick) * BLADE_SPEED) % 360;
            state.itemRotation = ((gameTime + partialTick) * ROTATE_SPEED) % 360;
        }
        ItemStack stack = liquifier.getRenderStack();
        if (!stack.isEmpty()) {
            //TODO - 26.1: Evaluate the seed we are passing, and if we want to use this as the seed for transporters or if maybe we should be using zero here as well?
            int seed = Ints.saturatedCast(state.blockPos.asLong());
            this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GROUND, level, null, seed);
        }
    }

    @Override
    public void submit(LiquifierRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.pasteModel != null) {
            RenderResizableCuboid.renderCube(state.pasteModel, poseStack, Sheets.translucentBlockSheet(), nodeCollector, state.pasteTint, state.lightCoords, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(state.blockPos), state.pasteTexture);
        }
        //TODO - 26.1: rendering
        /*if (state.active) {
            //Render the blade at the correct rotation if we are active
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.bladeRotation));
            poseStack.translate(-0.5, -0.5, -0.5);
            nodeCollector.submitModel(
                  MekanismModelCache.INSTANCE.LIQUIFIER_BLADE.getBakedModel(),
                  Unit.INSTANCE,
                  poseStack,
                  Sheets.solidBlockSheet(),
                  state.lightCoords,
                  OverlayTexture.NO_OVERLAY,
                  0,//No outline
                  state.breakProgress
            );
            poseStack.popPose();
        }
        //Render the item and particle
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.6, 0.5);
            if (state.active) {
                //Make the item rotate if the liquifier is active
                poseStack.mulPose(Axis.YP.rotationDegrees(state.itemRotation));
            }
            state.item.submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
            if (state.active && Minecraft.getInstance().options.particles().get() != ParticleStatus.MINIMAL) {
                //TODO - 26.1: Can this be transitioned to being a nodeCollector.submitParticleGroup call?
                //Render eating particles
                PseudoParticleData pseudoParticles = particles.computeIfAbsent(tile, t -> new PseudoParticleData());
                if (isTickingNormally(tile)) {
                    //Don't add particles if the game is paused
                    if (pseudoParticles.lastTick != gameTime) {
                        pseudoParticles.lastTick = gameTime;
                        pseudoParticles.particles.removeIf(PseudoParticle::tick);
                    }
                    int rate = Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED ? 10 : 3;
                    if (gameTime % rate == 0) {
                        pseudoParticles.particles.add(new PseudoParticle(state.item, tile.getLevel().random));
                    }
                }
                //Render particles
                VertexConsumer buffer = renderer.getBuffer(MekanismRenderType.NUTRITIONAL_PARTICLE);
                poseStack.pushPose();
                poseStack.translate(0.5, 0.55, 0.5);
                Matrix4f matrix4f = poseStack.last().pose();
                for (PseudoParticle particle : pseudoParticles.particles) {
                    particle.render(matrix4f, buffer, partialTick, state.lightCoords);
                }
                poseStack.popPose();
            } else {
                particles.remove(tile);
            }
        }*/
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.NUTRITIONAL_LIQUIFIER;
    }

    private Model3D getPasteModel(FluidStack paste, float fluidScale) {
        int stage = ModelRenderer.getStage(paste, stages, fluidScale);
        Model3D model = cachedModels.get(stage);
        if (model == null) {
            model = new Model3D()
                  .setSideRender(Direction.DOWN, false)
                  .setSideRender(Direction.UP, stage < stages)
                  .xBounds(0.001F, 0.999F)
                  .yBounds(0.313F, 0.313F + 0.624F * (stage / (float) stages))
                  .zBounds(0.001F, 0.999F);
            cachedModels.put(stage, model);
        }
        return model;
    }

    public static class LiquifierRenderState extends BlockEntityRenderState {

        public final ItemStackRenderState item = new ItemStackRenderState();
        public float bladeRotation;
        public float itemRotation;
        public boolean active;
        @Nullable
        public Model3D pasteModel;
        public int pasteTint = 0xFFFFFFFF;
        @Nullable
        public RenderResizableCuboid.TexturePicker pasteTexture;
    }

    private static class PseudoParticleData {

        private final List<PseudoParticle> particles = new ArrayList<>();
        private long lastTick;
    }

    private static class PseudoParticle {

        private static final AABB INITIAL_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

        private final TextureAtlasSprite sprite;
        private final float quadSize;
        private final float uo;
        private final float vo;
        protected double xo;
        protected double yo;
        protected double zo;
        protected double x;
        protected double y;
        protected double z;
        protected double xd;
        protected double yd;
        protected double zd;
        protected int lifetime;
        protected int age;
        protected float gravity;

        private AABB bb = INITIAL_AABB;
        protected float bbWidth = 0.6F;
        protected float bbHeight = 1.8F;

        protected PseudoParticle(ItemStackRenderState item, RandomSource random) {
            //Particle Constructor
            setSize(0.2F, 0.2F);
            this.x = (random.nextFloat() - 0.5D) * 0.3D;
            this.y = (random.nextFloat() - 0.5D) * 0.3D;
            this.z = (random.nextFloat() - 0.5D) * 0.3D;
            this.xo = x;
            this.yo = y;
            this.zo = z;
            this.lifetime = (int) (4.0F / (random.nextFloat() * 0.9F + 0.1F));

            //Particle Constructor that takes speed
            this.xd = (Math.random() * 2.0D - 1.0D) * 0.4;
            this.yd = (Math.random() * 2.0D - 1.0D) * 0.4;
            this.zd = (Math.random() * 2.0D - 1.0D) * 0.4;
            float f = (float) (Math.random() + Math.random() + 1.0D) * 0.15F;
            float f1 = (float) Mth.length(xd, yd, zd);
            this.xd = (this.xd / f1) * f * 0.4;
            this.yd = (this.yd / f1) * f * 0.4 + 0.1;
            this.zd = (this.zd / f1) * f * 0.4;

            //BreakingItemParticle Constructor
            Baked itemSprite = item.pickParticleMaterial(random);
            sprite = itemSprite == null ? Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).missingSprite() : itemSprite.sprite();
            this.gravity = 1.0F;
            this.quadSize = 0.1F * (random.nextFloat() * 0.5F + 0.5F);
            this.uo = random.nextFloat() * 3.0F;
            this.vo = random.nextFloat() * 3.0F;

            //BreakingItemParticle Constructor that takes speed
            this.xd *= 0.1;
            this.yd *= 0.1;
            this.zd *= 0.1;
            this.xd += (random.nextFloat() - 0.5D) * 0.075;
            this.yd += Math.random() * 0.1D + 0.05D;
            this.zd += (random.nextFloat() - 0.5D) * 0.075;
        }

        public boolean tick() {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            if (this.age++ >= this.lifetime || this.y < -0.25) {
                return true;
            }
            this.yd -= 0.04D * this.gravity;
            if (this.xd != 0.0D || this.yd != 0.0D || this.zd != 0.0D) {
                bb = bb.move(this.xd, this.yd, this.zd);
                this.x = (bb.minX + bb.maxX) / 2.0D;
                this.y = bb.minY;
                this.z = (bb.minZ + bb.maxZ) / 2.0D;
            }
            this.xd *= 0.98;
            this.yd *= 0.98;
            this.zd *= 0.98;
            return false;
        }

        public void render(Matrix4f poseStack, VertexConsumer buffer, float partialTicks, int light) {
            Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
            //From SingleQuadParticle#render
            Quaternionf quaternion = new Quaternionf();
            FacingCameraMode.LOOKAT_XYZ.setRotation(quaternion, camera, partialTicks);

            //From SingleQuadParticle#renderRotatedQuad
            float f = (float) Mth.lerp(partialTicks, this.xo, this.x);
            float f1 = (float) Mth.lerp(partialTicks, this.yo, this.y);
            float f2 = (float) Mth.lerp(partialTicks, this.zo, this.z);
            renderRotatedQuad(poseStack, buffer, quaternion, f, f1, f2, light);
        }

        //Copy of SingleQuadParticle#renderRotatedQuad
        protected void renderRotatedQuad(Matrix4f poseStack, VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, int light) {
            float minU = this.getU0();
            float maxU = this.getU1();
            float minV = this.getV0();
            float maxV = this.getV1();
            this.renderVertex(poseStack, buffer, quaternion, x, y, z, 1.0F, -1.0F, maxU, maxV, light);
            this.renderVertex(poseStack, buffer, quaternion, x, y, z, 1.0F, 1.0F, maxU, minV, light);
            this.renderVertex(poseStack, buffer, quaternion, x, y, z, -1.0F, 1.0F, minU, minV, light);
            this.renderVertex(poseStack, buffer, quaternion, x, y, z, -1.0F, -1.0F, minU, maxV, light);
        }

        //Copy of SingleQuadParticle#renderVertex
        private void renderVertex(Matrix4f poseStack, VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float xOffset, float yOffset, float u,
              float v, int light) {
            Vector3f vector3f = new Vector3f(xOffset, yOffset, 0.0F).rotate(quaternion).mul(quadSize).add(x, y, z);
            buffer.addVertex(poseStack, vector3f.x(), vector3f.y(), vector3f.z())
                  .setUv(u, v)
                  .setColor(0xFFFFFFFF)
                  .setLight(light);
        }

        protected float getU0() {
            return this.sprite.getU((this.uo + 1.0F) / 4.0F);
        }

        protected float getU1() {
            return this.sprite.getU(this.uo / 4.0F);
        }

        protected float getV0() {
            return this.sprite.getV(this.vo / 4.0F);
        }

        protected float getV1() {
            return this.sprite.getV((this.vo + 1.0F) / 4.0F);
        }

        protected void setSize(float particleWidth, float particleHeight) {
            if (particleWidth != this.bbWidth || particleHeight != this.bbHeight) {
                this.bbWidth = particleWidth;
                this.bbHeight = particleHeight;
                double d0 = (bb.minX + bb.maxX - particleWidth) / 2.0D;
                double d1 = (bb.minZ + bb.maxZ - particleWidth) / 2.0D;
                bb = new AABB(d0, bb.minY, d1, d0 + this.bbWidth, bb.minY + this.bbHeight, d1 + this.bbWidth);
            }
        }
    }
}