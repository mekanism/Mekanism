package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import mekanism.api.RelativeSide;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiRadialSelector;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.client.render.hud.RadiationOverlay;
import mekanism.client.render.lib.Outlines;
import mekanism.client.render.lib.Outlines.Line;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.client.render.tileentity.IWireFrameRenderer;
import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.joml.Matrix3f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

public class RenderTickHandler {

    public static final Minecraft minecraft = Minecraft.getInstance();

    private static final Map<BlockState, List<Line>> cachedWireFrames = new Reference2ObjectOpenHashMap<>();
    private static final BoltRenderer boltRenderer = new BoltRenderer();
    private static final Object2BooleanMap<Class<?>> IS_EMI_SCREEN = new Object2BooleanOpenHashMap<>();

    private boolean outliningArea = false;

    public static void clearQueued() {
        RadiationOverlay.INSTANCE.resetRadiation();
    }

    public static void resetCached() {
        cachedWireFrames.clear();
    }

    public static void renderBolt(Object renderer, BoltEffect bolt, long gameTime) {
        boltRenderer.update(renderer, bolt, gameTime, MekanismRenderer.getPartialTick());
    }

    //Note: This listener is only registered if a recipe viewer is loaded
    public static void guiOpening(ScreenEvent.Opening event) {
        if (event.getCurrentScreen() instanceof GuiMekanism<?> screen) {
            if (Mekanism.hooks.jei.isLoaded()) {
                //If JEI is loaded and our current screen is a mekanism gui, check if the new screen is a JEI recipe screen
                if (event.getNewScreen() instanceof IRecipesGui) {
                    //If it is mark on our current screen that we are switching to JEI
                    screen.switchingToRecipeViewer = true;
                }
            }
            if (Mekanism.hooks.emi.isLoaded()) {
                //If Emi is loaded and our current screen is a mekanism gui, check if the new screen is an Emi recipe screen
                // https://github.com/emilyploszaj/emi/issues/481
                if (isEmiScreen(event.getNewScreen())) {
                    //If it is mark on our current screen that we are switching to EMI
                    screen.switchingToRecipeViewer = true;
                }
            }
        }
    }

    private static boolean isEmiScreen(@Nullable Screen newScreen) {
        return newScreen != null && IS_EMI_SCREEN.computeIfAbsent(newScreen.getClass(), (Class<?> cl) -> cl.getName().startsWith("dev.emi.emi"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderPostHighest(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof GuiMekanism) {
            //Translate forward how far we go, so that things like recipe viewers draw far enough forward
            // Note: We will pop this in a listener at the lowest priority
            Matrix3x2fStack pose = event.getGuiGraphics().pose();
            pose.pushMatrix();
            pose.translate(0, 0/* TODO - 26.1: , GuiMekanism.maxZOffset*/);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderPostLowest(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof GuiMekanism) {
            //Matching pop to the push we did in renderPostHighest
            event.getGuiGraphics().pose().popMatrix();
        }
    }

    @SubscribeEvent//TODO - 26.1 is this a correct replacement?
    public void renderWorldAfterParticles(RenderLevelStageEvent.AfterTranslucentParticles event) {
        if (boltRenderer.hasBoltsToRender()) {
            //TODO - 26.1: Figure out if this is still valid as the buffer
            /*MultiBufferSource.BufferSource renderer = minecraft.renderBuffers().bufferSource();
            LevelRenderState levelState = event.getLevelRenderState();
            boltRenderer.render(levelState.gameTime, MekanismRenderer.getPartialTick(), event.getPoseStack(), renderer, levelState.cameraRenderState.pos);
            renderer.endBatch(MekanismRenderType.MEK_LIGHTNING);*/
        }
    }

    @SubscribeEvent
    public void renderCrosshair(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.CROSSHAIR) && minecraft.gui.screen() instanceof GuiRadialSelector screen && screen.shouldHideCrosshair()) {
            //Hide the crosshair if we have a radial menu open and are drawing the back button
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void renderArm(RenderArmEvent event) {
        AbstractClientPlayer player = event.getPlayer();
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.getItem() instanceof ItemMekaSuitArmor armorItem) {
            MekaSuitArmor armor = (MekaSuitArmor) ((ISpecialGear) IClientItemExtensions.of(armorItem)).gearModel();
            AvatarRenderer<AbstractClientPlayer> renderer = (AvatarRenderer<AbstractClientPlayer>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
            PlayerModel model = renderer.getModel();
            AvatarRenderState renderState = renderer.createRenderState();
            renderer.extractRenderState(player, renderState, MekanismRenderer.getPartialTick());
            //TODO - 26.1 model.setAllVisible(true);
            //Note: We just want it to act as empty even if there is a map as it looks a lot better
            boolean rightHand = event.getArm() == HumanoidArm.RIGHT;
            if (rightHand) {
                renderState.rightArmPose = ArmPose.EMPTY;
            } else {
                renderState.leftArmPose = ArmPose.EMPTY;
            }
            renderState.attackTime = 0.0F;
            renderState.isCrouching = false;
            renderState.swimAmount = 0.0F;
            model.setupAnim(renderState);
            armor.renderArm(model, event.getPoseStack(), event.getSubmitNodeCollector(), event.getPackedLight(), renderState, chestStack, rightHand);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void tickEnd(ClientTickEvent.Post event) {
        //Note: We check that the game mode is not null as if it is that means the world is unloading, and we don't actually want to be rendering
        // as our data may be out of date or invalid. For example configs could unload while it is still unloading
        Level world;
        //noinspection ConstantValue
        if (minecraft.player != null && (world = minecraft.player.level()) != null && minecraft.gameMode != null && MekanismRenderer.isRunningNormally()) {
            float partialTicks = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            for (Player p : world.players()) {
                //Traverse active jetpacks and do animations
                if (Mekanism.playerState.isJetpackOn(p)) {
                    doJetpackRender(p, world, partialTicks);
                }

                if (world.getGameTime() % 4 == 0) {
                    //Traverse active scuba masks and do animations
                    if (Mekanism.playerState.isScubaMaskOn(p)) {
                        if (p.isInWater()) {
                            doScubaRender(p, world);
                        }
                    }

                    //Traverse players and do animations for idle flamethrowers
                    if (!p.swinging) {
                        if (p.isUsingItem()) {
                            InteractionHand usedHand = p.getUsedItemHand();
                            if (!(p.getItemInHand(usedHand).getItem() instanceof ItemFlamethrower)) {
                                //If we the used item isn't a flamethrower, grab the other hand's item for checks
                                // if it was an active flamethrower we just skip adding the idle particles
                                tryAddIdleFlamethrowerParticles(minecraft, p, usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                            }
                        } else if (!tryAddIdleFlamethrowerParticles(minecraft, p, InteractionHand.MAIN_HAND)) {
                            //If the player isn't using an item, try to first add particles for a flamethrower in the main hand
                            // and then add particles for a flamethrower in the offhand if we failed
                            tryAddIdleFlamethrowerParticles(minecraft, p, InteractionHand.OFF_HAND);
                        }
                    }
                }
            }
        }
    }

    private static void doScubaRender(Player p, Level world) {
        Pos3D vec = new Pos3D(0.4, 0.4, 0.4).multiply(p.getViewVector(1)).translate(0, -0.2, 0);
        Pos3D motion = vec.scale(0.2).translate(p.getDeltaMovement());
        Pos3D v = new Pos3D(p).translate(0, p.getEyeHeight(), 0).translate(vec);
        world.addParticle(MekanismParticleTypes.SCUBA_BUBBLE.get(), v.x, v.y, v.z, motion.x, motion.y + 0.2, motion.z);
    }

    private void doJetpackRender(Player p, Level world, float partialTicks) {
        Pos3D playerPos = new Pos3D(p).translate(0, p.getEyeHeight(), 0);
        //TODO - 1.21: Figure out why this is incorrect for other clients when they are hovering
        Vec3 playerMotion = p.getDeltaMovement();
        float random = (world.getRandom().nextFloat() - 0.5F) * 0.1F;
        //This positioning code is somewhat cursed, but it seems to be mostly working and entity pose code seems cursed in general
        float xRot;
        if (p.isCrouching()) {
            xRot = 20;
            playerPos = playerPos.translate(0, 0.125, 0);
        } else {
            float f = p.getSwimAmount(partialTicks);
            if (p.isFallFlying()) {
                float f1 = p.getFallFlyingTicks() + partialTicks;
                float f2 = Math.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
                xRot = f2 * (-90.0F - p.getXRot());
            } else {
                float f3 = p.isInWater() ? -90.0F - p.getXRot() : -90.0F;
                xRot = Mth.lerp(f, 0.0F, f3);
            }
            xRot = -xRot;
            Pos3D eyeAdjustments;
            if (p.isFallFlying() && (p != minecraft.player || !minecraft.options.getCameraType().isFirstPerson())) {
                eyeAdjustments = new Pos3D(0, p.getEyeHeight(Pose.STANDING), 0).xRot(xRot).yRot(p.yBodyRot);
            } else if (p.isVisuallySwimming()) {
                eyeAdjustments = new Pos3D(0, p.getEyeHeight(), 0).xRot(xRot).yRot(p.yBodyRot).translate(0, 0.5, 0);
            } else {
                eyeAdjustments = new Pos3D(0, p.getEyeHeight(), 0).xRot(xRot).yRot(p.yBodyRot);
            }
            playerPos = new Pos3D(p.getX() + eyeAdjustments.x, p.getY() + eyeAdjustments.y, p.getZ() + eyeAdjustments.z);
        }
        Pos3D vLeft = new Pos3D(-0.43, -0.55, -0.54).xRot(xRot).yRot(p.yBodyRot);
        renderJetpackSmoke(world, playerPos.translate(vLeft, playerMotion), vLeft.scale(0.2).translate(playerMotion, vLeft.scale(random)));
        Pos3D vRight = new Pos3D(0.43, -0.55, -0.54).xRot(xRot).yRot(p.yBodyRot);
        renderJetpackSmoke(world, playerPos.translate(vRight, playerMotion), vRight.scale(0.2).translate(playerMotion, vRight.scale(random)));
        Pos3D vCenter = new Pos3D((world.getRandom().nextFloat() - 0.5) * 0.4, -0.86, -0.30).xRot(xRot).yRot(p.yBodyRot);
        renderJetpackSmoke(world, playerPos.translate(vCenter, playerMotion), vCenter.scale(0.2).translate(playerMotion));
    }

    private static boolean tryAddIdleFlamethrowerParticles(Minecraft minecraft, Player player, InteractionHand hand) {
        if (!ItemFlamethrower.isIdleFlamethrower(player, hand)) {
            return false;
        }
        Pos3D flameVec;
        Entity vehicle = player.getVehicle();
        boolean rightHanded = MekanismUtils.isRightArm(player, hand);
        if (minecraft.player == player && minecraft.options.getCameraType().isFirstPerson()) {
            flameVec = new Pos3D(1, 1, 1)
                  .multiply(player.getViewVector(minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false)))
                  .yRot(rightHanded ? 15 : -15)
                  .translate(0, player.getEyeHeight() - 0.1, 0);
        } else {
            double flameXCoord = rightHanded ? -0.2 : 0.2;
            double flameYCoord = 1;
            double flameZCoord = 1.2;
            if (player.isCrouching()) {
                flameYCoord -= 0.65;
                flameZCoord -= 0.15;
            } else if (vehicle != null) {
                Vec3 attachmentPoint = player.getVehicleAttachmentPoint(vehicle);
                flameXCoord -= attachmentPoint.x;
                flameYCoord -= attachmentPoint.y + 0.1;
                flameZCoord -= attachmentPoint.z;
            }
            flameVec = new Pos3D(flameXCoord, flameYCoord, flameZCoord).yRot(player.yBodyRot);
        }
        Vec3 motion = vehicle == null ? player.getDeltaMovement() : vehicle.getDeltaMovement();
        Vec3 flameMotion = new Vec3(motion.x(), player.onGround() || vehicle != null ? 0 : motion.y(), motion.z());
        Vec3 mergedVec = player.position().add(flameVec);
        player.level().addParticle(MekanismParticleTypes.JETPACK_FLAME.get(), mergedVec.x, mergedVec.y, mergedVec.z, flameMotion.x, flameMotion.y, flameMotion.z);
        return true;
    }

    //TODO - 26.1 CustomBlockOutlineRenderer
    @SubscribeEvent
    public void onBlockHover(ExtractBlockOutlineRenderStateEvent event) {
        //TODO - 26.1: ExtractBlockOutlineRenderStateEvent and CustomBlockOutlineRenderer?
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        BlockHitResult rayTraceResult = event.getHitResult();
        ClientLevel world = (ClientLevel) player.level();
        BlockPos pos = event.getBlockPos();
        ProfilerFiller profiler = Profiler.get();
        BlockState blockState = event.getBlockState();

        //todo - 26.1: blasting unit. don't forget translucency check
            /*profiler.push(ProfilerConstants.AREA_MINE_OUTLINE);
            // Draw outlines for area mining blocks
            if (!outliningArea) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty() && stack.getItem() instanceof IBlastingItem tool) {
                    Map<BlockPos, BlockState> blocks = tool.getBlastedBlocks(world, player, stack, pos, blockState);
                    if (!blocks.isEmpty()) {
                        outliningArea = true;
                        Vec3 renderView = camera.position();
                        LevelRenderer levelRenderer = event.getLevelRenderer();
                        for (Map.Entry<BlockPos, BlockState> block : blocks.entrySet()) {
                            BlockPos blastingTarget = block.getKey();
                            // simulate ray tracing results for all block positions
                            if (!pos.equals(blastingTarget) && todo - 26.1: also move out of here. !ClientHooks.onDrawHighlight(levelRenderer, camera, rayTraceResult.withPosition(blastingTarget), event.getDeltaTracker(), matrix, renderer)) {
                                levelRenderer.renderHitOutline(matrix, renderer.getBuffer(RenderTypes.lines()), player, renderView.x, renderView.y, renderView.z, blastingTarget, block.getValue());
                            }
                        }
                        outliningArea = false;
                    }
                }
            }
            profiler.pop();*/

        profiler.push(ProfilerConstants.MEKANISM_OUTLINE);
        if (!blockState.isAir() && world.getWorldBorder().isWithinBounds(pos)) {
            BlockPos actualPos = pos;
            BlockState actualState = blockState;
            if (blockState.is(MekanismBlocks.BOUNDING_BLOCK)) {
                BlockPos mainPos = BlockBounding.getMainBlockPos(world, pos);
                if (mainPos != null) {
                    actualPos = mainPos;
                    actualState = world.getBlockState(actualPos);
                }
            }
            AttributeCustomSelectionBox customSelectionBox = Attribute.get(actualState, AttributeCustomSelectionBox.class);
            if (customSelectionBox != null) {
                if (customSelectionBox.isJavaModel()) {
                    //If we use a TER to render the wire frame, grab the tile
                    BlockEntity tile = WorldUtils.getTileEntity(world, actualPos);
                    if (tile != null) {
                        BlockEntityRenderer<BlockEntity, ?> tileRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile);
                        if (tileRenderer instanceof IWireFrameRenderer wireFrameRenderer && wireFrameRenderer.hasSelectionBox(actualState)) {
                            List<Line> outlinesFromModel = wireFrameRenderer.isCombined() ? getOutlinesFromModel(world, actualPos, actualState) : null;
                            event.addCustomRenderer(new IWireframeRendererHandler(actualPos, outlinesFromModel, wireFrameRenderer, tile, actualState, event.isHighContrast()));
                        }
                    }
                } else {
                    //Otherwise, skip getting the tile and just grab the model
                    List<Line> outlinesFromModel = getOutlinesFromModel(world, actualPos, actualState);
                    event.addCustomRenderer(new ModelOutlineHandler(actualPos, outlinesFromModel, event.isHighContrast()));
                }
            }
        }
        profiler.pop();

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemConfigurator)) {
            //If we are not holding a configurator, look if we are in the offhand
            stack = player.getOffhandItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemConfigurator)) {
                return;
            }
        }
        profiler.push(ProfilerConstants.CONFIGURABLE_MACHINE);
        ItemConfigurator.ConfiguratorMode state = ((ItemConfigurator) stack.getItem()).getMode(stack);
        if (state.isConfigurating()) {
            TransmissionType type = Objects.requireNonNull(state.getTransmission(), "Configurating state requires transmission type");
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile instanceof ISideConfiguration configurable) {
                TileComponentConfig config = configurable.getConfig();
                if (config.supports(type)) {
                    Direction face = rayTraceResult.getDirection();
                    ConfigInfo configInfo = config.getConfig(type);
                    if (configInfo != null) {
                        RelativeSide side = RelativeSide.fromDirections(configurable.getDirection(), face);
                        if (configInfo.isSideEnabled(side)) {
                            int transmissionColor = MekanismRenderer.getColorARGB(configInfo.getDataType(side).getColor(), 0.6F);
                            event.addCustomRenderer(new ConfiguratorOverlayHandler(pos, type, face, transmissionColor));
                        }
                    }
                }
            }
        }
        profiler.pop();
    }

    private static List<Line> getOutlinesFromModel(ClientLevel level, BlockPos pos, BlockState state) {
        List<Line> lines = cachedWireFrames.get(state);
        if (lines == null) {
            BlockStateModel bakedModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
            lines = Outlines.extract(level, pos, state, bakedModel);
            cachedWireFrames.put(state, lines);
        }
        return lines;
    }

    public static void renderVertexWireFrame(Collection<Line> lines, VertexConsumer buffer, PoseStack.Pose pose, boolean isHighContrast) {
        //tmp variables to avoid allocating each loop
        Vector4f pos = new Vector4f();
        Vector3f normal = new Vector3f();
        renderVertexWireFrame(lines, buffer, pose.pose(), pose.normal(), pos, normal, isHighContrast);
    }

    public static void renderVertexWireFrame(Collection<Line> lines, VertexConsumer buffer, Matrix4f pose, Matrix3f poseNormal, Vector4f pos, Vector3f normal, boolean isHighContrast) {
        float lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        //todo - 26.1: vanilla high contrast also does a black render. See net.minecraft.client.renderer.LevelRenderer.renderBlockOutline
        int color = isHighContrast ? CommonColors.HIGH_CONTRAST_DIAMOND : ARGB.black(102);
        for (Line line : lines) {
            poseNormal.transform(line.nX(), line.nY(), line.nZ(), normal);

            pose.transform(line.x1(), line.y1(), line.z1(), 1F, pos);
            buffer.addVertex(pos.x, pos.y, pos.z)
                  .setColor(color)
                  .setNormal(normal.x, normal.y, normal.z)
                  .setLineWidth(lineWidth);

            pose.transform(line.x2(), line.y2(), line.z2(), 1F, pos);
            buffer.addVertex(pos.x, pos.y, pos.z)
                  .setColor(color)
                  .setNormal(normal.x, normal.y, normal.z)
                  .setLineWidth(lineWidth);
        }
    }

    private void renderJetpackSmoke(Level world, Vec3 pos, Vec3 motion) {
        world.addParticle(MekanismParticleTypes.JETPACK_FLAME.get(), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        world.addParticle(MekanismParticleTypes.JETPACK_SMOKE.get(), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }

    @NullMarked
    private class IWireframeRendererHandler implements CustomBlockOutlineRenderer {

        private final BlockPos blockPos;
        @Nullable
        private final List<Line> outlinesFromModel;
        private final IWireFrameRenderer wireFrameRenderer;
        private final BlockEntity tile;
        private final BlockState blockState;
        private final boolean isHighContrast;

        public IWireframeRendererHandler(BlockPos blockPos, @Nullable List<Line> outlinesFromModel, IWireFrameRenderer wireFrameRenderer, BlockEntity tile, BlockState blockState, boolean isHighContrast) {
            this.blockPos = blockPos;
            this.outlinesFromModel = outlinesFromModel;
            this.wireFrameRenderer = wireFrameRenderer;
            this.tile = tile;
            this.blockState = blockState;
            this.isHighContrast = isHighContrast;
        }

        @Override
        public boolean render(BlockOutlineRenderState renderState, SubmitNodeCollector submitNodeCollector, PoseStack matrix, LevelRenderState levelRenderState) {
            //TODO - 26.2: Figure out if we need an equivalent to this
            //if (renderState.isTranslucent() == translucentPass) {
            matrix.pushPose();
            Vec3 viewPosition = levelRenderState.cameraRenderState.pos;
            matrix.translate(blockPos.getX() - viewPosition.x, blockPos.getY() - viewPosition.y, blockPos.getZ() - viewPosition.z);
            //TODO - 26.2: Is custom geometry the correct way to do this?
            if (outlinesFromModel != null) {
                submitNodeCollector.submitCustomGeometry(matrix, RenderTypes.lines(), (pose, buffer) -> renderVertexWireFrame(outlinesFromModel, buffer, pose, isHighContrast));
            }
            wireFrameRenderer.renderWireFrame(tile, blockState, MekanismRenderer.getPartialTick(), submitNodeCollector, matrix, levelRenderState, isHighContrast);
            matrix.popPose();
            //}
            return true;
        }
    }

    @NullMarked
    private class ModelOutlineHandler implements CustomBlockOutlineRenderer {

        private final BlockPos blockPos;
        private final List<Line> outlinesFromModel;
        private final boolean isHighContrast;

        public ModelOutlineHandler(BlockPos blockPos, List<Line> outlinesFromModel, boolean isHighContrast) {
            this.blockPos = blockPos;
            this.outlinesFromModel = outlinesFromModel;
            this.isHighContrast = isHighContrast;
        }

        @Override
        public boolean render(BlockOutlineRenderState renderState, SubmitNodeCollector submitNodeCollector, PoseStack matrix, LevelRenderState levelRenderState) {
            //TODO - 26.2: Figure out if we need an equivalent to this
            //if (renderState.isTranslucent() == translucentPass) {
            matrix.pushPose();
            Vec3 viewPosition = levelRenderState.cameraRenderState.pos;
            matrix.translate(blockPos.getX() - viewPosition.x, blockPos.getY() - viewPosition.y, blockPos.getZ() - viewPosition.z);
            //0.4 Alpha
            //TODO - 26.2: Is custom geometry the correct way to do this?
            submitNodeCollector.submitCustomGeometry(matrix, RenderTypes.lines(), (pose, buffer) -> renderVertexWireFrame(outlinesFromModel, buffer, pose, isHighContrast));
            matrix.popPose();
            //}
            return true;
        }
    }

}