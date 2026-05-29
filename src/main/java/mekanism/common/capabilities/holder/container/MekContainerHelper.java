package mekanism.common.capabilities.holder.container;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.HeatSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekContainerHelper<CONTAINER> {

    public static final Function<ISlotInfo, List<IInventorySlot>> ITEM_SLOT_PARSER = slotInfo -> slotInfo instanceof InventorySlotInfo info ? info.getSlots() : Collections.emptyList();
    public static final Function<ISlotInfo, List<IFluidTank>> FLUID_SLOT_PARSER = slotInfo -> slotInfo instanceof FluidSlotInfo info ? info.getTanks() : Collections.emptyList();
    public static final Function<ISlotInfo, List<IChemicalTank>> CHEMICAL_SLOT_PARSER = slotInfo -> slotInfo instanceof ChemicalSlotInfo info ? info.getTanks() : Collections.emptyList();
    public static final Function<ISlotInfo, List<IHeatCapacitor>> HEAT_SLOT_PARSER = slotInfo -> slotInfo instanceof HeatSlotInfo info ? info.getHeatCapacitors() : Collections.emptyList();

    public static BiPredicate<ChemicalResource, @NotNull AutomationType> radioactiveInputTankPredicate(Supplier<IChemicalTank> outputTank) {
        //Allow extracting out of the input gas tank if it isn't external OR the output tank is empty AND the input is radioactive
        //Note: This only is the case if radiation is enabled as otherwise things like gauge droppers can work as the way to remove radioactive contents
        return (type, automationType) -> !automationType.isExternal() ||
                                         (outputTank.get().isEmpty() && type.isRadioactive() && RadiationManager.isGlobalRadiationEnabled());
    }

    private final IContainerHolder<CONTAINER> containerHolder;
    private boolean built;

    private MekContainerHelper(IContainerHolder<CONTAINER> containerHolder) {
        this.containerHolder = containerHolder;
    }

    public static <CONTAINER> MekContainerHelper<CONTAINER> readOnly() {
        return new MekContainerHelper<>(new ReadOnlyHolder<>());
    }

    public static <CONTAINER> MekContainerHelper<CONTAINER> forSide(Supplier<Direction> facingSupplier) {
        return forSide(facingSupplier, null, null);
    }

    public static <CONTAINER> MekContainerHelper<CONTAINER> forSide(Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate,
          @Nullable Predicate<RelativeSide> extractPredicate) {
        return new MekContainerHelper<>(new BasicContainerHolder<>(facingSupplier, insertPredicate, extractPredicate));
    }

    public static <CONTAINER> MekContainerHelper<CONTAINER> forSideWithOverrides(Supplier<Direction> facingSupplier) {
        return forSideWithOverrides(facingSupplier, null, null);
    }

    public static <CONTAINER> MekContainerHelper<CONTAINER> forSideWithOverrides(Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate,
          @Nullable Predicate<RelativeSide> extractPredicate) {
        return new MekContainerHelper<>(new OverridingHolder<>(facingSupplier, insertPredicate, extractPredicate));
    }

    public static MekContainerHelper<IInventorySlot> forSideWithItemConfig(ISideConfiguration sideConfiguration) {
        return forSideWithConfig(sideConfiguration, TransmissionType.ITEM, ITEM_SLOT_PARSER);
    }

    public static MekContainerHelper<IFluidTank> forSideWithFluidConfig(ISideConfiguration sideConfiguration) {
        return forSideWithConfig(sideConfiguration, TransmissionType.FLUID, FLUID_SLOT_PARSER);
    }

    public static MekContainerHelper<IChemicalTank> forSideWithChemicalConfig(ISideConfiguration sideConfiguration) {
        return forSideWithConfig(sideConfiguration, TransmissionType.CHEMICAL, CHEMICAL_SLOT_PARSER);
    }

    public static MekContainerHelper<IHeatCapacitor> forSideWithHeatConfig(ISideConfiguration sideConfiguration) {
        return forSideWithConfig(sideConfiguration, TransmissionType.HEAT, HEAT_SLOT_PARSER);
    }

    public static <CONTAINER> MekContainerHelper<CONTAINER> forSideWithConfig(ISideConfiguration sideConfiguration, TransmissionType transmissionType,
          Function<ISlotInfo, List<CONTAINER>> slotInfoParser) {
        return new MekContainerHelper<>(new ContainerConfigHolder<>(sideConfiguration, transmissionType, slotInfoParser));
    }

    public <CONT extends CONTAINER> CONT addContainer(@NotNull CONT container) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        switch (containerHolder) {
            case BasicContainerHolder<CONTAINER> holder -> holder.addContainer(container);
            case ReadOnlyHolder<CONTAINER> holder -> holder.addContainer(container);
            case ContainerConfigHolder<CONTAINER> holder -> holder.addContainer(container);
            default -> throw new IllegalArgumentException("Holder does not know how to add slots");
        }
        return container;
    }

    public <CONT extends CONTAINER> CONT addContainer(@NotNull CONT container, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (containerHolder instanceof BasicContainerHolder<CONTAINER> holder) {
            holder.addContainer(container, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add containers on specific sides");
        }
        return container;
    }

    public <CONT extends CONTAINER> CONT addContainer(@NotNull CONT container, BiFunction<CONTAINER, RelativeSide, @Nullable CONTAINER> containerTransformer) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (containerHolder instanceof OverridingHolder<CONTAINER> holder) {
            holder.addContainer(container, containerTransformer);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add container overrides on specific sides");
        }
        return container;
    }

    public IContainerHolder<CONTAINER> build() {
        built = true;
        return containerHolder;
    }
}