package mekanism.common.capabilities.holder.chemical;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.util.Direction;

public class ChemicalTankHolder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> implements
      IChemicalTankHolder<CHEMICAL, STACK, TANK> {

    private final Map<RelativeSide, List<TANK>> directionalTanks = new EnumMap<>(RelativeSide.class);
    private final List<TANK> tanks = new ArrayList<>();
    private final Supplier<Direction> facingSupplier;
    //TODO: Allow declaring that some sides will be the same, so can just be the same list in memory??

    ChemicalTankHolder(Supplier<Direction> facingSupplier) {
        this.facingSupplier = facingSupplier;
    }

    void addTank(@Nonnull TANK tank, RelativeSide... sides) {
        tanks.add(tank);
        for (RelativeSide side : sides) {
            directionalTanks.computeIfAbsent(side, k -> new ArrayList<>()).add(tank);
        }
    }

    @Nonnull
    @Override
    public List<TANK> getTanks(@Nullable Direction direction) {
        if (direction == null || directionalTanks.isEmpty()) {
            //If we want the internal OR we have no side specification, give all of our tanks
            return tanks;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        List<TANK> tanks = directionalTanks.get(side);
        if (tanks == null) {
            return Collections.emptyList();
        }
        return tanks;
    }
}