package mekanism.common.capabilities.holder.chemical;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;

public interface IChemicalTankHolder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends IHolder {

    @Nonnull
    List<? extends IChemicalTank<CHEMICAL, STACK>> getTanks(@Nullable Direction side);
}