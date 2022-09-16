package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;

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
            matrix.mulPose(Vector3f.YP.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * BLADE_SPEED % 360));
            matrix.translate(-0.5, -0.5, -0.5);
            Pose entry = matrix.last();
            VertexConsumer bladeBuffer = renderer.getBuffer(Sheets.solidBlockSheet());
            for (BakedQuad quad : MekanismModelCache.INSTANCE.LIQUIFIER_BLADE.getQuads(tile.getLevel().random)) {
                bladeBuffer.putBulkData(entry, quad, 1, 1, 1, light, overlayLight);
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
                matrix.mulPose(Vector3f.YP.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * ROTATE_SPEED % 360));
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, TransformType.GROUND, light, overlayLight, matrix, renderer,
                  MathUtils.clampToInt(tile.getBlockPos().asLong()));
            matrix.popPose();
            if (active && Minecraft.getInstance().options.particles().get() != ParticleStatus.MINIMAL) {
                //Render eating particles
                PseudoParticleData pseudoParticles = particles.computeIfAbsent(tile, t -> new PseudoParticleData());
                if (!Minecraft.getInstance().isPaused()) {
                    //Don't add particles if the game is paused
                    if (pseudoParticles.lastTick != tile.getLevel().getGameTime()) {
                        pseudoParticles.lastTick = tile.getLevel().getGameTime();
                        pseudoParticles.particles.removeIf(PseudoParticle::tick);
                    }
                    int rate = Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED ? 12 : 4;
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
        return cachedModels.computeIfAbsent(ModelRenderer.getStage(paste, stages, fluidScale), stage -> new Model3D()
              .setTexture(MekanismRenderer.getFluidTexture(paste, FluidTextureType.STILL))
              .setSideRender(Direction.DOWN, false)
              .setSideRender(Direction.UP, stage < stages)
              .xBounds(0.001F, 0.999F)
              .yBounds(0.313F, 0.313F + 0.624F * (stage / (float) stages))
              .zBounds(0.001F, 0.999F)
        );
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
            setSize(0.2F, 0.2F);
            this.x = (world.random.nextFloat() - 0.5D) * 0.3D;
            this.y = (world.random.nextFloat() - 0.5D) * 0.3D;
            this.z = (world.random.nextFloat() - 0.5D) * 0.3D;
            this.xo = x;
            this.yo = y;
            this.zo = z;
            this.lifetime = (int) (4.0F / (world.random.nextFloat() * 0.9F + 0.1F));

            this.xd = (Math.random() * 2.0D - 1.0D) * 0.4;
            this.yd = (Math.random() * 2.0D - 1.0D) * 0.4;
            this.zd = (Math.random() * 2.0D - 1.0D) * 0.4;
            float f = (float) (Math.random() + Math.random() + 1.0D) * 0.15F;
            float f1 = (float) Mth.length(xd, yd, zd);
            this.xd = (this.xd / f1) * f * 0.4;
            this.yd = (this.yd / f1) * f * 0.4 + 0.1;
            this.zd = (this.zd / f1) * f * 0.4;

            sprite = Minecraft.getInstance().getItemRenderer().getModel(stack, world, null, 0).getParticleIcon(ModelData.EMPTY);
            this.gravity = 1.0F;
            this.quadSize = 0.1F * (world.random.nextFloat() * 0.5F + 0.5F);
            this.uo = world.random.nextFloat() * 3.0F;
            this.vo = world.random.nextFloat() * 3.0F;

            this.xd *= 0.1;
            this.yd *= 0.1;
            this.zd *= 0.1;
            this.xd += (world.random.nextFloat() - 0.5D) * 0.075;
            this.yd += Math.random() * 0.1D + 0.05D;
            this.zd += (world.random.nextFloat() - 0.5D) * 0.075;
        }

        public boolean tick() {
            if (this.age++ >= this.lifetime || this.y < -0.25) {
                return true;
            }
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
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
            float f = (float) Mth.lerp(partialTicks, this.xo, this.x);
            float f1 = (float) Mth.lerp(partialTicks, this.yo, this.y);
            float f2 = (float) Mth.lerp(partialTicks, this.zo, this.z);
            Quaternion quaternion = Minecraft.getInstance().getEntityRenderDispatcher().camera.rotation();

            //Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
            //vector3f1.transform(quaternion);
            Vector3f[] vectors = {new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F),
                                  new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
            for (int i = 0; i < 4; ++i) {
                Vector3f vector3f = vectors[i];
                vector3f.transform(quaternion);
                vector3f.mul(quadSize);
                vector3f.add(f, f1, f2);
            }

            float f7 = this.getU0();
            float f8 = this.getU1();
            float f5 = this.getV0();
            float f6 = this.getV1();
            buffer.vertex(matrix, vectors[0].x(), vectors[0].y(), vectors[0].z())
                  .uv(f8, f6)
                  .color(0xFF, 0xFF, 0xFF, 0xFF)
                  .uv2(light)
                  .endVertex();
            buffer.vertex(matrix, vectors[1].x(), vectors[1].y(), vectors[1].z())
                  .uv(f8, f5)
                  .color(0xFF, 0xFF, 0xFF, 0xFF)
                  .uv2(light)
                  .endVertex();
            buffer.vertex(matrix, vectors[2].x(), vectors[2].y(), vectors[2].z())
                  .uv(f7, f5)
                  .color(0xFF, 0xFF, 0xFF, 0xFF)
                  .uv2(light)
                  .endVertex();
            buffer.vertex(matrix, vectors[3].x(), vectors[3].y(), vectors[3].z())
                  .uv(f7, f6)
                  .color(0xFF, 0xFF, 0xFF, 0xFF)
                  .uv2(light)
                  .endVertex();
        }

        protected float getU0() {
            return this.sprite.getU((this.uo + 1.0F) / 4.0F * 16.0F);
        }

        protected float getU1() {
            return this.sprite.getU(this.uo / 4.0F * 16.0F);
        }

        protected float getV0() {
            return this.sprite.getV(this.vo / 4.0F * 16.0F);
        }

        protected float getV1() {
            return this.sprite.getV((this.vo + 1.0F) / 4.0F * 16.0F);
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