package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.LaserManager;
import mekanism.common.LaserManager.LaserInfo;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityLaserAmplifier extends TileEntityMekanism implements ILaserReceptor, IStrictEnergyOutputter, IStrictEnergyStorage, IComputerIntegration {

    public static final double MAX_ENERGY = 5E9;
    private static final String[] methods = new String[]{"getEnergy", "getMaxEnergy"};
    public double collectedEnergy = 0;
    public double lastFired = 0;
    public double minThreshold = 0;
    public double maxThreshold = 5E9;
    public int ticks = 0;
    public int time = 0;
    public boolean on = false;
    public Coord4D digging;
    public double diggingProgress;
    public boolean emittingRedstone;
    public int currentRedstoneLevel;
    public RedstoneOutput outputMode = RedstoneOutput.OFF;

    public TileEntityLaserAmplifier() {
        super(MekanismBlock.LASER_AMPLIFIER);
    }

    @Override
    public void receiveLaserEnergy(double energy, Direction side) {
        setEnergy(getEnergy() + energy);
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) {
            if (on) {
                BlockRayTraceResult mop = LaserManager.fireLaserClient(this, getDirection(), lastFired, world);
                Coord4D hitCoord = mop == null ? null : new Coord4D(mop, world);
                if (hitCoord == null || !hitCoord.equals(digging)) {
                    digging = hitCoord;
                    diggingProgress = 0;
                }

                if (hitCoord != null) {
                    BlockState blockHit = hitCoord.getBlockState(world);
                    TileEntity tileHit = hitCoord.getTileEntity(world);
                    float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());

                    if (hardness >= 0 && !CapabilityUtils.getCapabilityHelper(tileHit, Capabilities.LASER_RECEPTOR_CAPABILITY, mop.getFace()).matches(ILaserReceptor::canLasersDig)) {
                        diggingProgress += lastFired;
                        if (diggingProgress < hardness * MekanismConfig.general.laserEnergyNeededPerHardness.get()) {
                            Mekanism.proxy.addHitEffects(hitCoord, mop);
                        }
                    }
                }

            }
        } else {
            boolean prevRedstone = emittingRedstone;
            emittingRedstone = false;
            if (ticks < time) {
                ticks++;
            } else {
                ticks = 0;
            }

            if (toFire() > 0) {
                double firing = toFire();
                if (!on || firing != lastFired) {
                    on = true;
                    lastFired = firing;
                    Mekanism.packetHandler.sendUpdatePacket(this);
                }

                LaserInfo info = LaserManager.fireLaser(this, getDirection(), firing, world);
                Coord4D hitCoord = info.movingPos == null ? null : new Coord4D(info.movingPos, world);

                if (hitCoord == null || !hitCoord.equals(digging)) {
                    digging = hitCoord;
                    diggingProgress = 0;
                }

                if (hitCoord != null) {
                    BlockState blockHit = hitCoord.getBlockState(world);
                    TileEntity tileHit = hitCoord.getTileEntity(world);
                    float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());
                    if (hardness >= 0 && !CapabilityUtils.getCapabilityHelper(tileHit, Capabilities.LASER_RECEPTOR_CAPABILITY, info.movingPos.getFace()).matches(ILaserReceptor::canLasersDig)) {
                        diggingProgress += firing;
                        if (diggingProgress >= hardness * MekanismConfig.general.laserEnergyNeededPerHardness.get()) {
                            LaserManager.breakBlock(hitCoord, true, world, pos);
                            diggingProgress = 0;
                        }
                    }
                }
                emittingRedstone = info.foundEntity;
                setEnergy(getEnergy() - firing);
            } else if (on) {
                on = false;
                diggingProgress = 0;
                Mekanism.packetHandler.sendUpdatePacket(this);
            }

            if (outputMode != RedstoneOutput.ENTITY_DETECTION) {
                emittingRedstone = false;
            }
            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                markDirty();
                currentRedstoneLevel = newRedstoneLevel;
            }
            if (emittingRedstone != prevRedstone) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType());
            }
        }
    }

    @Override
    public double pullEnergy(Direction side, double amount, boolean simulate) {
        double toGive = Math.min(getEnergy(), amount);
        if (toGive < 0.0001) {
            return 0;
        }
        if (!simulate) {
            setEnergy(getEnergy() - toGive);
        }
        return toGive;
    }

    @Override
    public double getEnergy() {
        return collectedEnergy;
    }

    @Override
    public void setEnergy(double energy) {
        collectedEnergy = Math.max(0, Math.min(energy, MAX_ENERGY));
    }

    public boolean shouldFire() {
        return collectedEnergy >= minThreshold && ticks >= time && MekanismUtils.canFunction(this);
    }

    public double toFire() {
        return shouldFire() ? Math.min(collectedEnergy, maxThreshold) : 0;
    }

    public int getRedstoneLevel() {
        if (outputMode != RedstoneOutput.ENERGY_CONTENTS) {
            return 0;
        }
        return MekanismUtils.redstoneLevelFromContents(getEnergy(), getMaxEnergy());
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(on);
        data.add(minThreshold);
        data.add(maxThreshold);
        data.add(time);
        data.add(collectedEnergy);
        data.add(lastFired);
        data.add(emittingRedstone);
        data.add(outputMode.ordinal());
        return data;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!world.isRemote) {
            switch (dataStream.readInt()) {
                case 0:
                    minThreshold = Math.min(MAX_ENERGY, MekanismUtils.convertToJoules(dataStream.readDouble()));
                    break;
                case 1:
                    maxThreshold = Math.min(MAX_ENERGY, MekanismUtils.convertToJoules(dataStream.readDouble()));
                    break;
                case 2:
                    time = dataStream.readInt();
                    break;
                case 3:
                    outputMode = RedstoneOutput.values()[outputMode.ordinal() == RedstoneOutput.values().length - 1 ? 0 : outputMode.ordinal() + 1];
                    break;
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (world.isRemote) {
            on = dataStream.readBoolean();
            minThreshold = dataStream.readDouble();
            maxThreshold = dataStream.readDouble();
            time = dataStream.readInt();
            collectedEnergy = dataStream.readDouble();
            lastFired = dataStream.readDouble();
            emittingRedstone = dataStream.readBoolean();
            outputMode = RedstoneOutput.values()[dataStream.readInt()];
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        on = nbtTags.getBoolean("on");
        minThreshold = nbtTags.getDouble("minThreshold");
        maxThreshold = nbtTags.getDouble("maxThreshold");
        time = nbtTags.getInt("time");
        collectedEnergy = nbtTags.getDouble("collectedEnergy");
        lastFired = nbtTags.getDouble("lastFired");
        outputMode = RedstoneOutput.values()[nbtTags.getInt("outputMode")];
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean("on", on);
        nbtTags.putDouble("minThreshold", minThreshold);
        nbtTags.putDouble("maxThreshold", maxThreshold);
        nbtTags.putInt("time", time);
        nbtTags.putDouble("collectedEnergy", collectedEnergy);
        nbtTags.putDouble("lastFired", lastFired);
        nbtTags.putInt("outputMode", outputMode.ordinal());
        return nbtTags;
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return true;
    }

    @Override
    public double getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{getMaxEnergy()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
            return Capabilities.ENERGY_STORAGE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY) {
            return Capabilities.ENERGY_OUTPUTTER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.LASER_RECEPTOR_CAPABILITY) {
            return Capabilities.LASER_RECEPTOR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    public enum RedstoneOutput implements IHasTranslationKey {
        OFF("off"),
        ENTITY_DETECTION("entityDetection"),
        ENERGY_CONTENTS("energyContents");

        private String unlocalizedName;

        RedstoneOutput(String name) {
            unlocalizedName = name;
        }

        @Override
        public String getTranslationKey() {
            return "mekanism.gui." + unlocalizedName;
        }
    }
}