package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.Upgrade;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class TileEntityOperationalMachine extends TileEntityMachine implements IComparatorSupport {

    public int operatingTicks;

    public int BASE_TICKS_REQUIRED;

    public int ticksRequired;

    protected TileEntityOperationalMachine(IBlockProvider blockProvider, int upgradeSlot, int baseTicksRequired) {
        super(blockProvider, upgradeSlot);
        ticksRequired = BASE_TICKS_REQUIRED = baseTicksRequired;
    }

    public double getScaledProgress() {
        return (double) operatingTicks / (double) ticksRequired;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
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
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        operatingTicks = nbtTags.getInt("operatingTicks");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("operatingTicks", operatingTicks);
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