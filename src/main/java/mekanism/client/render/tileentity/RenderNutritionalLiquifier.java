package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
public class RenderNutritionalLiquifier extends MekanismTileEntityRenderer<TileEntityNutritionalLiquifier> {

    //Expect size of one as it is likely to just be nutritional paste, but support more in case the recipe system changes
    private static final Map<Gas, Model3D> cachedModels = new HashMap<>(1);
    private static final Map<TileEntityNutritionalLiquifier, PseudoParticleData> particles = new WeakHashMap<>();
    private static final float BLADE_SPEED = 25F;
    private static final float ROTATE_SPEED = 10F;

    public static void resetCachedModels() {
        cachedModels.clear();
    }

    public RenderNutritionalLiquifier(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityNutritionalLiquifier tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        if (!tile.gasTank.isEmpty()) {
            GasStack paste = tile.gasTank.getStack();
            float gasScale = paste.getAmount() / (float) tile.gasTank.getCapacity();
            MekanismRenderer.renderObject(getPasteModel(paste), matrix, renderer.getBuffer(Atlases.translucentCullBlockSheet()),
                  MekanismRenderer.getColorARGB(paste, gasScale, true), light, overlayLight, FaceDisplay.FRONT);
        }
        boolean active = tile.getActive();
        matrix.pushPose();
        if (active) {
            matrix.translate(0.5, 0.5, 0.5);
            matrix.mulPose(Vector3f.YP.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * BLADE_SPEED % 360));
            matrix.translate(-0.5, -0.5, -0.5);
        }
        Entry entry = matrix.last();
        IVertexBuilder bladeBuffer = renderer.getBuffer(Atlases.solidBlockSheet());
        for (BakedQuad quad : MekanismModelCache.INSTANCE.LIQUIFIER_BLADE.getBakedModel().getQuads(null, null, tile.getLevel().random)) {
            bladeBuffer.addVertexData(entry, quad, 1F, 1F, 1F, 1F, light, overlayLight);
        }
        matrix.popPose();
        //Render the item and particle
        ItemStack stack = tile.getRenderStack();
        if (!stack.isEmpty()) {
            matrix.pushPose();
            matrix.translate(0.5, 0.6, 0.5);
            if (active) {
                //Make the item rotate if the liquifier is active
                matrix.mulPose(Vector3f.YP.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * ROTATE_SPEED % 360));
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, TransformType.GROUND, light, overlayLight, matrix, renderer);
            matrix.popPose();
            if (active && Minecraft.getInstance().options.particles != ParticleStatus.MINIMAL) {
                //Render eating particles
                PseudoParticleData pseudoParticles = particles.computeIfAbsent(tile, t -> new PseudoParticleData());
                if (!Minecraft.getInstance().isPaused()) {
                    //Don't add particles if the game is paused
                    if (pseudoParticles.lastTick != tile.getLevel().getGameTime()) {
                        pseudoParticles.lastTick = tile.getLevel().getGameTime();
                        pseudoParticles.particles.removeIf(PseudoParticle::tick);
                    }
                    int rate = Minecraft.getInstance().options.particles == ParticleStatus.DECREASED ? 12 : 4;
                    if (tile.getLevel().getGameTime() % rate == 0) {
                        pseudoParticles.particles.add(new PseudoParticle(tile.getLevel(), stack));
                        //TODO - 1.18: Try using this instead of our pseudo particles as maybe it will be able to render properly
                        // behind translucent glass and our contents in fast
                        /*Vector3d motion = new Vector3d((world.random.nextFloat() - 0.5D) * 0.075D,
                              world.random.nextDouble() * 0.1D,
                              (world.random.nextFloat() - 0.5D) * 0.075D);
                        Vector3d position = new Vector3d((world.random.nextFloat() - 0.5D) * 0.3D,
                              (world.random.nextFloat() - 0.5D) * 0.3D,
                              (world.random.nextFloat() - 0.5D) * 0.3D);
                        position = position.add(pos.getX() + 0.5, pos.getY() + 0.55, pos.getZ() + 0.5);
                        world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), position.x, position.y, position.z, motion.x, motion.y + 0.05D, motion.z);*/
                    }
                }
                //Render particles
                IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.nutritionalParticle());
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

    private Model3D getPasteModel(@Nonnull GasStack stack) {
        return cachedModels.computeIfAbsent(stack.getType(), gas -> {
            Model3D model = new Model3D();
            model.setTexture(MekanismRenderer.getChemicalTexture(gas));
            model.minX = 0.001F;
            model.minY = 0.313F;
            model.minZ = 0.001F;

            model.maxX = 0.999F;
            model.maxY = 0.937F;
            model.maxZ = 0.999F;
            return model;
        });
    }

    private static class PseudoParticleData {

        private final List<PseudoParticle> particles = new ArrayList<>();
        private long lastTick;
    }

    private static class PseudoParticle {

        private static final AxisAlignedBB INITIAL_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

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

        private AxisAlignedBB bb = INITIAL_AABB;
        protected float bbWidth = 0.6F;
        protected float bbHeight = 1.8F;

        protected PseudoParticle(World world, ItemStack stack) {
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
            float f1 = MathHelper.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
            this.xd = (this.xd / f1) * f * 0.4;
            this.yd = (this.yd / f1) * f * 0.4 + 0.1;
            this.zd = (this.zd / f1) * f * 0.4;

            sprite = Minecraft.getInstance().getItemRenderer().getModel(stack, world, null).getParticleIcon();
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

        public void render(Matrix4f matrix, IVertexBuilder buffer, float partialTicks, int light) {
            float f = (float) MathHelper.lerp(partialTicks, this.xo, this.x);
            float f1 = (float) MathHelper.lerp(partialTicks, this.yo, this.y);
            float f2 = (float) MathHelper.lerp(partialTicks, this.zo, this.z);
            Quaternion quaternion = Minecraft.getInstance().getEntityRenderDispatcher().camera.rotation();

            //Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
            //vector3f1.transform(quaternion);
            Vector3f[] vectors = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F),
                                                new Vector3f(-1.0F, 1.0F, 0.0F),
                                                new Vector3f(1.0F, 1.0F, 0.0F),
                                                new Vector3f(1.0F, -1.0F, 0.0F)};
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
                  .color(1F, 1F, 1F, 1F)
                  .uv(f8, f6)
                  //.color(1F, 1F, 1F, 1F)
                  .uv2(light)
                  .endVertex();
            buffer.vertex(matrix, vectors[1].x(), vectors[1].y(), vectors[1].z())
                  .color(1F, 1F, 1F, 1F)
                  .uv(f8, f5)
                  //.color(1F, 1F, 1F, 1F)
                  .uv2(light)
                  .endVertex();
            buffer.vertex(matrix, vectors[2].x(), vectors[2].y(), vectors[2].z())
                  .color(1F, 1F, 1F, 1F)
                  .uv(f7, f5)
                  //.color(1F, 1F, 1F, 1F)
                  .uv2(light)
                  .endVertex();
            buffer.vertex(matrix, vectors[3].x(), vectors[3].y(), vectors[3].z())
                  .color(1F, 1F, 1F, 1F)
                  .uv(f7, f6)
                  //.color(1F, 1F, 1F, 1F)
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
                bb = new AxisAlignedBB(d0, bb.minY, d1, d0 + this.bbWidth, bb.minY + this.bbHeight, d1 + this.bbWidth);
            }
        }
    }
}