package mekanism.common.attachments.containers;

import java.util.List;
import mekanism.api.DataHandlerUtils;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class AttachedContainers<CONTAINER extends INBTSerializable<CompoundTag>> implements INBTSerializable<ListTag>, IContentsListener {

    @Nullable
    private final IContentsListener listener;
    protected final List<CONTAINER> containers;

    AttachedContainers(List<CONTAINER> containers, @Nullable IContentsListener listener) {
        //Ensure that the list the attachment receives is immutable. In general, we will already have an immutable type and this will not cause any copying to occur
        this.containers = List.copyOf(containers);
        this.listener = listener;
    }

    @Nullable
    @Override
    public ListTag serializeNBT() {
        ListTag serialized = DataHandlerUtils.writeContainers(this.containers);
        return serialized.isEmpty() ? null : serialized;
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        DataHandlerUtils.readContainers(this.containers, nbt);
    }

    @Override
    public void onContentsChanged() {
        if (this.listener != null) {
            this.listener.onContentsChanged();
        }
    }

    public List<CONTAINER> getContainers() {
        return containers;
    }

    /**
     * @implNote This only needs a basic check of contents as that is what would happen if the stacks were serialized. Checking the capacity of the containers can be
     * skipped.
     */
    protected abstract boolean isContainerCompatible(CONTAINER a, CONTAINER b);

    public boolean isCompatible(AttachedContainers<CONTAINER> other) {
        int containerCount = containers.size();
        if (containerCount != other.containers.size()) {
            return false;
        }
        for (int i = 0; i < containerCount; i++) {
            if (!isContainerCompatible(containers.get(i), other.containers.get(i))) {
                return false;
            }
        }
        return true;
    }
}