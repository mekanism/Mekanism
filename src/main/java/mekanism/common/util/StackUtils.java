package mekanism.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public final class StackUtils {

    private StackUtils() {
    }

    /// Get state for placement for a generic item, with our fake player
    ///
    /// @param stack  the item to place
    /// @param pos    where
    /// @param player our fake player, usually
    ///
    /// @return the result of [Block#getStateForPlacement(BlockPlaceContext)], or null if it cannot be placed in that location
    @Nullable
    public static BlockState getStateForPlacement(ItemStack stack, BlockPos pos, Player player) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            BlockPlaceContext context = new ReplacingBlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, pos);
            return blockItem.getBlock().getStateForPlacement(context);
        }
        return null;
    }

    /// @implNote Renderable check based on [net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer#shouldRender(Equippable, EquipmentSlot)]
    @Contract(value = "null -> false", pure = true)
    public static boolean isRenderableArmor(@Nullable Equippable equippable) {
        //Valid slot check based on HumanoidArmorLayer#shouldRender
        return equippable != null && equippable.assetId().isPresent() && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    private static class ReplacingBlockPlaceContext extends BlockPlaceContext {

        public ReplacingBlockPlaceContext(Player player, InteractionHand hand, ItemStack itemInHand, BlockPos pos) {
            super(player, hand, itemInHand, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, true));
            //Set replaceClicked to true to force it to act as the context for that position
            this.replaceClicked = true;
        }
    }
}