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

public abstract class TileEntityMachine extends TileEntityNoisyBlock implements IUpgradeTile, IRedstoneControl,
      ISecurityTile {

    public boolean isActive;

    public double prevEnergy;

    public double BASE_ENERGY_PER_TICK;

    public double energyPerTick;

    private long lastActive = -1;

    // Number of ticks that a machine can be inactive before it's considered not
    // recently active
    private final int RECENT_THRESHOLD = 100;

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
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote && !isActive && lastActive > 0) {
            long updateDiff = world.getTotalWorldTime() - lastActive;
            if (updateDiff > RECENT_THRESHOLD) {
                MekanismUtils.updateBlock(world, getPos());
                lastActive = -1;
            }
        }
    }

    @Override
    public boolean canSetFacing(int facing) {
        return facing != 0 && facing != 1;
    }

    @Override
    public boolean getActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        boolean stateChange = (isActive != active);

        if (stateChange) {
            isActive = active;
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));
        }
    }

    public boolean wasActiveRecently() {
        // If the machine is currently active or it flipped off within our threshold,
        // we'll consider it recently active.
        return isActive || (lastActive > 0 && (world.getTotalWorldTime() - lastActive) < RECENT_THRESHOLD);
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
            boolean newActive = dataStream.readBoolean();
            controlType = RedstoneControl.values()[dataStream.readInt()];
            energyPerTick = dataStream.readDouble();
            maxEnergy = dataStream.readDouble();

            boolean stateChange = (newActive != isActive);
            isActive = newActive;

            if (stateChange && !isActive) {
                // Switched off; note the time
                lastActive = world.getTotalWorldTime();
            } else if (stateChange && isActive) {
                // Switching on; if lastActive is not currently set, trigger a lighting update
                // and make sure lastActive is clear
                if (lastActive == -1) {
                    MekanismUtils.updateBlock(world, getPos());
                }
                lastActive = -1;
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(isActive);
        data.add(controlType.ordinal());
        data.add(energyPerTick);
        data.add(maxEnergy);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        isActive = nbtTags.getBoolean("isActive");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setBoolean("isActive", isActive);
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
