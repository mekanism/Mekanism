package mekanism.common.attachments.containers;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.HolderLookup;
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

    protected abstract ContainerType<CONTAINER, ?, ?> getContainerType();

    @Nullable
    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider) {
        ListTag serialized = getContainerType().save(provider, this.containers);
        return serialized.isEmpty() ? null : serialized;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag nbt) {
        getContainerType().read(provider, this.containers, nbt);
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
}