package mekanism.common.util;

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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class StackUtils {

    private StackUtils() {
    }

    //TODO - 26.1: Evaluate moving remainder of uses to copyWithCount. This method mainly is just useful for better handling when size is <= 0
    public static ItemStack size(ItemStack stack, int size) {
        return size <= 0 ? ItemStack.EMPTY : stack.copyWithCount(size);
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