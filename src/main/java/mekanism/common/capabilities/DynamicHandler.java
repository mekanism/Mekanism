package mekanism.common.capabilities;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class DynamicHandler<TANK> extends SimpleDynamicHandler<TANK> {

    protected final Predicate<@Nullable Direction> canExtract;
    protected final Predicate<@Nullable Direction> canInsert;

    protected DynamicHandler(Function<Direction, List<TANK>> containerSupplier, Predicate<@Nullable Direction> canExtract, Predicate<@Nullable Direction> canInsert,
          @Nullable IContentsListener listener) {
        super(containerSupplier, listener);
        this.canExtract = canExtract;
        this.canInsert = canInsert;
    }
}