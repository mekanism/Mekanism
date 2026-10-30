package mekanism.common.inventory.container.slot;

import java.util.function.Consumer;
import mekanism.common.inventory.container.sync.ISyncableData;

public interface IHasExtraData {

    void addTrackers(Consumer<ISyncableData> tracker);
}