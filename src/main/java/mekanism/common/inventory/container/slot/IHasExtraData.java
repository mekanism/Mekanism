package mekanism.common.inventory.container.slot;

import java.util.function.Consumer;
import mekanism.common.inventory.container.sync.ISyncableData;
import net.minecraft.entity.player.PlayerEntity;

public interface IHasExtraData {

    /**
     * @param player "Owner" of the inventory
     */
    void addTrackers(PlayerEntity player, Consumer<ISyncableData> tracker);
}