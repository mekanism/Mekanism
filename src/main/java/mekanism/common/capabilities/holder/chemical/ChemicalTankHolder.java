package mekanism.common.capabilities.holder.chemical;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChemicalTankHolder extends BasicHolder<IChemicalTank> implements IChemicalTankHolder {

    ChemicalTankHolder(Supplier<Direction> facingSupplier) {
        super(facingSupplier);
    }

    void addTank(IChemicalTank tank, RelativeSide... sides) {
        addSlotInternal(tank, sides);
    }

    @NotNull
    @Override
    public List<IChemicalTank> getTanks(@Nullable Direction direction) {
        return getSlots(direction);
    }
}