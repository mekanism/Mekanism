package mekanism.common.capabilities.holder.heat;

import java.util.List;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IHeatCapacitorHolder extends IHolder {

    @NotNull
    List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side);
}
