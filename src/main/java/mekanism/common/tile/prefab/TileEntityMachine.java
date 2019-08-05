package mekanism.common.tile.prefab;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.Upgrade;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.FMLCommonHandler;

//TODO
public abstract class TileEntityMachine extends TileEntityEffectsBlock implements IUpgradeTile, IRedstoneControl, ISecurityTile {

    public double prevEnergy;

    /**
     * This machine's current RedstoneControl type.
     */
    private RedstoneControl controlType = RedstoneControl.DISABLED;

    public TileComponentUpgrade<TileEntityMachine> upgradeComponent;
    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

    public TileEntityMachine(IBlockProvider blockProvider, int upgradeSlot) {
        super(blockProvider);
        upgradeComponent = new TileComponentUpgrade<>(this, upgradeSlot);
        upgradeComponent.setSupported(Upgrade.MUFFLING);
    }

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
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
            setEnergyPerTick(dataStream.readDouble());
            setMaxEnergy(dataStream.readDouble());
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(controlType.ordinal());
        data.add(getEnergyPerTick());
        data.add(getMaxEnergy());
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
    public void setControlType(@Nonnull RedstoneControl type) {
        controlType = Objects.requireNonNull(type);
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
    public void recalculateUpgrades(Upgrade upgrade) {
        //TODO: Should this go away
        if (upgrade == Upgrade.ENERGY) {
            setMaxEnergy(MekanismUtils.getMaxEnergy(this, getBaseStorage()));
            setEnergyPerTick(MekanismUtils.getBaseEnergyPerTick(this, getBaseUsage()));
            setEnergy(Math.min(getMaxEnergy(), getEnergy()));
        }
    }
}