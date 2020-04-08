package mekanism.common.capabilities.holder.heat;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.heat.IHeatCapacitor;
import net.minecraft.util.Direction;

public interface IHeatCapacitorHolder {

    @Nonnull
    List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side);
}
