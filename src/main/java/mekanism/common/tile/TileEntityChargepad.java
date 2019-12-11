package mekanism.common.tile;

import java.util.List;
import java.util.Random;
import mekanism.common.MekanismBlock;
import mekanism.common.entity.EntityRobit;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityChargepad extends TileEntityMekanism {

    public Random random = new Random();

    public TileEntityChargepad() {
        super(MekanismBlock.CHARGEPAD);
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            boolean active = false;
            List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class,
                  new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.2, pos.getZ() + 1));

            for (LivingEntity entity : entities) {
                if (entity instanceof PlayerEntity || entity instanceof EntityRobit) {
                    active = getEnergy() > 0;
                }
                if (getActive()) {
                    if (entity instanceof EntityRobit) {
                        EntityRobit robit = (EntityRobit) entity;
                        double canGive = Math.min(getEnergy(), 1000);
                        double toGive = Math.min(robit.MAX_ELECTRICITY - robit.getEnergy(), canGive);
                        robit.setEnergy(robit.getEnergy() + toGive);
                        setEnergy(getEnergy() - toGive);
                    } else if (entity instanceof PlayerEntity) {
                        PlayerEntity player = (PlayerEntity) entity;
                        double prevEnergy = getEnergy();
                        for (ItemStack stack : player.inventory.armorInventory) {
                            ChargeUtils.charge(stack, this);
                            if (prevEnergy != getEnergy()) {
                                break;
                            }
                        }
                        for (ItemStack stack : player.inventory.mainInventory) {
                            ChargeUtils.charge(stack, this);
                            if (prevEnergy != getEnergy()) {
                                break;
                            }
                        }
                    }
                }
            }
            if (active != getActive()) {
                setActive(active);
            }
        } else if (getActive()) {
            world.addParticle(RedstoneParticleData.REDSTONE_DUST, getPos().getX() + random.nextDouble(), getPos().getY() + 0.15,
                  getPos().getZ() + random.nextDouble(), 0, 0, 0);
        }
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return side == Direction.DOWN || side == getOppositeDirection();
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        boolean wasActive = getActive();
        super.handlePacketData(dataStream);
        if (isRemote()) {
            //If the state changed play pressure plate sound
            if (wasActive != getActive()) {
                if (getActive()) {
                    world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.1, getPos().getZ() + 0.5,
                          SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.8F);
                } else {
                    world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.1, getPos().getZ() + 0.5,
                          SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.7F);
                }
            }
        }
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }
}