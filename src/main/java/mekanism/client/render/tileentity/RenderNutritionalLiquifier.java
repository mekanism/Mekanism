package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.tileentity.RenderNutritionalLiquifier.LiquifierRenderState;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public class RenderNutritionalLiquifier extends MekanismTileEntityRenderer<TileEntityNutritionalLiquifier, LiquifierRenderState> {

    private static final Map<TileEntityNutritionalLiquifier, PseudoParticleData> particles = new WeakHashMap<>();
    private static final int stages = 40;
    private static final float BLADE_SPEED = 25F;
    private static final float ROTATE_SPEED = 10F;

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
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(liquifier, state, partialTick, cameraPosition, breakProgress);
        if (!liquifier.fluidTank.isEmpty()) {
            FluidResource paste = liquifier.fluidTank.resource();
            float fluidScale = liquifier.fluidTank.amountAsLong() / (float) liquifier.fluidTank.capacityAsLong(paste);
            state.pasteTint = MekanismRenderer.getColorARGB(paste, fluidScale);
            state.stage = ModelRenderer.getStage(paste, stages, fluidScale);
            state.pasteTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(paste, FluidTextureType.STILL));
            state.pasteGlow = LightCoordsUtil.lightCoordsWithEmission(state.lightCoords, paste.getFluidType().getLightLevel());
        } else {
            state.stage = 0;
        }
        state.active = liquifier.getActive();
        if (state.active) {
            long gameTime = liquifier.getGameTime();
            state.bladeRotation = ((gameTime + partialTick) * BLADE_SPEED) % 360;
            state.itemRotation = ((gameTime + partialTick) * ROTATE_SPEED) % 360;
        }
        ItemStack stack = liquifier.getRenderStack();
        if (!stack.isEmpty()) {
            Level level = liquifier.getLevel();
            //Copy from how the campfire renderer calculates the seed
            int seed = (int) state.blockPos.asLong();
            this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GROUND, level, null, seed);

            if (state.active && Minecraft.getInstance().options.particles().get() != ParticleStatus.MINIMAL) {
                //Render eating particles
                PseudoParticleData pseudoParticles = particles.computeIfAbsent(liquifier, _ -> new PseudoParticleData());
                if (isTickingNormally(liquifier)) {
                    //Don't add particles if the game is paused
                    long gameTime = liquifier.getGameTime();
                    if (pseudoParticles.lastTick != gameTime) {
                        pseudoParticles.lastTick = gameTime;
                        for (Iterator<BreakingItemParticle> iterator = pseudoParticles.particles.iterator(); iterator.hasNext(); ) {
                            BreakingItemParticle particle = iterator.next();
                            particle.tick();
                            if (!particle.isAlive()) {
                                iterator.remove();
                            }
                        }
                    }
                    int rate = Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED ? 10 : 3;
                    if (gameTime % rate == 0) {
                        RandomSource random = level.getRandom();
                        Material.Baked itemSprite = state.item.pickParticleMaterial(random);
                        pseudoParticles.particles.add(new EatingParticle((ClientLevel) level, state.blockPos, itemSprite, state.lightCoords));
                    }
                }
                Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
                for (BreakingItemParticle particle : pseudoParticles.particles) {
                    particle.extract(state.particles, camera, partialTick);
                }
                state.partialTick = partialTick;
            } else {
                particles.remove(liquifier);
            }
        }
    }

    @Override
    public void submit(LiquifierRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.stage > 0 && state.pasteTexture != null) {
            RenderResizableCuboid.renderCube(RenderResizableCuboid.SideRender.NOT_DOWN, 0.001F, 0.313F, 0.001F, 0.999F,
                  0.313F + 0.624F * (state.stage / (float) stages), 0.999F, poseStack, Sheets.translucentBlockItemSheet(), nodeCollector,
                  state.pasteTint, state.pasteGlow, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(state.blockPos), state.pasteTexture);
        }
        if (state.active) {
            //Render the blade at the correct rotation if we are active
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.rotateDegrees(Axis.YP, state.bladeRotation);
            poseStack.translate(-0.5, -0.5, -0.5);
            submitBreakableBlockModel(nodeCollector, poseStack, Sheets.cutoutBlockItemSheet(), MekanismModelCache.INSTANCE.LIQUIFIER_BLADE.getBakedModel(), state);
            poseStack.popPose();
        }
        //Render the item and particle
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.6, 0.5);
            if (state.active) {
                //Make the item rotate if the liquifier is active
                poseStack.rotateDegrees(Axis.YP, state.itemRotation);
            }
            state.item.submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
            if (!state.particles.isEmpty()) {
                state.particles.submit(nodeCollector, camera);
            }
        }
    }

    public static class LiquifierRenderState extends BlockEntityRenderState {

        public final ItemStackRenderState item = new ItemStackRenderState();
        public final QuadParticleRenderState particles = new QuadParticleRenderState();
        public float partialTick;
        public float bladeRotation;
        public float itemRotation;
        public boolean active;
        public int pasteTint = CommonColors.WHITE;
        public RenderResizableCuboid.@Nullable TexturePicker pasteTexture;
        public int pasteGlow;
        public int stage;
    }

    private static class PseudoParticleData {

        private final List<BreakingItemParticle> particles = new ArrayList<>();
        private long lastTick;
    }

    private static class EatingParticle extends BreakingItemParticle {

        private final int lightCoords;
        private final BlockPos blockPos;
        private boolean stoppedByCollision;

        public EatingParticle(ClientLevel level, BlockPos pos, Material.@Nullable Baked itemSprite, int lightCoords) {
            this.blockPos = pos;
            this.lightCoords = lightCoords;
            RandomSource random = level.getRandom();
            super(level,
                  pos.getX() + 0.5 + (random.nextFloat() - 0.5D) * 0.3D,
                  pos.getY() + 0.55 + (random.nextFloat() - 0.5D) * 0.3D,
                  pos.getZ() + 0.5 + (random.nextFloat() - 0.5D) * 0.3D,
                  (random.nextFloat() - 0.5D) * 0.075,
                  random.nextDouble() * 0.1D + 0.05D,
                  (random.nextFloat() - 0.5D) * 0.075,
                  itemSprite == null ? Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).missingSprite() : itemSprite.sprite()
            );
        }

        @Override
        protected int getLightCoords(float a) {
            return lightCoords;
        }

        /// Copy of super with collision check changed
        @Override
        public void move(double xa, double ya, double za) {
            if (!this.stoppedByCollision) {
                double originalXa = xa;
                double originalYa = ya;
                double originalZa = za;
                AABB bb = getBoundingBox();
                double minX = blockPos.getX();
                double maxX = minX + 1;
                double minY = blockPos.getY() + 0.325;
                double minZ = blockPos.getZ();
                double maxZ = minZ + 1;
                if (bb.minX + xa <= minX) {
                    xa = minX - bb.minX;
                } else if (bb.maxX + xa >= maxX) {
                    xa = maxX - bb.maxX;
                }
                if (bb.minY + ya <= minY) {
                    ya = minY - bb.minY;
                }
                if (bb.minZ + za <= minZ) {
                    za = minZ - bb.minZ;
                } else if (bb.maxZ + za >= maxZ) {
                    za = maxZ - bb.maxZ;
                }

                if (xa != 0.0 || ya != 0.0 || za != 0.0) {
                    setBoundingBox(getBoundingBox().move(xa, ya, za));
                    setLocationFromBoundingbox();
                }

                if (Math.abs(originalYa) >= 1.0E-5F && Math.abs(ya) < 1.0E-5F) {
                    this.stoppedByCollision = true;
                }

                this.onGround = originalYa != ya && originalYa < 0.0;
                if (originalXa != xa) {
                    this.xd = 0.0;
                }

                if (originalZa != za) {
                    this.zd = 0.0;
                }
            }
        }
    }
}