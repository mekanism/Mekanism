package mekanism.common.attachments.containers.heat;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.heat.HeatAPI;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;

public class HeatCapacitorsBuilder {

    public static HeatCapacitorsBuilder builder() {
        return new HeatCapacitorsBuilder();
    }

    private final List<IBasicContainerCreator<? extends ComponentBackedHeatCapacitor>> capacitorCreators = new ArrayList<>();
    private final DoubleList defaultHeatCapacities = new DoubleArrayList();

    private HeatCapacitorsBuilder() {
    }

    public BaseContainerCreator<AttachedHeat, ComponentBackedHeatCapacitor> build() {
        return new BaseHeatCapacitorCreator(capacitorCreators, defaultHeatCapacities);
    }

    public HeatCapacitorsBuilder addBasic(double heatCapacity, double inverseConductionCoefficient, double inverseInsulationCoefficient) {
        return addCapacitor(heatCapacity, (type, attachedTo, containerIndex) -> new ComponentBackedHeatCapacitor(attachedTo, containerIndex, inverseConductionCoefficient,
              inverseInsulationCoefficient, heatCapacity));
    }

    public HeatCapacitorsBuilder addCapacitor(IBasicContainerCreator<? extends ComponentBackedHeatCapacitor> capacitor) {
        return addCapacitor(HeatAPI.DEFAULT_HEAT_CAPACITY, capacitor);
    }

    public HeatCapacitorsBuilder addCapacitor(double defaultHeatCapacity, IBasicContainerCreator<? extends ComponentBackedHeatCapacitor> capacitor) {
        defaultHeatCapacities.add(defaultHeatCapacity);
        capacitorCreators.add(capacitor);
        return this;
    }

    private static class BaseHeatCapacitorCreator extends BaseContainerCreator<AttachedHeat, ComponentBackedHeatCapacitor> {

        private final DoubleList defaultHeatCapacities;

        public BaseHeatCapacitorCreator(List<IBasicContainerCreator<? extends ComponentBackedHeatCapacitor>> creators, DoubleList defaultHeatCapacities) {
            super(creators);
            this.defaultHeatCapacities = defaultHeatCapacities;
        }

        @Override
        public AttachedHeat initStorage(int containers) {
            List<HeatCapacitorData> capacitors = new ArrayList<>(containers);
            for (int capacitor = 0; capacitor < containers; capacitor++) {
                capacitors.add(new HeatCapacitorData(defaultHeatCapacities.getDouble(capacitor)));
            }
            return new AttachedHeat(capacitors);
        }
    }
}