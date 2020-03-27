package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import mekanism.api.Pos3D;
import mekanism.api.RelativeSide;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.ColorRGBA;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.IItemHUDProvider;
import mekanism.common.item.IModeItem;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderTickHandler {

    private static final Map<Direction, Map<TransmissionType, Model3D>> cachedOverlays = new EnumMap<>(Direction.class);

    private static double HUD_SCALE = 0.6;

    public static int modeSwitchTimer = 0;
    public Random rand = new Random();
    public Minecraft minecraft = Minecraft.getInstance();

    public static void resetCachedOverlays() {
        cachedOverlays.clear();
    }

    @SubscribeEvent
    public void tickEnd(RenderTickEvent event) {
        if (event.phase == Phase.END) {
            if (minecraft.player != null && minecraft.player.world != null && !minecraft.isGamePaused() && minecraft.fontRenderer != null) {
                FontRenderer font = minecraft.fontRenderer;
                PlayerEntity player = minecraft.player;
                World world = minecraft.player.world;
                //TODO: use vanilla status bar text? Note, the vanilla status bar text stays a lot longer than we have our message
                // display for, so we would need to somehow modify it. This can be done via ATs but does cause it to always appear
                // to be more faded in color, and blinks to full color just before disappearing
                if (modeSwitchTimer > 1 && minecraft.currentScreen == null && IModeItem.isModeItem(player, EquipmentSlotType.MAINHAND)) {
                    ItemStack stack = player.getHeldItemMainhand();
                    ITextComponent scrollTextComponent = ((IModeItem) stack.getItem()).getScrollTextComponent(stack);
                    if (scrollTextComponent != null) {
                        int x = minecraft.getMainWindow().getScaledWidth();
                        int y = minecraft.getMainWindow().getScaledHeight();
                        String text = scrollTextComponent.getFormattedText();
                        int color = new ColorRGBA(1, 1, 1, (float) modeSwitchTimer / 100F).argb();
                        font.drawString(text, x / 2 - font.getStringWidth(text) / 2, y - 60, color);
                    }
                }

                modeSwitchTimer = Math.max(modeSwitchTimer - 1, 0);

                if (minecraft.currentScreen == null && !minecraft.gameSettings.hideGUI && !player.isSpectator() && MekanismConfig.client.enableHUD.get()) {
                    int y = minecraft.getMainWindow().getScaledHeight();
                    boolean alignLeft = MekanismConfig.client.alignHUDLeft.get();
                    List<ITextComponent> renderStrings = new ArrayList<>();
                    for (EquipmentSlotType slotType : EquipmentSlotType.values()) {
                        ItemStack stack = player.getItemStackFromSlot(slotType);
                        if (stack.getItem() instanceof IItemHUDProvider) {
                            ((IItemHUDProvider) stack.getItem()).addHUDStrings(renderStrings, stack, slotType);
                        }
                    }

                    RenderSystem.pushMatrix();
                    RenderSystem.scaled(HUD_SCALE, HUD_SCALE, HUD_SCALE);
                    int start = 2 + renderStrings.size() * 9;
                    for (ITextComponent text : renderStrings) {
                        drawString(text, alignLeft, (int) (y * (1 / HUD_SCALE)) - start, 0xc8c8c8);
                        start -= 9;
                    }
                    RenderSystem.popMatrix();
                }

                // Traverse a copy of jetpack state and do animations
                for (UUID uuid : Mekanism.playerState.getActiveJetpacks()) {
                    PlayerEntity p = world.getPlayerByUuid(uuid);
                    if (p == null) {
                        continue;
                    }
                    Pos3D playerPos = new Pos3D(p).translate(0, 1.7, 0);
                    float random = (rand.nextFloat() - 0.5F) * 0.1F;
                    Pos3D vLeft = new Pos3D(-0.43, -0.55, -0.54).rotatePitch(p.isShiftKeyDown() ? 20 : 0).rotateYaw(p.renderYawOffset);
                    Pos3D vRight = new Pos3D(0.43, -0.55, -0.54).rotatePitch(p.isShiftKeyDown() ? 20 : 0).rotateYaw(p.renderYawOffset);
                    Pos3D vCenter = new Pos3D((rand.nextFloat() - 0.5F) * 0.4F, -0.86, -0.30).rotatePitch(p.isShiftKeyDown() ? 25 : 0).rotateYaw(p.renderYawOffset);

                    Pos3D rLeft = vLeft.scale(random);
                    Pos3D rRight = vRight.scale(random);

                    Pos3D mLeft = vLeft.scale(0.2).translate(new Pos3D(p.getMotion()));
                    Pos3D mRight = vRight.scale(0.2).translate(new Pos3D(p.getMotion()));
                    Pos3D mCenter = vCenter.scale(0.2).translate(new Pos3D(p.getMotion()));

                    mLeft = mLeft.translate(rLeft);
                    mRight = mRight.translate(rRight);

                    Pos3D v = playerPos.translate(vLeft).translate(new Pos3D(p.getMotion()));
                    world.addParticle((BasicParticleType) MekanismParticleTypes.JETPACK_FLAME.getParticleType(), v.x, v.y, v.z, mLeft.x, mLeft.y, mLeft.z);
                    world.addParticle((BasicParticleType) MekanismParticleTypes.JETPACK_SMOKE.getParticleType(), v.x, v.y, v.z, mLeft.x, mLeft.y, mLeft.z);

                    v = playerPos.translate(vRight).translate(new Pos3D(p.getMotion()));
                    world.addParticle((BasicParticleType) MekanismParticleTypes.JETPACK_FLAME.getParticleType(), v.x, v.y, v.z, mRight.x, mRight.y, mRight.z);
                    world.addParticle((BasicParticleType) MekanismParticleTypes.JETPACK_SMOKE.getParticleType(), v.x, v.y, v.z, mRight.x, mRight.y, mRight.z);

                    v = playerPos.translate(vCenter).translate(new Pos3D(p.getMotion()));
                    world.addParticle((BasicParticleType) MekanismParticleTypes.JETPACK_FLAME.getParticleType(), v.x, v.y, v.z, mCenter.x, mCenter.y, mCenter.z);
                    world.addParticle((BasicParticleType) MekanismParticleTypes.JETPACK_SMOKE.getParticleType(), v.x, v.y, v.z, mCenter.x, mCenter.y, mCenter.z);
                }

                // Traverse a copy of gasmask state and do animations
                if (world.getDayTime() % 4 == 0) {
                    for (UUID uuid : Mekanism.playerState.getActiveGasmasks()) {
                        PlayerEntity p = world.getPlayerByUuid(uuid);
                        if (p == null || !p.isInWater()) {
                            continue;
                        }

                        Pos3D playerPos = new Pos3D(p).translate(0, 1.7, 0);

                        Pos3D vec = new Pos3D(0.4, 0.4, 0.4).multiply(new Pos3D(p.getLook(1))).translate(0, -0.2, 0);
                        Pos3D motion = vec.scale(0.2).translate(new Pos3D(p.getMotion()));

                        Pos3D v = playerPos.translate(vec);
                        world.addParticle((BasicParticleType) MekanismParticleTypes.SCUBA_BUBBLE.getParticleType(), v.x, v.y, v.z, motion.x, motion.y + 0.2, motion.z);
                    }
                }

                // Traverse a copy of flamethrower state and do animations
                if (world.getDayTime() % 4 == 0) {
                    for (PlayerEntity p : world.getPlayers()) {
                        if (!Mekanism.playerState.isFlamethrowerOn(p) && !p.isSwingInProgress) {
                            ItemStack currentItem = p.inventory.getCurrentItem();
                            if (!currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower && GasUtils.hasGas(currentItem)) {
                                Pos3D playerPos = new Pos3D(p);
                                Pos3D flameVec;
                                double flameXCoord = 0;
                                double flameYCoord = 1.5;
                                double flameZCoord = 0;
                                Vec3d motion = p.getMotion();
                                Pos3D flameMotion = new Pos3D(motion.getX(), p.onGround ? 0 : motion.getY(), motion.getZ());
                                if (player == p && minecraft.gameSettings.thirdPersonView == 0) {
                                    flameVec = new Pos3D(1, 1, 1).multiply(p.getLook(1)).rotateYaw(5).translate(flameXCoord, flameYCoord + 0.1, flameZCoord);
                                } else {
                                    flameXCoord += 0.25F;
                                    flameXCoord -= 0.45F;
                                    flameZCoord += 0.15F;
                                    if (p.isShiftKeyDown()) {
                                        flameYCoord -= 0.55F;
                                        flameZCoord -= 0.15F;
                                    }
                                    flameYCoord -= 0.5F;
                                    flameZCoord += 1.05F;
                                    flameVec = new Pos3D(flameXCoord, flameYCoord, flameZCoord).rotateYaw(p.renderYawOffset);
                                }
                                Pos3D mergedVec = playerPos.translate(flameVec);
                                world.addParticle((BasicParticleType) MekanismParticleTypes.JETPACK_FLAME.getParticleType(),
                                      mergedVec.x, mergedVec.y, mergedVec.z, flameMotion.x, flameMotion.y, flameMotion.z);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void configurableMachine(DrawHighlightEvent.HighlightBlock event) {
        PlayerEntity player = minecraft.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getHeldItem(Hand.MAIN_HAND);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemConfigurator)) {
            //If we are not holding a configurator, look if we are in the offhand
            stack = player.getHeldItem(Hand.OFF_HAND);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemConfigurator)) {
                return;
            }
        }
        World world = player.getEntityWorld();
        IProfiler profiler = world.getProfiler();
        profiler.startSection(ProfilerConstants.CONFIGURABLE_MACHINE);
        BlockRayTraceResult rayTraceResult = event.getTarget();
        if (!rayTraceResult.getType().equals(Type.MISS)) {
            ConfiguratorMode state = ((ItemConfigurator) stack.getItem()).getState(stack);
            if (state.isConfigurating()) {
                TransmissionType type = Objects.requireNonNull(state.getTransmission(), "Configurating state requires transmission type");
                BlockPos pos = rayTraceResult.getPos();
                TileEntity tile = MekanismUtils.getTileEntity(world, pos);
                if (tile instanceof ISideConfiguration) {
                    ISideConfiguration configurable = (ISideConfiguration) tile;
                    TileComponentConfig config = configurable.getConfig();
                    if (config.supports(type)) {
                        Direction face = rayTraceResult.getFace();
                        DataType dataType = config.getDataType(type, RelativeSide.fromDirections(configurable.getOrientation(), face));
                        if (dataType != null) {
                            Vec3d viewPosition = event.getInfo().getProjectedView();
                            MatrixStack matrix = event.getMatrix();
                            matrix.push();
                            matrix.translate(pos.getX() - viewPosition.x, pos.getY() - viewPosition.y, pos.getZ() - viewPosition.z);
                            MekanismRenderer.renderObject(getOverlayModel(face, type), matrix, event.getBuffers().getBuffer(MekanismRenderType.resizableCuboid()),
                                  MekanismRenderer.getColorARGB(dataType.getColor(), 0.6F), MekanismRenderer.FULL_LIGHT);
                            matrix.pop();
                        }
                    }
                }
            }
        }
        profiler.endSection();
    }

    private void drawString(ITextComponent textComponent, boolean leftSide, int y, int color) {
        String s = textComponent.getFormattedText();
        FontRenderer font = minecraft.fontRenderer;
        // Note that we always offset by 2 pixels when left or right aligned
        if (leftSide) {
            font.drawStringWithShadow(s, 2, y, color);
        } else {
            int width = font.getStringWidth(s) + 2;
            font.drawStringWithShadow(s, minecraft.getMainWindow().getScaledWidth() - width, y, color);
        }
    }

    private Model3D getOverlayModel(Direction side, TransmissionType type) {
        if (cachedOverlays.containsKey(side) && cachedOverlays.get(side).containsKey(type)) {
            return cachedOverlays.get(side).get(type);
        }

        Model3D toReturn = new Model3D();
        toReturn.setTexture(MekanismRenderer.overlays.get(type));

        if (cachedOverlays.containsKey(side)) {
            cachedOverlays.get(side).put(type, toReturn);
        } else {
            Map<TransmissionType, Model3D> map = new EnumMap<>(TransmissionType.class);
            map.put(type, toReturn);
            cachedOverlays.put(side, map);
        }

        switch (side) {
            case DOWN:
                toReturn.minY = -.01;
                toReturn.maxY = -.001;

                toReturn.minX = 0;
                toReturn.minZ = 0;
                toReturn.maxX = 1;
                toReturn.maxZ = 1;
                break;
            case UP:
                toReturn.minY = 1.001;
                toReturn.maxY = 1.01;

                toReturn.minX = 0;
                toReturn.minZ = 0;
                toReturn.maxX = 1;
                toReturn.maxZ = 1;
                break;
            case NORTH:
                toReturn.minZ = -.01;
                toReturn.maxZ = -.001;

                toReturn.minX = 0;
                toReturn.minY = 0;
                toReturn.maxX = 1;
                toReturn.maxY = 1;
                break;
            case SOUTH:
                toReturn.minZ = 1.001;
                toReturn.maxZ = 1.01;

                toReturn.minX = 0;
                toReturn.minY = 0;
                toReturn.maxX = 1;
                toReturn.maxY = 1;
                break;
            case WEST:
                toReturn.minX = -.01;
                toReturn.maxX = -.001;

                toReturn.minY = 0;
                toReturn.minZ = 0;
                toReturn.maxY = 1;
                toReturn.maxZ = 1;
                break;
            case EAST:
                toReturn.minX = 1.001;
                toReturn.maxX = 1.01;

                toReturn.minY = 0;
                toReturn.minZ = 0;
                toReturn.maxY = 1;
                toReturn.maxZ = 1;
                break;
        }
        return toReturn;
    }
}