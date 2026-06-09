package mekanism.common.inventory.warning;

import java.util.function.BooleanSupplier;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import org.jspecify.annotations.Nullable;

public interface ISupportsWarning<TYPE extends ISupportsWarning<TYPE>> {

    TYPE warning(WarningType type, BooleanSupplier warningSupplier);

    static BooleanSupplier compound(@Nullable BooleanSupplier existing, BooleanSupplier newSupplier) {
        return existing == null ? newSupplier : () -> existing.getAsBoolean() || newSupplier.getAsBoolean();
    }
}