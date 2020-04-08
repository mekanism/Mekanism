package mekanism.common.tile.base;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

public enum SubstanceType {
    ENERGY(NBTConstants.ENERGY_CONTAINERS, (tile) -> tile.getEnergyContainers(null), DataHandlerUtils::writeContainers, DataHandlerUtils::readContainers),
    FLUID(NBTConstants.FLUID_TANKS, (tile) -> tile.getFluidTanks(null), DataHandlerUtils::writeTanks, DataHandlerUtils::readTanks),
    GAS(NBTConstants.GAS_TANKS, (tile) -> tile.getGasTanks(null), DataHandlerUtils::writeTanks, DataHandlerUtils::readTanks),
    INFUSION(NBTConstants.INFUSION_TANKS, (tile) -> tile.getInfusionTanks(null), DataHandlerUtils::writeTanks, DataHandlerUtils::readTanks),
    HEAT(NBTConstants.HEAT_CAPACITORS, (tile) -> tile.getHeatCapacitors(null), DataHandlerUtils::writeContainers, DataHandlerUtils::readContainers);

    private String containerTag;
    private Function<TileEntityMekanism, List<? extends INBTSerializable<CompoundNBT>>> containerSupplier;
    private Function<List<? extends INBTSerializable<CompoundNBT>>, ListNBT> writeFunction;
    private BiConsumer<List<? extends INBTSerializable<CompoundNBT>>, ListNBT> readFunction;

    private SubstanceType(String containerTag,
          Function<TileEntityMekanism, List<? extends INBTSerializable<CompoundNBT>>> containerSupplier,
          Function<List<? extends INBTSerializable<CompoundNBT>>, ListNBT> writeFunction,
          BiConsumer<List<? extends INBTSerializable<CompoundNBT>>, ListNBT> readFunction) {
        this.containerTag = containerTag;
        this.containerSupplier = containerSupplier;
        this.writeFunction = writeFunction;
        this.readFunction = readFunction;
    }

    public void write(TileEntityMekanism tile, CompoundNBT tag) {
        tag.put(containerTag, writeFunction.apply(containerSupplier.apply(tile)));
    }

    public void read(TileEntityMekanism tile, CompoundNBT tag) {
        readFunction.accept(containerSupplier.apply(tile), tag.getList(containerTag, NBT.TAG_COMPOUND));
    }

    public String getContainerTag() {
        return containerTag;
    }

    public Function<List<? extends INBTSerializable<CompoundNBT>>, ListNBT> getWriteFunction() {
        return writeFunction;
    }

    public BiConsumer<List<? extends INBTSerializable<CompoundNBT>>, ListNBT> getReadFunction() {
        return readFunction;
    }

    public List<? extends INBTSerializable<CompoundNBT>> getContainers(TileEntityMekanism tile) {
        return containerSupplier.apply(tile);
    }

    public boolean canHandle(TileEntityMekanism tile) {
        switch (this) {
            case ENERGY: return tile.canHandleEnergy();
            case FLUID: return tile.canHandleFluid();
            case GAS: return tile.canHandleGas();
            case INFUSION: return tile.canHandleInfusion();
            case HEAT: return tile.canHandleHeat();
        }
        return false;
    }
}
