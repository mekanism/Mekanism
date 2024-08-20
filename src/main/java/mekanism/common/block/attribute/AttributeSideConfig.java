package mekanism.common.block.attribute;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import mekanism.common.lib.transmitter.TransmissionType;
import org.jetbrains.annotations.NotNull;

public record AttributeSideConfig(@NotNull Set<TransmissionType> supportedTypes) implements Attribute {

    public static final AttributeSideConfig ELECTRIC_MACHINE = create(TransmissionType.ITEM, TransmissionType.ENERGY);
    public static final AttributeSideConfig ADVANCED_ELECTRIC_MACHINE = create(TransmissionType.ITEM, TransmissionType.CHEMICAL, TransmissionType.ENERGY);

    public static AttributeSideConfig create(TransmissionType... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("Expected at least one supported transmission type");
        }
        //Note: We care about the order of the types for displaying them in the GUI
        Set<TransmissionType> supported = new LinkedHashSet<>(types.length);
        Collections.addAll(supported, types);
        return new AttributeSideConfig(supported);
    }
}
