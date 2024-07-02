package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NothingNullByDefault
public class RenderNutritionalLiquifier extends MekanismTileEntityRenderer<TileEntityNutritionalLiquifier> {

    private static final Int2ObjectMap<Model3D> cachedModels = new Int2ObjectOpenHashMap<>();
    private static final Map<TileEntityNutritionalLiquifier, PseudoParticleData> particles = new WeakHashMap<>();
    private static final int stages = 40;
    private static final float BLADE_SPEED = 25F;
    private static final float ROTATE_SPEED = 10F;

    public static void resetCachedModels() {
        cachedModels.clear();
    }

    public RenderNutritionalLiquifier(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityNutritionalLiquifier tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        if (!tile.fluidTank.isEmpty()) {
            FluidStack paste = tile.fluidTank.getFluid();
            float fluidScale = paste.getAmount() / (float) tile.fluidTank.getCapacity();
            MekanismRenderer.renderObject(getPasteModel(paste, fluidScale), matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()),
                  MekanismRenderer.getColorARGB(paste, fluidScale), light, overlayLight, FaceDisplay.FRONT, getCamera(), tile.getBlockPos());
        }
        boolean active = tile.getActive();
        if (active) {
            //Render the blade at the correct rotation if we are active
            matrix.pushPose();
            matrix.translate(0.5, 0.5, 0.5);
            matrix.mulPose(Axis.YP.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * BLADE_SPEED % 360));
            matrix.translate(-0.5, -0.5, -0.5);
            Pose entry = matrix.last();
            VertexConsumer bladeBuffer = renderer.getBuffer(Sheets.solidBlockSheet());
            for (BakedQuad quad : MekanismModelCache.INSTANCE.LIQUIFIER_BLADE.getQuads(tile.getLevel().random)) {
                bladeBuffer.putBulkData(entry, quad, 1, 1, 1, 1, light, overlayLight);
            }
            matrix.popPose();
        }
        //Render the item and particle
        ItemStack stack = tile.getRenderStack();
        if (!stack.isEmpty()) {
            matrix.pushPose();
            matrix.translate(0.5, 0.6, 0.5);
            if (active) {
                //Make the item rotate if the liquifier is active
                matrix.mulPose(Axis.YP.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * ROTATE_SPEED % 360));
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, overlayLight, matrix, renderer, tile.getLevel(),
                  MathUtils.clampToInt(tile.getBlockPos().asLong()));
            matrix.popPose();
            if (active && Minecraft.getInstance().options.particles().get() != ParticleStatus.MINIMAL) {
                //Render eating particles
                PseudoParticleData pseudoParticles = particles.computeIfAbsent(tile, t -> new PseudoParticleData());
                if (isTickingNormally(tile)) {
                    //Don't add particles if the game is paused
                    if (pseudoParticles.lastTick != tile.getLevel().getGameTime()) {
                        pseudoParticles.lastTick = tile.getLevel().getGameTime();
                        pseudoParticles.particles.removeIf(PseudoParticle::tick);
                    }
                    int rate = Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED ? 10 : 3;
                    if (tile.getLevel().getGameTime() % rate == 0) {
                        pseudoParticles.particles.add(new PseudoParticle(tile.getLevel(), stack));
                    }
                }
                //Render particles
                VertexConsumer buffer = renderer.getBuffer(MekanismRenderType.NUTRITIONAL_PARTICLE);
                matrix.pushPose();
                matrix.translate(0.5, 0.55, 0.5);
                Matrix4f matrix4f = matrix.last().pose();
                for (PseudoParticle particle : pseudoParticles.particles) {
                    particle.render(matrix4f, buffer, partialTick, light);
                }
                matrix.popPose();
            } else {
                particles.remove(tile);
            }
        }
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
                  .setTexture(MekanismRenderer.getFluidTexture(paste, FluidTextureType.STILL))
                  .setSideRender(Direction.DOWN, false)
                  .setSideRender(Direction.UP, stage < stages)
                  .xBounds(0.001F, 0.999F)
                  .yBounds(0.313F, 0.313F + 0.624F * (stage / (float) stages))
                  .zBounds(0.001F, 0.999F);
            cachedModels.put(stage, model);
        }
        return model;
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

        protected PseudoParticle(Level world, ItemStack stack) {
            //Particle Constructor
            setSize(0.2F, 0.2F);
            this.x = (world.random.nextFloat() - 0.5D) * 0.3D;
            this.y = (world.random.nextFloat() - 0.5D) * 0.3D;
            this.z = (world.random.nextFloat() - 0.5D) * 0.3D;
            this.xo = x;
            this.yo = y;
            this.zo = z;
            this.lifetime = (int) (4.0F / (world.random.nextFloat() * 0.9F + 0.1F));

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
            BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, world, null, 0);
            BakedModel override = model.getOverrides().resolve(model, stack, world instanceof ClientLevel level ? level : null, null, 0);
            if (override != null) {
                model = override;
            }
            sprite = model.getParticleIcon(ModelData.EMPTY);
            this.gravity = 1.0F;
            this.quadSize = 0.1F * (world.random.nextFloat() * 0.5F + 0.5F);
            this.uo = world.random.nextFloat() * 3.0F;
            this.vo = world.random.nextFloat() * 3.0F;

            //BreakingItemParticle Constructor that takes speed
            this.xd *= 0.1;
            this.yd *= 0.1;
            this.zd *= 0.1;
            this.xd += (world.random.nextFloat() - 0.5D) * 0.075;
            this.yd += Math.random() * 0.1D + 0.05D;
            this.zd += (world.random.nextFloat() - 0.5D) * 0.075;
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

        public void render(Matrix4f matrix, VertexConsumer buffer, float partialTicks, int light) {
            Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
            //From SingleQuadParticle#render
            Quaternionf quaternion = new Quaternionf();
            SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ.setRotation(quaternion, camera, partialTicks);

            //From SingleQuadParticle#renderRotatedQuad
            float f = (float) Mth.lerp(partialTicks, this.xo, this.x);
            float f1 = (float) Mth.lerp(partialTicks, this.yo, this.y);
            float f2 = (float) Mth.lerp(partialTicks, this.zo, this.z);
            renderRotatedQuad(matrix, buffer, quaternion, f, f1, f2, light);
        }

        //Copy of SingleQuadParticle#renderRotatedQuad
        protected void renderRotatedQuad(Matrix4f matrix,VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, int light) {
            float minU = this.getU0();
            float maxU = this.getU1();
            float minV = this.getV0();
            float maxV = this.getV1();
            this.renderVertex(matrix, buffer, quaternion, x, y, z, 1.0F, -1.0F, maxU, maxV, light);
            this.renderVertex(matrix, buffer, quaternion, x, y, z, 1.0F, 1.0F, maxU, minV, light);
            this.renderVertex(matrix, buffer, quaternion, x, y, z, -1.0F, 1.0F, minU, minV, light);
            this.renderVertex(matrix, buffer, quaternion, x, y, z, -1.0F, -1.0F, minU, maxV, light);
        }

        //Copy of SingleQuadParticle#renderVertex
        private void renderVertex(Matrix4f matrix, VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float xOffset, float yOffset, float u,
              float v, int light) {
            Vector3f vector3f = new Vector3f(xOffset, yOffset, 0.0F).rotate(quaternion).mul(quadSize).add(x, y, z);
            buffer.addVertex(matrix, vector3f.x(), vector3f.y(), vector3f.z())
                  .setUv(u, v)
                  .setColor(0xFF, 0xFF, 0xFF, 0xFF)
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