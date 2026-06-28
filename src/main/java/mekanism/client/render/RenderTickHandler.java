package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Objects;
import mekanism.api.RelativeSide;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiRadialSelector;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.client.render.hud.RadiationOverlay;
import mekanism.client.render.lib.effect.BoltFeatureRenderer;
import mekanism.client.render.lib.effect.BoltFeatureRenderer.BoltRenderState;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.client.render.outline.Outlines;
import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
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
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;
import org.jspecify.annotations.Nullable;

public class RenderTickHandler {

    public static final Minecraft minecraft = Minecraft.getInstance();

    private static final BoltRenderer boltRenderer = new BoltRenderer();
    private static final Object2BooleanMap<Class<?>> IS_EMI_SCREEN = new Object2BooleanOpenHashMap<>();

    private boolean outliningArea = false;

    public static void clearQueued() {
        RadiationOverlay.INSTANCE.resetRadiation();
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

    //TODO - 26.2: Figure out if we need this any more
    /*@SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderPostHighest(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof GuiMekanism) {
            //Translate forward how far we go, so that things like recipe viewers draw far enough forward
            // Note: We will pop this in a listener at the lowest priority
            Matrix3x2fStack pose = event.getGuiGraphics().pose();
            pose.pushMatrix();
            pose.translate(0, 0, GuiMekanism.maxZOffset);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderPostLowest(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof GuiMekanism) {
            //Matching pop to the push we did in renderPostHighest
            event.getGuiGraphics().pose().popMatrix();
        }
    }*/

    @SubscribeEvent
    public void submitCustomGeometry(SubmitCustomGeometryEvent event) {
        if (boltRenderer.hasBoltsToRender()) {
            LevelRenderState levelState = event.getLevelRenderState();
            List<BoltRenderState> boltRenderStates = boltRenderer.collectBoltStates(levelState.gameTime, MekanismRenderer.getPartialTick());
            if (!boltRenderStates.isEmpty()) {

                PoseStack poseStack = event.getPoseStack();
                SubmitNodeCollector nodeCollector = event.getSubmitNodeCollector();
                poseStack.pushPose();
                Vec3 cameraPos = levelState.cameraRenderState.pos;
                poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

                PoseStack.Pose pose = poseStack.last().copy();
                for (BoltRenderState state : boltRenderStates) {
                    nodeCollector.submitSpecial(RenderPhaseKeys.AFTER_TERRAIN, new BoltFeatureRenderer.Submit(pose, state));
                }
                poseStack.popPose();
            }
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
            //TODO - 26.2 model.setAllVisible(true);
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
            float partialTicks = MekanismRenderer.getPartialTick();
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
                                tryAddIdleFlamethrowerParticles(minecraft, p, usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, partialTicks);
                            }
                        } else if (!tryAddIdleFlamethrowerParticles(minecraft, p, InteractionHand.MAIN_HAND, partialTicks)) {
                            //If the player isn't using an item, try to first add particles for a flamethrower in the main hand
                            // and then add particles for a flamethrower in the offhand if we failed
                            tryAddIdleFlamethrowerParticles(minecraft, p, InteractionHand.OFF_HAND, partialTicks);
                        }
                    }
                }
            }
        }
    }

    private static void doScubaRender(Player p, Level world) {
        Vec3 vec = new Vec3(0.4, 0.4, 0.4).multiply(p.getViewVector(1)).add(0, -0.2, 0);
        Vec3 motion = vec.scale(0.2).add(p.getDeltaMovement());
        Vec3 v = p.getEyePosition().add(vec);
        world.addParticle(MekanismParticleTypes.SCUBA_BUBBLE.get(), v.x, v.y, v.z, motion.x, motion.y + 0.2, motion.z);
    }

    private void doJetpackRender(Player p, Level world, float partialTicks) {
        Vec3 playerPos = p.getEyePosition();
        //TODO - 1.21: Figure out why this is incorrect for other clients when they are hovering
        Vec3 playerMotion = p.getDeltaMovement();
        float random = (world.getRandom().nextFloat() - 0.5F) * 0.1F;
        //This positioning code is somewhat cursed, but it seems to be mostly working and entity pose code seems cursed in general
        float xAngle;
        float bodyYRot = -p.yBodyRot * Mth.DEG_TO_RAD;
        if (p.isCrouching()) {
            xAngle = 20 * Mth.DEG_TO_RAD;
            playerPos = playerPos.add(0, 0.125, 0);
        } else {
            float swimAmount = p.getSwimAmount(partialTicks);
            if (p.isFallFlying()) {
                float fallFlyingTimeInTicks = p.getFallFlyingTicks() + partialTicks;
                //AvatarRenderSate#fallFlyingScale
                float fallFlyingScale = Math.clamp(fallFlyingTimeInTicks * fallFlyingTimeInTicks / 100.0F, 0.0F, 1.0F);
                if (!p.isAutoSpinAttack()) {
                    xAngle = fallFlyingScale * (-90.0F - p.getXRot()) * Mth.DEG_TO_RAD;
                } else {
                    xAngle = 0;
                }
            } else {
                float targetXRot = p.isInWater() ? -90.0F - p.getXRot() : -90.0F;
                xAngle = Mth.lerp(swimAmount, 0.0F, targetXRot) * Mth.DEG_TO_RAD;
            }
            Vec3 eyeAdjustments;
            if (p.isFallFlying() && (p != minecraft.player || !minecraft.options.getCameraType().isFirstPerson())) {
                eyeAdjustments = new Vec3(0, p.getEyeHeight(Pose.STANDING), 0).xRot(xAngle).yRot(bodyYRot).add(0, 0.5, 0);
            } else {
                eyeAdjustments = new Vec3(0, p.getEyeHeight(), 0).xRot(xAngle).yRot(bodyYRot);
                if (p.isVisuallySwimming()) {
                    eyeAdjustments = eyeAdjustments.add(0, 0.5, 0);
                }
            }
            playerPos = p.position().add(eyeAdjustments);
        }
        Vec3 vLeft = new Vec3(-0.43, -0.55, -0.54).xRot(xAngle).yRot(bodyYRot);
        renderJetpackSmoke(world, playerPos.add(vLeft).add(playerMotion), vLeft.scale(0.2).add(playerMotion).add(vLeft.scale(random)));
        Vec3 vRight = new Vec3(0.43, -0.55, -0.54).xRot(xAngle).yRot(bodyYRot);
        renderJetpackSmoke(world, playerPos.add(vRight).add(playerMotion), vRight.scale(0.2).add(playerMotion).add(vRight.scale(random)));
        Vec3 vCenter = new Vec3((world.getRandom().nextFloat() - 0.5) * 0.4, -0.86, -0.30).xRot(xAngle).yRot(bodyYRot);
        renderJetpackSmoke(world, playerPos.add(vCenter).add(playerMotion), vCenter.scale(0.2).add(playerMotion));
    }

    private static boolean tryAddIdleFlamethrowerParticles(Minecraft minecraft, Player player, InteractionHand hand, float partialTick) {
        if (!ItemFlamethrower.isIdleFlamethrower(player, hand)) {
            return false;
        }
        Vec3 flameVec;
        Entity vehicle = player.getVehicle();
        boolean rightHanded = MekanismUtils.isRightArm(player, hand);
        if (minecraft.player == player && minecraft.options.getCameraType().isFirstPerson()) {
            float angle = 15 * Mth.DEG_TO_RAD;
            flameVec = player.getViewVector(partialTick)
                  .yRot(rightHanded ? -angle : angle)
                  .add(0, player.getEyeHeight() - 0.1, 0);
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
            flameVec = new Vec3(flameXCoord, flameYCoord, flameZCoord).yRot(-player.yBodyRot * Mth.DEG_TO_RAD);
        }
        Vec3 motion = vehicle == null ? player.getDeltaMovement() : vehicle.getDeltaMovement();
        Vec3 flameMotion = new Vec3(motion.x(), player.onGround() || vehicle != null ? 0 : motion.y(), motion.z());
        Vec3 mergedVec = player.position().add(flameVec);
        player.level().addParticle(MekanismParticleTypes.JETPACK_FLAME.get(), mergedVec.x, mergedVec.y, mergedVec.z, flameMotion.x, flameMotion.y, flameMotion.z);
        return true;
    }

    //TODO - 26.2 CustomBlockOutlineRenderer
    @SubscribeEvent
    public void onBlockHover(ExtractBlockOutlineRenderStateEvent event) {
        //TODO - 26.2: ExtractBlockOutlineRenderStateEvent and CustomBlockOutlineRenderer?
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        BlockHitResult rayTraceResult = event.getHitResult();
        ClientLevel world = (ClientLevel) player.level();
        BlockPos pos = event.getBlockPos();
        ProfilerFiller profiler = Profiler.get();
        BlockState blockState = event.getBlockState();

        //TODO - 26.2: blasting unit. don't forget translucency check
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
                        if (!pos.equals(blastingTarget) && TODO - 26.2: also move out of here. !ClientHooks.onDrawHighlight(levelRenderer, camera, rayTraceResult.withPosition(blastingTarget), event.getDeltaTracker(), matrix, renderer)) {
                            levelRenderer.renderHitOutline(matrix, renderer.getBuffer(RenderTypes.lines()), player, renderView.x, renderView.y, renderView.z, blastingTarget, block.getValue());
                        }
                    }
                    outliningArea = false;
                }
            }
        }
        profiler.pop();*/

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemConfigurator)) {
            //If we are not holding a configurator, look if we are in the offhand
            stack = player.getOffhandItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemConfigurator)) {
                return;
            }
        }
        boolean showingConfiguratorOverlay = false;
        profiler.push(ProfilerConstants.CONFIGURABLE_MACHINE);
        ItemConfigurator.ConfiguratorMode state = ((ItemConfigurator) stack.getItem()).getMode(stack);
        if (blockState.is(MekanismBlocks.DIVERSION_TRANSPORTER)) {
            TileEntityDiversionTransporter transporter = WorldUtils.getTileEntity(TileEntityDiversionTransporter.class, world, pos);
            if (transporter != null) {
                Direction face = transporter.getSideLookingAt(player, rayTraceResult.getDirection());
                TextureAtlasSprite sprite = switch (transporter.getTransmitter().modes[face.ordinal()]) {
                    case DISABLED -> MekanismRenderer.GUNPOWDER_SPRITE;
                    case HIGH -> MekanismRenderer.REDSTONE_TORCH_SPRITE;
                    case LOW -> MekanismRenderer.REDSTONE_TORCH_OFF_SPRITE;
                };
                if (sprite != null) {
                    event.addCustomRenderer(new ConfiguratorOverlayHandler(pos, sprite, face));
                }
            }
        } else if (state.isConfigurating()) {
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
                            showingConfiguratorOverlay = true;
                        }
                    }
                }
            }
        }
        profiler.pop();
        if (!showingConfiguratorOverlay) {
            //Only do normal outline rendering if we aren't displaying the configurator overlay
            Outlines.onBlockHover(event, profiler);
        }
    }

    private void renderJetpackSmoke(Level world, Vec3 pos, Vec3 motion) {
        world.addParticle(MekanismParticleTypes.JETPACK_FLAME.get(), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        world.addParticle(MekanismParticleTypes.JETPACK_SMOKE.get(), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }
}