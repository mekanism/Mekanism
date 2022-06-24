package mekanism.common.capabilities.holder.fluid;

import java.util.List;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFluidTankHolder extends IHolder {

    @NotNull
    List<IExtendedFluidTank> getTanks(@Nullable Direction side);
}