package mekanism.common.capabilities.holder.chemical;

import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IChemicalTankHolder extends IHolder {

    @NotNull
    List<IChemicalTank> getTanks(@Nullable Direction side);
}