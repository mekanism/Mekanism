package mekanism.common.content.filter;

import java.util.function.Supplier;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.Nullable;

public interface IFilter<FILTER extends IFilter<FILTER>> {

    FILTER clone();

    @ComputerMethod(threadSafe = true)
    FilterType getFilterType();

    boolean hasFilter();

    @ComputerMethod(threadSafe = true)
    boolean isEnabled();

    @ComputerMethod(threadSafe = true)
    void setEnabled(boolean enabled);

    void setRegistryAccess(Supplier<HolderLookup.@Nullable Provider> registryAccess);
}