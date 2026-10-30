package mekanism.common.inventory.container.slot;

import java.util.function.Consumer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.warning.ISupportsWarning;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class InventoryContainerResultSlot extends InventoryContainerSlot {

    @Nullable
    private final Consumer<ServerPlayer> onTakeCheck;
    private final Player player;
    private int removeCount;

    public InventoryContainerResultSlot(BasicInventorySlot slot, int x, int y, ContainerSlotType slotType, @Nullable SlotOverlay slotOverlay,
          @Nullable Consumer<ISupportsWarning<?>> warningAdder, Player player, @Nullable Consumer<ServerPlayer> onTakeCheck) {
        this.onTakeCheck = onTakeCheck;
        this.player = player;
        super(slot, x, y, slotType, slotOverlay, warningAdder);
    }

    @Override
    public ItemStack remove(int amount) {
        IInventorySlot slot = getInventorySlot();
        if (!slot.isEmpty()) {
            this.removeCount += Math.min(amount, slot.amountAsInt());
        }
        return super.remove(amount);
    }

    @Override
    public void onTake(Player player, ItemStack carried) {
        checkTakeAchievements(carried);
        super.onTake(player, carried);
    }

    @Override
    protected void onQuickCraft(ItemStack picked, int count) {
        this.removeCount += count;
        checkTakeAchievements(picked);
    }

    @Override
    protected void checkTakeAchievements(ItemStack carried) {
        carried.onCraftedBy(this.player, this.removeCount);
        if (this.player instanceof ServerPlayer serverPlayer && onTakeCheck != null) {
            onTakeCheck.accept(serverPlayer);
        }
        this.removeCount = 0;
    }
}