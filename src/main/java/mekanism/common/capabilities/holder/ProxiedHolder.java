package mekanism.common.capabilities.holder;

import java.util.function.Predicate;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public abstract class ProxiedHolder implements IHolder {

    private final Predicate<@Nullable Direction> insertPredicate;
    private final Predicate<@Nullable Direction> extractPredicate;

    protected ProxiedHolder(Predicate<@Nullable Direction> insertPredicate, Predicate<@Nullable Direction> extractPredicate) {
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