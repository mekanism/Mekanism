package mekanism.common.lib.inventory;

import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.lib.SidedBlockPos;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface IAdvancedTransportEjector {

    @Nullable
    SidedBlockPos getRoundRobinTarget();

    void setRoundRobinTarget(@Nullable SidedBlockPos target);

    default void setRoundRobinTarget(Destination destination) {
        setRoundRobinTarget(SidedBlockPos.get(destination));
    }

    boolean getRoundRobin();

    void toggleRoundRobin();

    boolean canSendHome(ItemResource itemType, int amount, @Nullable TransactionContext transaction);

    TransitResponse sendHome(TransitRequest request, TransactionContext transaction);
}