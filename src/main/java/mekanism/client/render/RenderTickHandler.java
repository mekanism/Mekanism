package mekanism.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.Pos3D;
import mekanism.common.ColorRGBA;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.IItemHUDProvider;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderTickHandler {

    private static double HUD_SCALE = 0.6;

    public static int modeSwitchTimer = 0;
    public Random rand = new Random();
    public Minecraft minecraft = Minecraft.getInstance();

    @SubscribeEvent
    public void tickEnd(RenderTickEvent event) {
        if (event.phase == Phase.END) {
            if (minecraft.player != null && minecraft.world != null && !minecraft.isGamePaused()) {
                FontRenderer font = minecraft.fontRenderer;
                if (font == null) {
                    return;
                }

                PlayerEntity player = minecraft.player;
                World world = minecraft.player.world;
                BlockRayTraceResult pos = MekanismUtils.rayTrace(player);
                if (pos.getType() != Type.MISS) {
                    BlockPos blockPos = pos.getPos();
                    if (MekanismUtils.isBlockLoaded(world, blockPos)) {
                        Block block = world.getBlockState(blockPos).getBlock();
                        if (block != Blocks.AIR && MekanismAPI.debug && minecraft.currentScreen == null && !minecraft.gameSettings.showDebugInfo) {
                            String tileDisplay = "";
                            TileEntity tile = MekanismUtils.getTileEntity(world, blockPos);
                            if (tile != null && tile.getClass() != null) {
                                tileDisplay = tile.getClass().getSimpleName();
                            }
                            font.drawStringWithShadow("Block: " + block.getTranslationKey(), 1, 1, 0x404040);
                            font.drawStringWithShadow("Metadata: " + world.getBlockState(blockPos), 1, 10, 0x404040);
                            font.drawStringWithShadow("Location: " + MekanismUtils.getCoordDisplay(blockPos), 1, 19, 0x404040);
                            font.drawStringWithShadow("TileEntity: " + tileDisplay, 1, 28, 0x404040);
                            font.drawStringWithShadow("Side: " + pos.getFace(), 1, 37, 0x404040);
                        }
                    }
                }

                //TODO: use vanilla status bar text? Note, the vanilla status bar text stays a lot longer than we have our message
                // display for, so we would need to somehow modify it. This can be done via ATs but does cause it to always appear
                // to be more faded in color, and blinks to full color just before disappearing
                if (modeSwitchTimer > 1 && minecraft.currentScreen == null && player.getHeldItemMainhand().getItem() instanceof ItemConfigurator) {
                    ItemStack stack = player.getHeldItemMainhand();
                    ConfiguratorMode mode = ((ItemConfigurator) stack.getItem()).getState(stack);

                    int x = minecraft.getMainWindow().getScaledWidth();
                    int y = minecraft.getMainWindow().getScaledHeight();
                    //TODO: Check this, though if we use vanilla status bar text it may be a lot simpler instead
                    String text = mode.getTextComponent().getFormattedText();
                    int color = new ColorRGBA(1, 1, 1, (float) modeSwitchTimer / 100F).argb();
                    font.drawString(text, x / 2 - font.getStringWidth(text) / 2, y - 60, color);
                }

                modeSwitchTimer = Math.max(modeSwitchTimer - 1, 0);

                if (minecraft.currentScreen == null && !minecraft.gameSettings.hideGUI && !player.isSpectator() && MekanismConfig.client.enableHUD.get()) {
                    int y = minecraft.getMainWindow().getScaledHeight();
                    boolean alignLeft = MekanismConfig.client.alignHUDLeft.get();
                    List<ITextComponent> renderStrings = new ArrayList<>();
                    for (EquipmentSlotType slotType : EquipmentSlotType.values()) {
                        ItemStack stack = player.getItemStackFromSlot(slotType);
                        if (stack.getItem() instanceof IItemHUDProvider) {
                            ((IItemHUDProvider) stack.getItem()).addHUDStrings(renderStrings, stack);
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
                    PlayerEntity p = minecraft.world.getPlayerByUuid(uuid);
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
                        PlayerEntity p = minecraft.world.getPlayerByUuid(uuid);
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
}