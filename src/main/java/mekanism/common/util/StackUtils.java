package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StackUtils {

    private StackUtils() {
    }

    //TODO - 26.1: Evaluate moving remainder of uses to copyWithCount. This method mainly is just useful for better handling when size is <= 0
    public static ItemStack size(ItemStack stack, int size) {
        return size <= 0 ? ItemStack.EMPTY : stack.copyWithCount(size);
    }

    //TODO - 26.1: validate and then add as docs that we don't need to also be modifying toAdd
    public static void merge(@NotNull List<IInventorySlot> orig, @NotNull List<IInventorySlot> toAdd, Object2IntMap<ItemResource> rejects, TransactionContext transaction) {
        StorageUtils.validateSizeMatches(orig, toAdd, "slot");
        for (int i = 0, slotCount = toAdd.size(); i < slotCount; i++) {
            IInventorySlot toAddSlot = toAdd.get(i);
            if (!toAddSlot.isEmpty()) {
                ItemResource toAddResource = toAddSlot.getResource();
                int toAddAmount = toAddSlot.amount();
                //TODO - 26.1: Validate all callers have this work with the given automation type
                // Also how much do we care about merging identical slots? Should we use the InventoryUtils#insertItem helper
                // to try inserting against all the slots of the other?
                int added = orig.get(i).insert(toAddResource, toAddAmount, transaction, AutomationType.INTERNAL);
                if (added < toAddAmount) {
                    //Add any remainder to the rejects
                    rejects.mergeInt(toAddResource, toAddAmount - added, Integer::sum);
                }
            }
        }
    }

    /**
     * Get state for placement for a generic item, with our fake player
     *
     * @param stack  the item to place
     * @param pos    where
     * @param player our fake player, usually
     *
     * @return the result of {@link Block#getStateForPlacement(BlockPlaceContext)}, or null if it cannot be placed in that location
     */
    @Nullable
    public static BlockState getStateForPlacement(ItemStack stack, BlockPos pos, Player player) {
        return Block.byItem(stack.getItem()).getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND,
              new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false))));
    }

    /**
     * @implNote Renderable check based on {@link net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer#shouldRender(Equippable, EquipmentSlot)}
     */
    @Contract(value = "null -> false", pure = true)
    public static boolean isRenderableArmor(@Nullable Equippable equippable) {
        //Valid slot check based on HumanoidArmorLayer#shouldRender
        return equippable != null && equippable.assetId().isPresent() && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }
}