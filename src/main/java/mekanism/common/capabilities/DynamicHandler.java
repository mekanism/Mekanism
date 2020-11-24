package mekanism.common.capabilities;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DynamicHandler<TANK> implements IContentsListener {

    protected final Function<Direction, List<TANK>> containerSupplier;
    protected final InteractPredicate canExtract;
    protected final InteractPredicate canInsert;
    @Nullable
    private final IContentsListener listener;

    protected DynamicHandler(Function<Direction, List<TANK>> containerSupplier, InteractPredicate canExtract, InteractPredicate canInsert,
          @Nullable IContentsListener listener) {
        this.containerSupplier = containerSupplier;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.listener = listener;
    }

    @Override
    public void onContentsChanged() {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @FunctionalInterface
    public interface InteractPredicate {

        InteractPredicate ALWAYS_TRUE = (tank, side) -> true;

        boolean test(int tank, @Nullable Direction side);
    }
}