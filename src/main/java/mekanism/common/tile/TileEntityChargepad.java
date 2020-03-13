package mekanism.common.tile;

import java.util.List;
import java.util.Random;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityChargepad extends TileEntityMekanism {

    public Random random = new Random();

    public TileEntityChargepad() {
        super(MekanismBlocks.CHARGEPAD);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        boolean active = false;
        //Use 0.4 for y so as to catch entities that are partially standing on the back pane
        List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class,
              new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.4, pos.getZ() + 1));

        for (LivingEntity entity : entities) {
            if (entity instanceof PlayerEntity || entity instanceof EntityRobit) {
                active = getEnergy() > 0;
            }
            if (getActive()) {
                if (entity instanceof EntityRobit) {
                    EntityRobit robit = (EntityRobit) entity;
                    double canGive = Math.min(getEnergy(), 1_000);
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
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (getActive()) {
            world.addParticle(RedstoneParticleData.REDSTONE_DUST, getPos().getX() + random.nextDouble(), getPos().getY() + 0.15,
                  getPos().getZ() + random.nextDouble(), 0, 0, 0);
        }
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return side == Direction.DOWN || side == getOppositeDirection();
    }

    @Override
    public void setActive(boolean active) {
        boolean wasActive = getActive();
        super.setActive(active);
        if (wasActive != active) {
            //If the state changed play pressure plate sound
            SoundEvent sound;
            float pitch;
            if (active) {
                sound = SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON;
                pitch = 0.8F;
            } else {
                sound = SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF;
                pitch = 0.7F;
            }
            world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.1, getPos().getZ() + 0.5, sound, SoundCategory.BLOCKS, 0.3F, pitch);
        }
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }
}