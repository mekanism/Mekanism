package mekanism.generators.common.tile.turbine;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.content.turbine.TurbineCache;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityTurbineCasing extends TileEntityMultiblock<SynchronizedTurbineData> implements IStrictEnergyStorage {

    public TileEntityTurbineCasing() {
        this(GeneratorsBlocks.TURBINE_CASING);
    }

    public TileEntityTurbineCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!isRemote() && structure != null && isRendering) {
            structure.lastSteamInput = structure.newSteamInput;
            structure.newSteamInput = 0;

            int stored = structure.fluidTank.getFluidAmount();
            double proportion = (double) stored / (double) structure.getFluidCapacity();
            double flowRate = 0;

            if (stored > 0 && getEnergy() < structure.getEnergyCapacity()) {
                double energyMultiplier = (MekanismConfig.general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                          Math.min(structure.blades, structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
                double rate = structure.lowerVolume * (structure.getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
                rate = Math.min(rate, structure.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());

                double origRate = rate;
                rate = Math.min(Math.min(stored, rate), (getMaxEnergy() - getEnergy()) / energyMultiplier) * proportion;

                flowRate = rate / origRate;
                setEnergy(getEnergy() + (int) rate * energyMultiplier);

                FluidStack fluid = structure.fluidTank.getFluid();
                structure.fluidTank.setStack(new FluidStack(fluid, (int) (fluid.getAmount() - rate)));
                structure.clientFlow = (int) rate;
                structure.flowRemaining = Math.min((int) rate, structure.condensers * MekanismGeneratorsConfig.generators.condenserRate.get());
            } else {
                structure.clientFlow = 0;
            }

            if (structure.dumpMode == GasMode.DUMPING && !structure.fluidTank.isEmpty()) {
                FluidStack fluid = structure.fluidTank.getFluid();
                int amount = fluid.getAmount();
                structure.fluidTank.setStack(new FluidStack(fluid, amount - Math.min(amount, Math.max(amount / 50, structure.lastSteamInput * 2))));
            }

            float newRotation = (float) flowRate;
            boolean needsRotationUpdate = false;

            if (Math.abs(newRotation - structure.clientRotation) > SynchronizedTurbineData.ROTATION_THRESHOLD) {
                structure.clientRotation = newRotation;
                needsRotationUpdate = true;
            }

            if (structure.needsRenderUpdate() || needsRotationUpdate) {
                sendPacketToRenderer();
            }
            structure.prevFluid = structure.fluidTank.isEmpty() ? FluidStack.EMPTY : structure.fluidTank.getFluid().copy();
        }
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (structure == null) {
            return ActionResultType.PASS;
        }
        return openGui(player);
    }

    @Override
    public double getEnergy() {
        return structure != null ? structure.electricityStored : 0;
    }

    @Override
    public void setEnergy(double energy) {
        if (structure != null) {
            structure.electricityStored = Math.max(Math.min(energy, getMaxEnergy()), 0);
            MekanismUtils.saveChunk(this);
        }
    }

    @Override
    public double getMaxEnergy() {
        return structure != null ? structure.getEnergyCapacity() : 0;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        if (structure != null) {
            data.add(structure.volume);
            data.add(structure.lowerVolume);
            data.add(structure.vents);
            data.add(structure.blades);
            data.add(structure.coils);
            data.add(structure.condensers);
            data.add(structure.getDispersers());
            data.add(structure.electricityStored);
            data.add(structure.clientFlow);
            data.add(structure.lastSteamInput);
            data.add(structure.dumpMode);
            data.add(structure.fluidTank.getFluid());
            if (isRendering) {
                structure.complex.write(data);
                data.add(structure.clientRotation);
            }
        }
        return data;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            if (structure != null) {
                byte type = dataStream.readByte();
                if (type == 0) {
                    structure.dumpMode = structure.dumpMode.getNext();
                }
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (isRemote()) {
            if (clientHasStructure) {
                structure.volume = dataStream.readInt();
                structure.lowerVolume = dataStream.readInt();
                structure.vents = dataStream.readInt();
                structure.blades = dataStream.readInt();
                structure.coils = dataStream.readInt();
                structure.condensers = dataStream.readInt();
                structure.clientDispersers = dataStream.readInt();
                structure.electricityStored = dataStream.readDouble();
                structure.clientFlow = dataStream.readInt();
                structure.lastSteamInput = dataStream.readInt();
                structure.dumpMode = dataStream.readEnumValue(GasMode.class);

                structure.fluidTank.setStack(dataStream.readFluidStack());

                if (isRendering) {
                    structure.complex = Coord4D.read(dataStream);
                    structure.clientRotation = dataStream.readFloat();
                    SynchronizedTurbineData.clientRotationMap.put(structure.inventoryID, structure.clientRotation);
                }
            }
        }
    }

    @Nonnull
    @Override
    protected SynchronizedTurbineData getNewStructure() {
        return new SynchronizedTurbineData(this);
    }

    @Override
    public MultiblockCache<SynchronizedTurbineData> getNewCache() {
        return new TurbineCache();
    }

    @Override
    protected UpdateProtocol<SynchronizedTurbineData> getProtocol() {
        return new TurbineUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SynchronizedTurbineData> getManager() {
        return MekanismGenerators.turbineManager;
    }
}