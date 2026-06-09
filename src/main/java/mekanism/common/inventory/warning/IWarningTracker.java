package mekanism.common.inventory.warning;

import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.network.chat.Component;

public interface IWarningTracker {

    BooleanSupplier trackWarning(WarningType type, BooleanSupplier warningSupplier);

    boolean hasWarning();

    List<Component> getWarnings();

    void clearTrackedWarnings();
}