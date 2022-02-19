package mekanism.common.capabilities.holder.chemical;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.util.Direction;

public class ChemicalTankHolder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      extends BasicHolder<TANK> implements IChemicalTankHolder<CHEMICAL, STACK, TANK> {

    ChemicalTankHolder(Supplier<Direction> facingSupplier) {
        super(facingSupplier);
    }

    void addTank(@Nonnull TANK tank, RelativeSide... sides) {
        addSlotInternal(tank, sides);
    }

    @Nonnull
    @Override
    public List<TANK> getTanks(@Nullable Direction direction) {
        return getSlots(direction);
    }
}