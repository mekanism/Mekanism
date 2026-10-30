package mekanism.common.inventory.slot;

import java.util.function.Consumer;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerResultSlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class OutputInventorySlot extends BasicInventorySlot {

    public static OutputInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return new OutputInventorySlot(null, null, listener, x, y);
    }

    @Nullable
    private Consumer<ServerPlayer> onTakeCheck;

    private OutputInventorySlot(@Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter, @Nullable IContentsListener listener, int x, int y) {
        super(ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue(), insertionRateLimiter, extractionRateLimiter, listener, x, y);
        setSlotType(ContainerSlotType.OUTPUT);
    }

    public OutputInventorySlot setOnTakeCheck(Consumer<ServerPlayer> onTakeCheck) {
        this.onTakeCheck = onTakeCheck;
        return this;
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot(Player player) {
        if (isRecipeCraftingSlot()) {
            return new InventoryContainerResultSlot(this, getGuiX(), getGuiY(), getSlotType(), getSlotOverlay(), getWarningAdder(), player, onTakeCheck);
        }
        assert onTakeCheck == null;
        return super.createContainerSlot(player);
    }
}