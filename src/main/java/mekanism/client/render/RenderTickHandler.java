package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.client.MekanismClient;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.Vertex;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.client.render.tileentity.IWireFrameRenderer;
import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel.ArmPose;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderTickHandler {

    public final Minecraft minecraft = Minecraft.getInstance();

    private static final ResourceLocation POWER_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "horizontal_power_long.png");
    private static final Map<BlockState, List<Vertex[]>> cachedWireFrames = new HashMap<>();
    private static final Map<Direction, Map<TransmissionType, Model3D>> cachedOverlays = new EnumMap<>(Direction.class);
    private static final EquipmentSlotType[] EQUIPMENT_ORDER = new EquipmentSlotType[]{EquipmentSlotType.OFFHAND, EquipmentSlotType.MAINHAND,
                                                                                       EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS,
                                                                                       EquipmentSlotType.FEET};

    private static final HUDRenderer hudRenderer = new HUDRenderer();

    public static int modeSwitchTimer = 0;
    public static double prevRadiation = 0;

    private static final BoltRenderer boltRenderer = new BoltRenderer();

    public static void resetCached() {
        cachedOverlays.clear();
        cachedWireFrames.clear();
    }

    public static void renderBolt(Object renderer, BoltEffect bolt) {
        boltRenderer.update(renderer, bolt, MekanismRenderer.getPartialTick());
    }

    //Note: This listener is only registered if JEI is loaded
    public static void guiOpening(GuiOpenEvent event) {
        if (Minecraft.getInstance().screen instanceof GuiMekanism) {
            //If JEI is loaded and our current screen is a mekanism gui,
            // check if the new screen is a JEI recipe screen
            if (event.getGui() instanceof IRecipesGui) {
                //If it is mark on our current screen that we are switching to JEI
                ((GuiMekanism<?>) Minecraft.getInstance().screen).switchingToJEI = true;
            }
        }
    }

    @SubscribeEvent
    public void filterTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof IModuleContainerItem) {
            ((IModuleContainerItem) stack.getItem()).filterTooltips(stack, event.getToolTip());
        }
    }

    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        if (boltRenderer.hasBoltsToRender()) {
            //Only do matrix transforms and mess with buffers if we actually have any bolts to render
            MatrixStack matrix = event.getMatrixStack();
            matrix.pushPose();
            // here we translate based on the inverse position of the client viewing camera to get back to 0, 0, 0
            Vector3d camVec = minecraft.gameRenderer.getMainCamera().getPosition();
            matrix.translate(-camVec.x, -camVec.y, -camVec.z);
            //TODO: FIXME, this doesn't work on fabulous, I think it needs something like
            // https://github.com/MinecraftForge/MinecraftForge/pull/7225
            IRenderTypeBuffer.Impl renderer = minecraft.renderBuffers().bufferSource();
            boltRenderer.render(event.getPartialTicks(), matrix, renderer);
            renderer.endBatch(MekanismRenderType.MEK_LIGHTNING);
            matrix.popPose();
        }
    }

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == ElementType.HOTBAR) {
            if (!minecraft.player.isSpectator() && MekanismConfig.client.enableHUD.get() && MekanismClient.renderHUD) {
                int count = 0;
                Map<EquipmentSlotType, List<ITextComponent>> renderStrings = new LinkedHashMap<>();
                for (EquipmentSlotType slotType : EQUIPMENT_ORDER) {
                    ItemStack stack = minecraft.player.getItemBySlot(slotType);
                    if (stack.getItem() instanceof IItemHUDProvider) {
                        List<ITextComponent> list = new ArrayList<>();
                        ((IItemHUDProvider) stack.getItem()).addHUDStrings(list, minecraft.player, stack, slotType);
                        int size = list.size();
                        if (size > 0) {
                            renderStrings.put(slotType, list);
                            count += size;
                        }
                    }
                }
                MatrixStack matrix = event.getMatrixStack();
                if (count > 0) {
                    int start = (renderStrings.size() * 2) + (count * 9);
                    boolean alignLeft = MekanismConfig.client.alignHUDLeft.get();
                    MainWindow window = event.getWindow();
                    int y = window.getGuiScaledHeight();
                    float hudScale = MekanismConfig.client.hudScale.get();
                    int yScale = (int) ((1 / hudScale) * y);
                    matrix.pushPose();
                    matrix.scale(hudScale, hudScale, hudScale);
                    for (Map.Entry<EquipmentSlotType, List<ITextComponent>> entry : renderStrings.entrySet()) {
                        for (ITextComponent text : entry.getValue()) {
                            drawString(window, matrix, text, alignLeft, yScale - start, 0xC8C8C8);
                            start -= 9;
                        }
                        start -= 2;
                    }
                    matrix.popPose();
                }
                if (minecraft.player.getItemBySlot(EquipmentSlotType.HEAD).getItem() instanceof ItemMekaSuitArmor) {
                    hudRenderer.renderHUD(matrix, event.getPartialTicks());
                }
            }
        } else if (event.getType() == ElementType.ARMOR) {
            FloatingLong capacity = FloatingLong.ZERO, stored = FloatingLong.ZERO;
            for (ItemStack stack : minecraft.player.inventory.armor) {
                if (stack.getItem() instanceof ItemMekaSuitArmor) {
                    IEnergyContainer container = StorageUtils.getEnergyContainer(stack, 0);
                    if (container != null) {
                        capacity = capacity.plusEqual(container.getMaxEnergy());
                        stored = stored.plusEqual(container.getEnergy());
                    }
                }
            }
            if (!capacity.isZero()) {
                int x = event.getWindow().getGuiScaledWidth() / 2 - 91;
                int y = event.getWindow().getGuiScaledHeight() - ForgeIngameGui.left_height + 2;
                int length = (int) Math.round(stored.divide(capacity).doubleValue() * 79);
                MatrixStack matrix = event.getMatrixStack();
                GuiUtils.renderExtendedTexture(matrix, GuiBar.BAR, 2, 2, x, y, 81, 6);
                minecraft.getTextureManager().bind(POWER_BAR);
                AbstractGui.blit(matrix, x + 1, y + 1, length, 4, 0, 0, length, 4, 79, 4);
                minecraft.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
                ForgeIngameGui.left_height += 8;
            }
        }
    }

    @SubscribeEvent
    public void renderArm(RenderArmEvent event) {
        AbstractClientPlayerEntity player = event.getPlayer();
        ItemStack chestStack = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (chestStack.getItem() instanceof ItemMekaSuitArmor) {
            MekaSuitArmor armor = (MekaSuitArmor) ((ItemMekaSuitArmor) chestStack.getItem()).getGearModel();
            armor.setAllVisible(true);
            //Note: We just want it to act as empty even if there is a map as it looks a lot better
            boolean rightHand = event.getArm() == HandSide.RIGHT;
            if (rightHand) {
                armor.rightArmPose = ArmPose.EMPTY;
            } else {
                armor.leftArmPose = ArmPose.EMPTY;
            }
            armor.attackTime = 0.0F;
            armor.crouching = false;
            armor.swimAmount = 0.0F;
            armor.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            armor.renderArm(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), OverlayTexture.NO_OVERLAY, chestStack.hasFoil(), player, rightHand);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void tickEnd(RenderTickEvent event) {
        if (event.phase == Phase.END) {
            if (minecraft.player != null && minecraft.player.level != null && !minecraft.isPaused()) {
                PlayerEntity player = minecraft.player;
                World world = minecraft.player.level;
                //TODO: Check if we have another matrix stack we should use
                MatrixStack matrix = new MatrixStack();
                renderStatusBar(matrix, player);
                //Traverse active jetpacks and do animations
                for (UUID uuid : Mekanism.playerState.getActiveJetpacks()) {
                    PlayerEntity p = world.getPlayerByUUID(uuid);
                    if (p != null) {
                        Pos3D playerPos = new Pos3D(p).translate(0, p.getEyeHeight(), 0);
                        Vector3d playerMotion = p.getDeltaMovement();
                        float random = (world.random.nextFloat() - 0.5F) * 0.1F;
                        //This positioning code is somewhat cursed but it seems to be mostly working and entity pose code seems cursed in general
                        float xRot;
                        if (p.isCrouching()) {
                            xRot = 20;
                            playerPos = playerPos.translate(0, 0.125, 0);
                        } else {
                            float f = p.getSwimAmount(event.renderTickTime);
                            if (p.isFallFlying()) {
                                float f1 = (float) p.getFallFlyingTicks() + event.renderTickTime;
                                float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
                                xRot = f2 * (-90.0F - p.xRot);
                            } else {
                                float f3 = p.isInWater() ? -90.0F - p.xRot : -90.0F;
                                xRot = MathHelper.lerp(f, 0.0F, f3);
                            }
                            xRot = -xRot;
                            Pos3D eyeAdjustments;
                            if (p.isFallFlying() && (p != player || !minecraft.options.getCameraType().isFirstPerson())) {
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
                        Pos3D vCenter = new Pos3D((world.random.nextFloat() - 0.5) * 0.4, -0.86, -0.30).xRot(xRot).yRot(p.yBodyRot);
                        renderJetpackSmoke(world, playerPos.translate(vCenter, playerMotion), vCenter.scale(0.2).translate(playerMotion));
                    }
                }

                if (world.getGameTime() % 4 == 0) {
                    //Traverse active scuba masks and do animations
                    for (UUID uuid : Mekanism.playerState.getActiveScubaMasks()) {
                        PlayerEntity p = world.getPlayerByUUID(uuid);
                        if (p != null && p.isInWater()) {
                            Pos3D vec = new Pos3D(0.4, 0.4, 0.4).multiply(p.getViewVector(1)).translate(0, -0.2, 0);
                            Pos3D motion = vec.scale(0.2).translate(p.getDeltaMovement());
                            Pos3D v = new Pos3D(p).translate(0, p.getEyeHeight(), 0).translate(vec);
                            world.addParticle(MekanismParticleTypes.SCUBA_BUBBLE.getParticleType(), v.x, v.y, v.z, motion.x, motion.y + 0.2, motion.z);
                        }
                    }
                    //Traverse players and do animations for idle flame throwers
                    for (PlayerEntity p : world.players()) {
                        if (!p.swinging && !Mekanism.playerState.isFlamethrowerOn(p)) {
                            ItemStack currentItem = p.getMainHandItem();
                            if (!currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower && ChemicalUtil.hasGas(currentItem)) {
                                Pos3D flameVec;
                                boolean rightHanded = p.getMainArm() == HandSide.RIGHT;
                                if (player == p && minecraft.options.getCameraType().isFirstPerson()) {
                                    flameVec = new Pos3D(1, 1, 1)
                                          .multiply(p.getViewVector(event.renderTickTime))
                                          .yRot(rightHanded ? 15 : -15)
                                          .translate(0, p.getEyeHeight() - 0.1, 0);
                                } else {
                                    double flameXCoord = rightHanded ? -0.2 : 0.2;
                                    double flameYCoord = 1;
                                    double flameZCoord = 1.2;
                                    if (p.isCrouching()) {
                                        flameYCoord -= 0.65;
                                        flameZCoord -= 0.15;
                                    }
                                    flameVec = new Pos3D(flameXCoord, flameYCoord, flameZCoord).yRot(p.yBodyRot);
                                }
                                Vector3d motion = p.getDeltaMovement();
                                Vector3d flameMotion = new Vector3d(motion.x(), p.isOnGround() ? 0 : motion.y(), motion.z());
                                Vector3d mergedVec = p.position().add(flameVec);
                                world.addParticle(MekanismParticleTypes.JETPACK_FLAME.getParticleType(),
                                      mergedVec.x, mergedVec.y, mergedVec.z, flameMotion.x, flameMotion.y, flameMotion.z);
                            }
                        }
                    }
                }

                if (MekanismUtils.isPlayingMode(player)) {
                    player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> {
                        double radiation = c.getRadiation();
                        double severity = RadiationScale.getScaledDoseSeverity(radiation) * 0.8;
                        if (prevRadiation < severity) {
                            prevRadiation = Math.min(severity, prevRadiation + 0.01);
                        }
                        if (prevRadiation > severity) {
                            prevRadiation = Math.max(severity, prevRadiation - 0.01);
                        }
                        if (severity > RadiationManager.BASELINE) {
                            int effect = (int) (prevRadiation * 255);
                            int color = (0x701E1E << 8) + effect;
                            MekanismRenderer.renderColorOverlay(matrix, 0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), color);
                        }
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockHover(DrawHighlightEvent.HighlightBlock event) {
        PlayerEntity player = minecraft.player;
        if (player == null) {
            return;
        }
        BlockRayTraceResult rayTraceResult = event.getTarget();
        if (!rayTraceResult.getType().equals(Type.MISS)) {
            World world = player.getCommandSenderWorld();
            BlockPos pos = rayTraceResult.getBlockPos();
            IRenderTypeBuffer renderer = event.getBuffers();
            ActiveRenderInfo info = event.getInfo();
            MatrixStack matrix = event.getMatrix();
            IProfiler profiler = world.getProfiler();
            BlockState blockState = world.getBlockState(pos);
            boolean shouldCancel = false;
            profiler.push(ProfilerConstants.MEKANISM_OUTLINE);
            if (!blockState.isAir(world, pos) && world.getWorldBorder().isWithinBounds(pos)) {
                BlockPos actualPos = pos;
                BlockState actualState = blockState;
                if (blockState.getBlock() instanceof BlockBounding) {
                    TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
                    if (tile != null) {
                        actualPos = tile.getMainPos();
                        actualState = world.getBlockState(actualPos);
                    }
                }
                AttributeCustomSelectionBox customSelectionBox = Attribute.get(actualState, AttributeCustomSelectionBox.class);
                if (customSelectionBox != null) {
                    WireFrameRenderer renderWireFrame = null;
                    if (customSelectionBox.isJavaModel()) {
                        //If we use a TER to render the wire frame, grab the tile
                        TileEntity tile = WorldUtils.getTileEntity(world, actualPos);
                        if (tile != null) {
                            TileEntityRenderer<TileEntity> tileRenderer = TileEntityRendererDispatcher.instance.getRenderer(tile);
                            if (tileRenderer instanceof IWireFrameRenderer) {
                                IWireFrameRenderer wireFrameRenderer = (IWireFrameRenderer) tileRenderer;
                                if (wireFrameRenderer.hasSelectionBox(actualState)) {
                                    renderWireFrame = (buffer, matrixStack, state, red, green, blue, alpha) -> {
                                        if (wireFrameRenderer.isCombined()) {
                                            renderQuadsWireFrame(state, buffer, matrixStack.last().pose(), world.random, red, green, blue, alpha);
                                        }
                                        wireFrameRenderer.renderWireFrame(tile, event.getPartialTicks(), matrixStack, buffer, red, green, blue, alpha);
                                    };
                                }
                            }
                        }
                    } else {
                        //Otherwise, skip getting the tile and just grab the model
                        renderWireFrame = (buffer, matrixStack, state, red, green, blue, alpha) ->
                              renderQuadsWireFrame(state, buffer, matrixStack.last().pose(), world.random, red, green, blue, alpha);
                    }
                    if (renderWireFrame != null) {
                        matrix.pushPose();
                        Vector3d viewPosition = info.getPosition();
                        matrix.translate(actualPos.getX() - viewPosition.x, actualPos.getY() - viewPosition.y, actualPos.getZ() - viewPosition.z);
                        renderWireFrame.render(renderer.getBuffer(RenderType.lines()), matrix, actualState, 0, 0, 0, 0.4F);
                        matrix.popPose();
                        shouldCancel = true;
                    }
                }
            }
            profiler.pop();

            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemConfigurator)) {
                //If we are not holding a configurator, look if we are in the offhand
                stack = player.getOffhandItem();
                if (stack.isEmpty() || !(stack.getItem() instanceof ItemConfigurator)) {
                    if (shouldCancel) {
                        event.setCanceled(true);
                    }
                    return;
                }
            }
            profiler.push(ProfilerConstants.CONFIGURABLE_MACHINE);
            ConfiguratorMode state = ((ItemConfigurator) stack.getItem()).getMode(stack);
            if (state.isConfigurating()) {
                TransmissionType type = Objects.requireNonNull(state.getTransmission(), "Configurating state requires transmission type");
                TileEntity tile = WorldUtils.getTileEntity(world, pos);
                if (tile instanceof ISideConfiguration) {
                    ISideConfiguration configurable = (ISideConfiguration) tile;
                    TileComponentConfig config = configurable.getConfig();
                    if (config.supports(type)) {
                        Direction face = rayTraceResult.getDirection();
                        DataType dataType = config.getDataType(type, RelativeSide.fromDirections(configurable.getDirection(), face));
                        if (dataType != null) {
                            Vector3d viewPosition = info.getPosition();
                            matrix.pushPose();
                            matrix.translate(pos.getX() - viewPosition.x, pos.getY() - viewPosition.y, pos.getZ() - viewPosition.z);
                            MekanismRenderer.renderObject(getOverlayModel(face, type), matrix, renderer.getBuffer(Atlases.translucentCullBlockSheet()),
                                  MekanismRenderer.getColorARGB(dataType.getColor(), 0.6F), MekanismRenderer.FULL_LIGHT, OverlayTexture.NO_OVERLAY, FaceDisplay.FRONT);
                            matrix.popPose();
                        }
                    }
                }
            }
            profiler.pop();
            if (shouldCancel) {
                event.setCanceled(true);
            }
        }
    }

    private void renderQuadsWireFrame(BlockState state, IVertexBuilder buffer, Matrix4f matrix, Random rand, float red, float green, float blue, float alpha) {
        List<Vertex[]> allVertices = cachedWireFrames.computeIfAbsent(state, s -> {
            IBakedModel bakedModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(s);
            //TODO: Eventually we may want to add support for Model data
            IModelData modelData = EmptyModelData.INSTANCE;
            List<Vertex[]> vertices = new ArrayList<>();
            for (Direction direction : EnumUtils.DIRECTIONS) {
                QuadUtils.unpack(bakedModel.getQuads(s, direction, rand, modelData)).stream().map(Quad::getVertices).forEach(vertices::add);
            }
            QuadUtils.unpack(bakedModel.getQuads(s, null, rand, modelData)).stream().map(Quad::getVertices).forEach(vertices::add);
            return vertices;
        });
        renderVertexWireFrame(allVertices, buffer, matrix, red, green, blue, alpha);
    }

    public static void renderVertexWireFrame(List<Vertex[]> allVertices, IVertexBuilder buffer, Matrix4f matrix, float red, float green, float blue, float alpha) {
        for (Vertex[] vertices : allVertices) {
            Vector4f vertex = getVertex(matrix, vertices[0]);
            Vector3d normal = vertices[0].getNormal();
            Vector4f vertex2 = getVertex(matrix, vertices[1]);
            Vector3d normal2 = vertices[1].getNormal();
            Vector4f vertex3 = getVertex(matrix, vertices[2]);
            Vector3d normal3 = vertices[2].getNormal();
            Vector4f vertex4 = getVertex(matrix, vertices[3]);
            Vector3d normal4 = vertices[3].getNormal();
            buffer.vertex(vertex.x(), vertex.y(), vertex.z()).normal((float) normal.x(), (float) normal.y(), (float) normal.z()).color(red, green, blue, alpha).endVertex();
            buffer.vertex(vertex2.x(), vertex2.y(), vertex2.z()).normal((float) normal2.x(), (float) normal2.y(), (float) normal2.z()).color(red, green, blue, alpha).endVertex();

            buffer.vertex(vertex3.x(), vertex3.y(), vertex3.z()).normal((float) normal3.x(), (float) normal3.y(), (float) normal3.z()).color(red, green, blue, alpha).endVertex();
            buffer.vertex(vertex4.x(), vertex4.y(), vertex4.z()).normal((float) normal4.x(), (float) normal4.y(), (float) normal4.z()).color(red, green, blue, alpha).endVertex();

            buffer.vertex(vertex2.x(), vertex2.y(), vertex2.z()).normal((float) normal2.x(), (float) normal2.y(), (float) normal2.z()).color(red, green, blue, alpha).endVertex();
            buffer.vertex(vertex3.x(), vertex3.y(), vertex3.z()).normal((float) normal3.x(), (float) normal3.y(), (float) normal3.z()).color(red, green, blue, alpha).endVertex();

            buffer.vertex(vertex.x(), vertex.y(), vertex.z()).normal((float) normal.x(), (float) normal.y(), (float) normal.z()).color(red, green, blue, alpha).endVertex();
            buffer.vertex(vertex4.x(), vertex4.y(), vertex4.z()).normal((float) normal4.x(), (float) normal4.y(), (float) normal4.z()).color(red, green, blue, alpha).endVertex();
        }
    }

    private static Vector4f getVertex(Matrix4f matrix4f, Vertex vertex) {
        Vector4f vector4f = new Vector4f((float) vertex.getPos().x(), (float) vertex.getPos().y(), (float) vertex.getPos().z(), 1);
        vector4f.transform(matrix4f);
        return vector4f;
    }

    private void renderStatusBar(MatrixStack matrix, @Nonnull PlayerEntity player) {
        //TODO: use vanilla status bar text? Note, the vanilla status bar text stays a lot longer than we have our message
        // display for, so we would need to somehow modify it. This can be done via ATs but does cause it to always appear
        // to be more faded in color, and blinks to full color just before disappearing
        if (modeSwitchTimer > 1) {
            if (minecraft.screen == null && minecraft.font != null) {
                ItemStack stack = player.getMainHandItem();
                if (IModeItem.isModeItem(stack, EquipmentSlotType.MAINHAND)) {
                    ITextComponent scrollTextComponent = ((IModeItem) stack.getItem()).getScrollTextComponent(stack);
                    if (scrollTextComponent != null) {
                        int x = minecraft.getWindow().getGuiScaledWidth();
                        int y = minecraft.getWindow().getGuiScaledHeight();
                        int color = Color.rgbad(1, 1, 1, modeSwitchTimer / 100F).argb();
                        minecraft.font.draw(matrix, scrollTextComponent, (x - minecraft.font.width(scrollTextComponent)) / 2, y - 60, color);
                    }
                }
            }
            modeSwitchTimer--;
        }
    }

    private void renderJetpackSmoke(World world, Vector3d pos, Vector3d motion) {
        world.addParticle(MekanismParticleTypes.JETPACK_FLAME.getParticleType(), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        world.addParticle(MekanismParticleTypes.JETPACK_SMOKE.getParticleType(), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }

    private void drawString(MainWindow window, MatrixStack matrix, ITextComponent text, boolean leftSide, int y, int color) {
        FontRenderer font = minecraft.font;
        // Note that we always offset by 2 pixels when left or right aligned
        if (leftSide) {
            font.drawShadow(matrix, text, 2, y, color);
        } else {
            int width = font.width(text) + 2;
            font.drawShadow(matrix, text, window.getGuiScaledWidth() - width, y, color);
        }
    }

    private Model3D getOverlayModel(Direction side, TransmissionType type) {
        if (cachedOverlays.containsKey(side) && cachedOverlays.get(side).containsKey(type)) {
            return cachedOverlays.get(side).get(type);
        }
        Model3D toReturn = new Model3D();
        toReturn.setTexture(MekanismRenderer.overlays.get(type));
        MekanismRenderer.prepSingleFaceModelSize(toReturn, side);
        cachedOverlays.computeIfAbsent(side, s -> new EnumMap<>(TransmissionType.class)).put(type, toReturn);
        return toReturn;
    }

    @FunctionalInterface
    private interface WireFrameRenderer {

        void render(IVertexBuilder buffer, MatrixStack matrix, BlockState state, float red, float green, float blue, float alpha);
    }
}