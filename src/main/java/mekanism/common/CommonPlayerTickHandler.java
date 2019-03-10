package mekanism.common;

import mekanism.api.gas.GasStack;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemScubaTank;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class CommonPlayerTickHandler {

    public static boolean isOnGround(EntityPlayer player) {
        int x = MathHelper.floor(player.posX);
        int y = MathHelper.floor(player.posY - 0.01);
        int z = MathHelper.floor(player.posZ);

        BlockPos pos = new BlockPos(x, y, z);
        IBlockState s = player.world.getBlockState(pos);
        AxisAlignedBB box = s.getBoundingBox(player.world, pos).offset(pos);
        AxisAlignedBB playerBox = player.getEntityBoundingBox();

        return !s.getBlock().isAir(s, player.world, pos) && playerBox.offset(0, -0.01, 0).intersects(box);

    }

    public static boolean isGasMaskOn(EntityPlayer player) {
        ItemStack tank = player.inventory.armorInventory.get(2);
        ItemStack mask = player.inventory.armorInventory.get(3);

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

    public static boolean isFlamethrowerOn(EntityPlayer player) {
        if (Mekanism.playerState.isFlamethrowerOn(player)) {
            return !player.inventory.getCurrentItem().isEmpty() && player.inventory.getCurrentItem()
                  .getItem() instanceof ItemFlamethrower;
        }

        return false;
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent event) {
        if (event.phase == Phase.END && event.side == Side.SERVER) {
            tickEnd(event.player);
        }
    }

    public void tickEnd(EntityPlayer player) {
        ItemStack feetStack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);

        if (!feetStack.isEmpty() && feetStack.getItem() instanceof ItemFreeRunners) {
            player.stepHeight = 1.002F;
        } else {
            if (player.stepHeight == 1.002F) {
                player.stepHeight = 0.6F;
            }
        }

        if (isFlamethrowerOn(player)) {
            player.world.spawnEntity(new EntityFlame(player));

            if (!(player.isCreative() || player.isSpectator())) {
                ((ItemFlamethrower) player.inventory.getCurrentItem().getItem())
                      .useGas(player.inventory.getCurrentItem());
            }
        }

        if (isJetpackOn(player)) {
            ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            ItemJetpack jetpack = (ItemJetpack) stack.getItem();

            if (jetpack.getMode(stack) == JetpackMode.NORMAL) {
                player.motionY = Math.min(player.motionY + 0.15D, 0.5D);
            } else if (jetpack.getMode(stack) == JetpackMode.HOVER) {
                if ((!Mekanism.keyMap.has(player, KeySync.ASCEND) && !Mekanism.keyMap.has(player, KeySync.DESCEND)) || (
                      Mekanism.keyMap.has(player, KeySync.ASCEND) && Mekanism.keyMap.has(player, KeySync.DESCEND))) {
                    if (player.motionY > 0) {
                        player.motionY = Math.max(player.motionY - 0.15D, 0);
                    } else if (player.motionY < 0) {
                        if (!isOnGround(player)) {
                            player.motionY = Math.min(player.motionY + 0.15D, 0);
                        }
                    }
                } else {
                    if (Mekanism.keyMap.has(player, KeySync.ASCEND)) {
                        player.motionY = Math.min(player.motionY + 0.15D, 0.2D);
                    } else if (Mekanism.keyMap.has(player, KeySync.DESCEND)) {
                        if (!isOnGround(player)) {
                            player.motionY = Math.max(player.motionY - 0.15D, -0.2D);
                        }
                    }
                }
            }

            player.fallDistance = 0.0F;

            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).connection.floatingTickCount = 0;
            }

            jetpack.useGas(stack);
        }

        if (isGasMaskOn(player)) {
            ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            ItemScubaTank tank = (ItemScubaTank) stack.getItem();

            final int max = 300;

            tank.useGas(stack);
            GasStack received = tank.useGas(stack, max - player.getAir());

            if (received != null) {
                player.setAir(player.getAir() + received.amount);
            }

            if (player.getAir() == max) {
                for (Object obj : player.getActivePotionEffects()) {
                    if (obj instanceof PotionEffect) {
                        for (int i = 0; i < 9; i++) {
                            ((PotionEffect) obj).onUpdate(player);
                        }
                    }
                }
            }
        }
    }

    public boolean isJetpackOn(EntityPlayer player) {
        ItemStack stack = player.inventory.armorInventory.get(2);

        if (!stack.isEmpty() && !(player.isCreative() || player.isSpectator())) {
            if (stack.getItem() instanceof ItemJetpack) {
                ItemJetpack jetpack = (ItemJetpack) stack.getItem();

                if (jetpack.getGas(stack) != null) {
                    if ((Mekanism.keyMap.has(player, KeySync.ASCEND) && jetpack.getMode(stack) == JetpackMode.NORMAL)) {
                        return true;
                    } else if (jetpack.getMode(stack) == JetpackMode.HOVER) {
                        if ((!Mekanism.keyMap.has(player, KeySync.ASCEND) && !Mekanism.keyMap
                              .has(player, KeySync.DESCEND)) || (Mekanism.keyMap.has(player, KeySync.ASCEND)
                              && Mekanism.keyMap.has(player, KeySync.DESCEND))) {
                            return !isOnGround(player);
                        } else if (Mekanism.keyMap.has(player, KeySync.DESCEND)) {
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
