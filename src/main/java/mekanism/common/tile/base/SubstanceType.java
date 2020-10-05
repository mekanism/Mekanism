package mekanism.common.tile.base;

import java.util.List;
import java.util.function.Function;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

public enum SubstanceType {
    ENERGY(NBTConstants.ENERGY_CONTAINERS, tile -> tile.getEnergyContainers(null)),
    FLUID(NBTConstants.FLUID_TANKS, tile -> tile.getFluidTanks(null)),
    GAS(NBTConstants.GAS_TANKS, tile -> tile.getGasTanks(null)),
    INFUSION(NBTConstants.INFUSION_TANKS, tile -> tile.getInfusionTanks(null)),
    PIGMENT(NBTConstants.PIGMENT_TANKS, tile -> tile.getPigmentTanks(null)),
    SLURRY(NBTConstants.SLURRY_TANKS, tile -> tile.getSlurryTanks(null)),
    HEAT(NBTConstants.HEAT_CAPACITORS, tile -> tile.getHeatCapacitors(null));

    private final String containerTag;
    private final Function<TileEntityMekanism, List<? extends INBTSerializable<CompoundNBT>>> containerSupplier;

    SubstanceType(String containerTag, Function<TileEntityMekanism, List<? extends INBTSerializable<CompoundNBT>>> containerSupplier) {
        this.containerTag = containerTag;
        this.containerSupplier = containerSupplier;
    }

    public void write(TileEntityMekanism tile, CompoundNBT tag) {
        tag.put(containerTag, DataHandlerUtils.writeContainers(containerSupplier.apply(tile)));
    }

    public void read(TileEntityMekanism tile, CompoundNBT tag) {
        DataHandlerUtils.readContainers(containerSupplier.apply(tile), tag.getList(containerTag, NBT.TAG_COMPOUND));
    }

    public String getContainerTag() {
        return containerTag;
    }

    public List<? extends INBTSerializable<CompoundNBT>> getContainers(TileEntityMekanism tile) {
        return containerSupplier.apply(tile);
    }

    public boolean canHandle(TileEntityMekanism tile) {
        switch (this) {
            case ENERGY:
                return tile.canHandleEnergy();
            case FLUID:
                return tile.canHandleFluid();
            case GAS:
                return tile.canHandleGas();
            case INFUSION:
                return tile.canHandleInfusion();
            case PIGMENT:
                return tile.canHandlePigment();
            case SLURRY:
                return tile.canHandleSlurry();
            case HEAT:
                return tile.canHandleHeat();
        }
        return false;
    }
}
