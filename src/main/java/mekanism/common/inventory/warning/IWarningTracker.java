package mekanism.common.inventory.warning;

import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface IWarningTracker {

    BooleanSupplier trackWarning(@NotNull WarningType type, @NotNull BooleanSupplier warningSupplier);

    boolean hasWarning();

    List<Component> getWarnings();

    void clearTrackedWarnings();
}