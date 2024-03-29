package mekanism.common.capabilities;

import java.util.List;
import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class SimpleDynamicHandler<TANK> implements IContentsListener {

    protected final Function<Direction, List<TANK>> containerSupplier;
    @Nullable
    private final IContentsListener listener;

    protected SimpleDynamicHandler(Function<Direction, List<TANK>> containerSupplier, @Nullable IContentsListener listener) {
        this.containerSupplier = containerSupplier;
        this.listener = listener;
    }

    @Override
    public void onContentsChanged() {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }
}