package mekanism.common.component.containers.heat;

import java.util.function.Function;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.component.containers.creator.IContainerCreator;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class HeatCapacitorBuilder {

    private HeatCapacitorBuilder() {
    }

    public static IContainerCreator<IHeatCapacitor, HeatCapacitorData> basicCreator(double heatCapacity, double inverseConductionCoefficient, double inverseInsulationCoefficient) {
        return creator(heatCapacity, attachedAccess -> new ComponentBackedHeatCapacitor(attachedAccess, inverseConductionCoefficient, inverseInsulationCoefficient));
    }

    public static IContainerCreator<IHeatCapacitor, HeatCapacitorData> creator(double defaultHeatCapacity, Function<ItemAccess, IHeatCapacitor> creator) {
        return new BaseHeatCapacitorCreator(creator, defaultHeatCapacity);
    }

    private record BaseHeatCapacitorCreator(Function<ItemAccess, IHeatCapacitor> creator, HeatCapacitorData initStorage)
          implements IContainerCreator<IHeatCapacitor, HeatCapacitorData> {

        public BaseHeatCapacitorCreator(Function<ItemAccess, IHeatCapacitor> creator, double defaultHeatCapacity) {
            this(creator, new HeatCapacitorData(defaultHeatCapacity));
        }

        @Override
        public int totalContainers() {
            return 1;
        }

        @Override
        public IHeatCapacitor create(ItemAccess attachedAccess, int containerIndex) {
            return creator.apply(attachedAccess);
        }
    }
}