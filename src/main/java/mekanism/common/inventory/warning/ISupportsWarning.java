package mekanism.common.inventory.warning;

import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.warning.WarningTracker.WarningType;

public interface ISupportsWarning<TYPE extends ISupportsWarning<TYPE>> {

    TYPE warning(@Nonnull WarningType type, @Nonnull BooleanSupplier warningSupplier);

    static BooleanSupplier compound(@Nullable BooleanSupplier existing, BooleanSupplier newSupplier) {
        return existing == null ? newSupplier : (() -> existing.getAsBoolean() || newSupplier.getAsBoolean());
    }
}