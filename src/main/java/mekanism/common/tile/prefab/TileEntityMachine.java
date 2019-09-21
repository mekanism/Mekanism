package mekanism.common.tile.prefab;

import mekanism.api.TileNetworkList;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.PacketBuffer;

//TODO
public abstract class TileEntityMachine extends TileEntityMekanism implements IUpgradeTile {

    public TileComponentUpgrade<TileEntityMachine> upgradeComponent;

    public TileEntityMachine(IBlockProvider blockProvider, int upgradeSlot) {
        super(blockProvider);
        upgradeComponent = new TileComponentUpgrade<>(this, upgradeSlot);
        upgradeComponent.setSupported(Upgrade.MUFFLING);
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
            setEnergyPerTick(dataStream.readDouble());
            setMaxEnergy(dataStream.readDouble());
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(getEnergyPerTick());
        data.add(getMaxEnergy());
        return data;
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        //TODO: Should this go away
        if (upgrade == Upgrade.ENERGY) {
            setMaxEnergy(MekanismUtils.getMaxEnergy(this, getBaseStorage()));
            setEnergyPerTick(MekanismUtils.getBaseEnergyPerTick(this, getBaseUsage()));
            setEnergy(Math.min(getMaxEnergy(), getEnergy()));
        }
    }
}