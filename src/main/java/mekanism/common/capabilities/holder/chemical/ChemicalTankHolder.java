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

public class ChemicalTankHolder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IChemicalTankHolder<CHEMICAL, STACK> {

    private final Map<RelativeSide, List<IChemicalTank<CHEMICAL, STACK>>> directionalTanks = new EnumMap<>(RelativeSide.class);
    private final List<IChemicalTank<CHEMICAL, STACK>> tanks = new ArrayList<>();
    private final Supplier<Direction> facingSupplier;
    //TODO: Allow declaring that some sides will be the same, so can just be the same list in memory??
    //TODO: Also allow for relative sides??

    ChemicalTankHolder(Supplier<Direction> facingSupplier) {
        this.facingSupplier = facingSupplier;
    }

    void addTank(@Nonnull IChemicalTank<CHEMICAL, STACK> tank, RelativeSide... sides) {
        tanks.add(tank);
        for (RelativeSide side : sides) {
            directionalTanks.computeIfAbsent(side, k -> new ArrayList<>()).add(tank);
        }
    }

    @Nonnull
    @Override
    public List<IChemicalTank<CHEMICAL, STACK>> getTanks(@Nullable Direction direction) {
        if (direction == null || directionalTanks.isEmpty()) {
            //If we want the internal OR we have no side specification, give all of our slots
            return tanks;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        List<IChemicalTank<CHEMICAL, STACK>> slots = directionalTanks.get(side);
        if (slots == null) {
            return Collections.emptyList();
        }
        return slots;
    }
}