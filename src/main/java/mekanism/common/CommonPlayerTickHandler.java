package mekanism.common;

import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.gear.ItemGasMask;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonPlayerTickHandler {

    public static boolean isOnGround(PlayerEntity player) {
        int x = MathHelper.floor(player.getPosX());
        int y = MathHelper.floor(player.getPosY() - 0.01);
        int z = MathHelper.floor(player.getPosZ());
        BlockPos pos = new BlockPos(x, y, z);
        BlockState s = player.world.getBlockState(pos);
        VoxelShape shape = s.getShape(player.world, pos);
        if (shape.isEmpty()) {
            return false;
        }
        AxisAlignedBB playerBox = player.getBoundingBox();
        return !s.isAir(player.world, pos) && playerBox.offset(0, -0.01, 0).intersects(shape.getBoundingBox().offset(pos));

    }

    public static boolean isGasMaskOn(PlayerEntity player) {
        ItemStack tank = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        ItemStack mask = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        return !tank.isEmpty() && !mask.isEmpty() && tank.getItem() instanceof ItemScubaTank && mask.getItem() instanceof ItemGasMask && GasUtils.hasGas(tank) &&
               ((ItemScubaTank) tank.getItem()).getFlowing(tank);
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
        if (event.phase == Phase.END && event.side.isServer()) {
            tickEnd(event.player);
        }
    }

    public void tickEnd(PlayerEntity player) {
        ItemStack feetStack = player.getItemStackFromSlot(EquipmentSlotType.FEET);
        if (!feetStack.isEmpty() && feetStack.getItem() instanceof ItemFreeRunners && !player.isShiftKeyDown()) {
            player.stepHeight = 1.002F;
        } else if (player.stepHeight == 1.002F) {
            player.stepHeight = 0.6F;
        }

        if (isFlamethrowerOn(player)) {
            player.world.addEntity(new EntityFlame(player));
            if (!player.isCreative() && !player.isSpectator()) {
                ItemStack currentItem = player.inventory.getCurrentItem();
                ((ItemFlamethrower) currentItem.getItem()).useGas(currentItem, 1);
            }
        }

        if (isJetpackOn(player)) {
            ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            ItemJetpack jetpack = (ItemJetpack) stack.getItem();
            JetpackMode mode = jetpack.getMode(stack);
            Vec3d motion = player.getMotion();
            if (mode == JetpackMode.NORMAL) {
                player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0.5D), motion.getZ());
            } else if (mode == JetpackMode.HOVER) {
                boolean ascending = Mekanism.keyMap.has(player, KeySync.ASCEND);
                boolean descending = Mekanism.keyMap.has(player, KeySync.DESCEND);
                if ((!ascending && !descending) || (ascending && descending)) {
                    if (motion.getY() > 0) {
                        player.setMotion(motion.getX(), Math.max(motion.getY() - 0.15D, 0), motion.getZ());
                    } else if (motion.getY() < 0) {
                        if (!isOnGround(player)) {
                            player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0), motion.getZ());
                        }
                    }
                } else if (ascending) {
                    player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0.2D), motion.getZ());
                } else if (!isOnGround(player)) {
                    player.setMotion(motion.getX(), Math.max(motion.getY() - 0.15D, -0.2D), motion.getZ());
                }
            }
            player.fallDistance = 0.0F;
            if (player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).connection.floatingTickCount = 0;
            }
            jetpack.useGas(stack, 1);
        }

        if (isGasMaskOn(player)) {
            ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            ItemScubaTank tank = (ItemScubaTank) stack.getItem();
            final int max = 300;
            tank.useGas(stack, 1);
            GasStack received = tank.useGas(stack, max - player.getAir());
            if (!received.isEmpty()) {
                player.setAir(player.getAir() + received.getAmount());
            }
            if (player.getAir() == max) {
                for (EffectInstance effect : player.getActivePotionEffects()) {
                    for (int i = 0; i < 9; i++) {
                        effect.tick(player, () -> MekanismUtils.onChangedPotionEffect(player, effect, true));
                    }
                }
            }
        }
    }

    public static boolean isJetpackOn(PlayerEntity player) {
        if (!player.isCreative() && !player.isSpectator()) {
            ItemStack chest = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (!chest.isEmpty() && chest.getItem() instanceof ItemJetpack && GasUtils.hasGas(chest)) {
                JetpackMode mode = ((ItemJetpack) chest.getItem()).getMode(chest);
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
        return false;
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        LivingEntity base = event.getEntityLiving();
        //Gas Mask checks
        ItemStack headStack = base.getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (!headStack.isEmpty() && headStack.getItem() instanceof ItemGasMask) {
            ItemStack chestStack = base.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (!chestStack.isEmpty() && chestStack.getItem() instanceof ItemScubaTank && ((ItemScubaTank) chestStack.getItem()).getFlowing(chestStack) &&
                GasUtils.hasGas(chestStack) && event.getSource() == DamageSource.MAGIC) {
                event.setCanceled(true);
            }
        }
        //Free runner checks
        ItemStack feetStack = base.getItemStackFromSlot(EquipmentSlotType.FEET);
        if (!feetStack.isEmpty() && feetStack.getItem() instanceof ItemFreeRunners) {
            ItemFreeRunners boots = (ItemFreeRunners) feetStack.getItem();
            if (boots.getMode(feetStack) == FreeRunnerMode.NORMAL && event.getSource() == DamageSource.FALL) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(feetStack, 0);
                if (energyContainer != null) {
                    energyContainer.extract(FloatingLong.createConst(event.getAmount() * 50), Action.EXECUTE, AutomationType.MANUAL);
                    event.setCanceled(true);
                }
            }
        }
    }
}