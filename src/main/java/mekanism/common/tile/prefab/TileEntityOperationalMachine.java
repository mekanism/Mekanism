package mekanism.common.tile.prefab;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.Upgrade;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class TileEntityOperationalMachine extends TileEntityMachine implements IComparatorSupport {

    public int operatingTicks;

    public int BASE_TICKS_REQUIRED;

    public int ticksRequired;

    protected TileEntityOperationalMachine(String sound, IBlockProvider blockProvider, int upgradeSlot, int baseTicksRequired) {
        super(sound, blockProvider, upgradeSlot);
        ticksRequired = BASE_TICKS_REQUIRED = baseTicksRequired;
    }

    public double getScaledProgress() {
        return (double) operatingTicks / (double) ticksRequired;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            operatingTicks = dataStream.readInt();
            ticksRequired = dataStream.readInt();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(operatingTicks);
        data.add(ticksRequired);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        operatingTicks = nbtTags.getInteger("operatingTicks");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setInteger("operatingTicks", operatingTicks);
        return nbtTags;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        switch (upgrade) {
            case ENERGY:
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage())); // incorporate speed upgrades
                break;
            case SPEED:
                ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
                break;
            default:
                break;
        }
    }

    @Override
    public int getRedstoneLevel() {
        return Container.calcRedstoneFromInventory(this);
    }
}