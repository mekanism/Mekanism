package mekanism.client.gui.warning;

import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.warning.WarningTracker.WarningType;
import net.minecraft.util.text.ITextComponent;

public interface IWarningTracker {

    BooleanSupplier trackWarning(@Nonnull WarningType type, @Nonnull BooleanSupplier warningSupplier);

    boolean hasWarning();

    List<ITextComponent> getWarnings();

    void clearTrackedWarnings();
}