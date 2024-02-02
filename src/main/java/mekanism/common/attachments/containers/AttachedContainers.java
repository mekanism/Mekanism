package mekanism.common.attachments.containers;

import java.util.List;
import mekanism.api.DataHandlerUtils;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

@NothingNullByDefault
public abstract class AttachedContainers<CONTAINER extends INBTSerializable<CompoundTag>> implements INBTSerializable<ListTag>, IContentsListener {

    protected final List<CONTAINER> containers;

    protected AttachedContainers(List<CONTAINER> containers) {
        this.containers = containers;
    }

    @Override
    public ListTag serializeNBT() {
        return DataHandlerUtils.writeContainers(this.containers);
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        DataHandlerUtils.readContainers(this.containers, nbt);
    }

    @Override
    public void onContentsChanged() {
        //TODO - 1.20.4: Do this based on the holder type? Items and entities always save
    }

    public List<CONTAINER> getContainers() {
        return containers;
    }
}