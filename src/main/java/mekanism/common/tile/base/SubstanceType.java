package mekanism.common.tile.base;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.DataHandlerUtils;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public enum SubstanceType {
    ENERGY(() -> ContainerType.ENERGY, tile -> tile.getEnergyContainers(null)),
    FLUID(() -> ContainerType.FLUID, tile -> tile.getFluidTanks(null)),
    GAS(() -> ContainerType.GAS, tile -> tile.getGasTanks(null)),
    INFUSION(() -> ContainerType.INFUSION, tile -> tile.getInfusionTanks(null)),
    PIGMENT(() -> ContainerType.PIGMENT, tile -> tile.getPigmentTanks(null)),
    SLURRY(() -> ContainerType.SLURRY, tile -> tile.getSlurryTanks(null)),
    HEAT(() -> ContainerType.HEAT, tile -> tile.getHeatCapacitors(null));

    private final Supplier<? extends ContainerType<?, ?, ?>> containerType;
    private final Function<TileEntityMekanism, ? extends List<? extends INBTSerializable<CompoundTag>>> containerSupplier;

    //Note: The container type must be a supplier or datagen freezes early with no output related to why it has halted
    <TANK extends INBTSerializable<CompoundTag>> SubstanceType(Supplier<ContainerType<TANK, ?, ?>> containerType, Function<TileEntityMekanism, List<TANK>> containerSupplier) {
        this.containerType = containerType;
        this.containerSupplier = containerSupplier;
    }

    public void write(TileEntityMekanism tile, CompoundTag tag) {
        tag.put(getContainerTag(), DataHandlerUtils.writeContainers(containerSupplier.apply(tile)));
    }

    public void read(TileEntityMekanism tile, CompoundTag tag) {
        DataHandlerUtils.readContainers(containerSupplier.apply(tile), tag.getList(getContainerTag(), Tag.TAG_COMPOUND));
    }

    private String getContainerTag() {
        return getContainerType().getTag();
    }

    public List<? extends INBTSerializable<CompoundTag>> getContainers(TileEntityMekanism tile) {
        return containerSupplier.apply(tile);
    }

    public ContainerType<?, ?, ?> getContainerType() {
        return containerType.get();
    }

    public boolean canHandle(TileEntityMekanism tile) {
        return switch (this) {
            case ENERGY -> tile.canHandleEnergy();
            case FLUID -> tile.canHandleFluid();
            case GAS -> tile.canHandleGas();
            case INFUSION -> tile.canHandleInfusion();
            case PIGMENT -> tile.canHandlePigment();
            case SLURRY -> tile.canHandleSlurry();
            case HEAT -> tile.canHandleHeat();
        };
    }
}
