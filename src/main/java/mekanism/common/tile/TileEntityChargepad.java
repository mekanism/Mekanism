package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.MekanismBlock;
import mekanism.common.entity.EntityRobit;
import mekanism.common.tile.prefab.TileEntityEffectsBlock;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityChargepad extends TileEntityEffectsBlock {

    public Random random = new Random();

    public TileEntityChargepad() {
        super(MekanismBlock.CHARGEPAD);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            isActive = false;
            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                  new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.2, pos.getZ() + 1));

            for (EntityLivingBase entity : entities) {
                if (entity instanceof EntityPlayer || entity instanceof EntityRobit) {
                    isActive = getEnergy() > 0;
                }
                if (getActive()) {
                    if (entity instanceof EntityRobit) {
                        EntityRobit robit = (EntityRobit) entity;
                        double canGive = Math.min(getEnergy(), 1000);
                        double toGive = Math.min(robit.MAX_ELECTRICITY - robit.getEnergy(), canGive);
                        robit.setEnergy(robit.getEnergy() + toGive);
                        setEnergy(getEnergy() - toGive);
                    } else if (entity instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) entity;
                        double prevEnergy = getEnergy();
                        for (ItemStack itemstack : player.inventory.armorInventory) {
                            ChargeUtils.charge(itemstack, this);
                            if (prevEnergy != getEnergy()) {
                                break;
                            }
                        }
                        for (ItemStack itemstack : player.inventory.mainInventory) {
                            ChargeUtils.charge(itemstack, this);
                            if (prevEnergy != getEnergy()) {
                                break;
                            }
                        }
                    }
                }
            }
        } else if (getActive()) {
            world.spawnParticle(EnumParticleTypes.REDSTONE, getPos().getX() + random.nextDouble(), getPos().getY() + 0.15,
                  getPos().getZ() + random.nextDouble(), 0, 0, 0);
        }
    }

    @Override
    public boolean canReceiveEnergy(EnumFacing side) {
        return side == EnumFacing.DOWN || side == getOppositeDirection();
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        boolean wasActive = getActive();
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            //If the state changed play pressure plate sound
            if (wasActive != getActive()) {
                if (getActive()) {
                    world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.1, getPos().getZ() + 0.5,
                          SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.8F);
                } else {
                    world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.1, getPos().getZ() + 0.5,
                          SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.7F);
                }
            }
        }
    }

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
    }

    @Override
    public boolean renderUpdate() {
        return false;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }
}