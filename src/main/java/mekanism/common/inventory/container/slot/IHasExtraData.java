package mekanism.common.inventory.container.slot;

import java.util.function.Consumer;
import mekanism.common.inventory.container.sync.ISyncableData;
import net.minecraft.world.entity.player.Player;

public interface IHasExtraData {

    /**
     * @param player "Owner" of the inventory
     */
    void addTrackers(Player player, Consumer<ISyncableData> tracker);
}