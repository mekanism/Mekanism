package mekanism.common.component.containers.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

public final class ContainerType {

    private ContainerType() {
    }

    static final List<IContainerType<?, ?>> TYPES_INTERNAL = new ArrayList<>();
    public static final List<IContainerType<?, ?>> TYPES = Collections.unmodifiableList(TYPES_INTERNAL);

    public static final EnergyContainerType ENERGY = new EnergyContainerType();
    public static final ItemContainerType ITEM = new ItemContainerType();
    public static final FluidContainerType FLUID = new FluidContainerType();
    public static final ChemicalContainerType CHEMICAL = new ChemicalContainerType();
    public static final HeatContainerType HEAT = new HeatContainerType();

    public static boolean anySupports(Holder<Item> item) {
        for (IContainerType<?, ?> type : TYPES) {
            if (type.supports(item)) {
                return true;
            }
        }
        return false;
    }
}