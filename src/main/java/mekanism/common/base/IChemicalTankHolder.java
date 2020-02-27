package mekanism.common.base;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.util.Direction;

@FunctionalInterface
public interface IChemicalTankHolder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

    @Nonnull
    List<? extends IChemicalTank<CHEMICAL, STACK>> getTanks(@Nullable Direction side);
}