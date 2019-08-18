package mekanism.common;

import mekanism.api.gas.GasStack;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemGasMask;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemScubaTank;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class CommonPlayerTickHandler {

    public static boolean isOnGround(PlayerEntity player) {
        int x = MathHelper.floor(player.posX);
        int y = MathHelper.floor(player.posY - 0.01);
        int z = MathHelper.floor(player.posZ);
        BlockPos pos = new BlockPos(x, y, z);
        BlockState s = player.world.getBlockState(pos);
        AxisAlignedBB box = s.getShape(player.world, pos).getBoundingBox().offset(pos);
        AxisAlignedBB playerBox = player.getBoundingBox();
        return !s.getBlock().isAir(s, player.world, pos) && playerBox.offset(0, -0.01, 0).intersects(box);

    }

    public static boolean isGasMaskOn(PlayerEntity player) {
        ItemStack tank = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        ItemStack mask = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (!tank.isEmpty() && !mask.isEmpty()) {
            if (tank.getItem() instanceof ItemScubaTank && mask.getItem() instanceof ItemGasMask) {
                ItemScubaTank scubaTank = (ItemScubaTank) tank.getItem();
                if (scubaTank.getGas(tank) != null) {
                    return scubaTank.getFlowing(tank);
                }
            }
        }
        return false;
    }

    public static boolean isFlamethrowerOn(PlayerEntity player) {
        if (Mekanism.playerState.isFlamethrowerOn(player)) {
            ItemStack currentItem = player.inventory.getCurrentItem();
            return !currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower;
        }
        return false;
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent event) {
        if (event.phase == Phase.END && event.side == LogicalSide.SERVER) {
            tickEnd(event.player);
        }
    }

    public void tickEnd(PlayerEntity player) {
        ItemStack feetStack = player.getItemStackFromSlot(EquipmentSlotType.FEET);
        if (!feetStack.isEmpty() && feetStack.getItem() instanceof ItemFreeRunners && !player.isSneaking()) {
            player.stepHeight = 1.002F;
        } else if (player.stepHeight == 1.002F) {
            player.stepHeight = 0.6F;
        }

        if (isFlamethrowerOn(player)) {
            player.world.addEntity(new EntityFlame(player));
            if (!player.isCreative() && !player.isSpectator()) {
                ItemStack currentItem = player.inventory.getCurrentItem();
                ((ItemFlamethrower) currentItem.getItem()).useGas(currentItem);
            }
        }

        if (isJetpackOn(player)) {
            ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            ItemJetpack jetpack = (ItemJetpack) stack.getItem();
            JetpackMode mode = jetpack.getMode(stack);
            Vec3d motion = player.getMotion();
            if (mode == JetpackMode.NORMAL) {
                player.setMotion(0, Math.min(motion.getY() + 0.15D, 0.5D), 0);
            } else if (mode == JetpackMode.HOVER) {
                boolean ascending = Mekanism.keyMap.has(player, KeySync.ASCEND);
                boolean descending = Mekanism.keyMap.has(player, KeySync.DESCEND);
                if ((!ascending && !descending) || (ascending && descending)) {
                    if (motion.getY() > 0) {
                        player.setMotion(0, Math.max(motion.getY() - 0.15D, 0), 0);
                    } else if (motion.getY() < 0) {
                        if (!isOnGround(player)) {
                            player.setMotion(0, Math.min(motion.getY() + 0.15D, 0), 0);
                        }
                    }
                } else if (ascending) {
                    player.setMotion(0, Math.min(motion.getY() + 0.15D, 0.2D), 0);
                } else if (!isOnGround(player)) {
                    player.setMotion(0, Math.max(motion.getY() - 0.15D, -0.2D), 0);
                }
            }
            player.fallDistance = 0.0F;
            if (player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).connection.floatingTickCount = 0;
            }
            jetpack.useGas(stack);
        }

        if (isGasMaskOn(player)) {
            ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            ItemScubaTank tank = (ItemScubaTank) stack.getItem();
            final int max = 300;
            tank.useGas(stack);
            GasStack received = tank.useGas(stack, max - player.getAir());
            if (received != null) {
                player.setAir(player.getAir() + received.amount);
            }
            if (player.getAir() == max) {
                for (EffectInstance effect : player.getActivePotionEffects()) {
                    for (int i = 0; i < 9; i++) {
                        effect.tick(player);
                    }
                }
            }
        }
    }

    public boolean isJetpackOn(PlayerEntity player) {
        if (!player.isCreative() && !player.isSpectator()) {
            ItemStack chest = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (!chest.isEmpty() && chest.getItem() instanceof ItemJetpack) {
                ItemJetpack jetpack = (ItemJetpack) chest.getItem();
                if (jetpack.getGas(chest) != null) {
                    JetpackMode mode = jetpack.getMode(chest);
                    if (mode == JetpackMode.NORMAL) {
                        return Mekanism.keyMap.has(player, KeySync.ASCEND);
                    } else if (mode == JetpackMode.HOVER) {
                        boolean ascending = Mekanism.keyMap.has(player, KeySync.ASCEND);
                        boolean descending = Mekanism.keyMap.has(player, KeySync.DESCEND);
                        //if ((!ascending && !descending) || (ascending && descending) || descending)
                        //Simplifies to
                        if (!ascending || descending) {
                            return !isOnGround(player);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}