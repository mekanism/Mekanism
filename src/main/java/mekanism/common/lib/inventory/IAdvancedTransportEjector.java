package mekanism.common.lib.inventory;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.lib.SidedBlockPos;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IAdvancedTransportEjector {

    @Nullable
    SidedBlockPos getRoundRobinTarget();

    void setRoundRobinTarget(@Nullable SidedBlockPos target);

    default void setRoundRobinTarget(Destination destination) {
        setRoundRobinTarget(SidedBlockPos.get(destination));
    }

    boolean getRoundRobin();

    void toggleRoundRobin();

    boolean canSendHome(ItemResource itemType, int amount);

    TransitResponse sendHome(TransitRequest request);
}