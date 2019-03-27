package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.prefab.TileEntityEffectsBlock;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityChargepad extends TileEntityEffectsBlock {

    public boolean isActive;
    public boolean clientActive;

    public Random random = new Random();

    public TileEntityChargepad() {
        super("machine.chargepad", "Chargepad", BlockStateMachine.MachineType.CHARGEPAD.baseEnergy);
        inventory = NonNullList.withSize(0, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            isActive = false;
            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                  new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1,
                        getPos().getY() + 0.2, getPos().getZ() + 1));

            for (EntityLivingBase entity : entities) {
                if (entity instanceof EntityPlayer || entity instanceof EntityRobit) {
                    isActive = true;
                }

                if (getEnergy() > 0) {
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

            if (clientActive != isActive) {
                if (isActive) {
                    world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.1, getPos().getZ() + 0.5,
                          SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.8F);
                } else {
                    world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.1, getPos().getZ() + 0.5,
                          SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.7F);
                }

                setActive(isActive);
            }
        } else if (isActive) {
            world.spawnParticle(EnumParticleTypes.REDSTONE, getPos().getX() + random.nextDouble(),
                  getPos().getY() + 0.15, getPos().getZ() + random.nextDouble(), 0, 0, 0);
        }
    }

    @Override
    public boolean sideIsConsumer(EnumFacing side) {
        return side == EnumFacing.DOWN || side == facing.getOpposite();
    }

    @Override
    public boolean getActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;

        if (clientActive != active) {
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));
        }

        clientActive = active;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        isActive = nbtTags.getBoolean("isActive");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setBoolean("isActive", isActive);

        return nbtTags;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            clientActive = dataStream.readBoolean();

            if (clientActive != isActive) {
                isActive = clientActive;
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(isActive);
        return data;
    }

    @Override
    public boolean canSetFacing(int side) {
        return side != 0 && side != 1;
    }

    @Override
    public boolean renderUpdate() {
        return false;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }
}
