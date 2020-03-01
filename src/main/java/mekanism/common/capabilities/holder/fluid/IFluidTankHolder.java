package mekanism.common.capabilities.holder.fluid;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;

public interface IFluidTankHolder extends IHolder {

    @Nonnull
    List<IExtendedFluidTank> getTanks(@Nullable Direction side);
}