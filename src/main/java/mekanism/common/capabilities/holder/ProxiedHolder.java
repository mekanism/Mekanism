package mekanism.common.capabilities.holder;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.util.Direction;

public abstract class ProxiedHolder implements IHolder {

    private final Predicate<Direction> insertPredicate;
    private final Predicate<Direction> extractPredicate;

    protected ProxiedHolder(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate) {
        this.insertPredicate = insertPredicate;
        this.extractPredicate = extractPredicate;
    }

    @Override
    public boolean canInsert(@Nullable Direction side) {
        return insertPredicate.test(side);
    }

    @Override
    public boolean canExtract(@Nullable Direction side) {
        return extractPredicate.test(side);
    }
}