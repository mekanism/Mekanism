package mekanism.common.tile.prefab;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.base.TileNetworkList;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class TileEntityMachine extends TileEntityEffectsBlock implements IUpgradeTile, IRedstoneControl,
      ISecurityTile {

    public double prevEnergy;

    public double BASE_ENERGY_PER_TICK;

    public double energyPerTick;

    /**
     * This machine's current RedstoneControl type.
     */
    public RedstoneControl controlType = RedstoneControl.DISABLED;

    public TileComponentUpgrade upgradeComponent;
    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

    public TileEntityMachine(String sound, String name, double maxEnergy, double baseEnergyUsage, int upgradeSlot) {
        super(sound, name, maxEnergy);

        energyPerTick = BASE_ENERGY_PER_TICK = baseEnergyUsage;

        upgradeComponent = new TileComponentUpgrade(this, upgradeSlot);
        upgradeComponent.setSupported(Upgrade.MUFFLING);
    }

    @Override
    public boolean canSetFacing(int facing) {
        return facing != 0 && facing != 1;
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
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            controlType = RedstoneControl.values()[dataStream.readInt()];
            energyPerTick = dataStream.readDouble();
            maxEnergy = dataStream.readDouble();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(controlType.ordinal());
        data.add(energyPerTick);
        data.add(maxEnergy);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("controlType", controlType.ordinal());

        return nbtTags;
    }

    @Override
    public RedstoneControl getControlType() {
        return controlType;
    }

    @Override
    public void setControlType(RedstoneControl type) {
        controlType = type;
        MekanismUtils.saveChunk(this);
    }

    @Override
    public boolean canPulse() {
        return false;
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);

        if (upgrade == Upgrade.ENERGY) {
            maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
            energyPerTick = MekanismUtils.getBaseEnergyPerTick(this, BASE_ENERGY_PER_TICK);
            setEnergy(Math.min(getMaxEnergy(), getEnergy()));
        }
    }
}
