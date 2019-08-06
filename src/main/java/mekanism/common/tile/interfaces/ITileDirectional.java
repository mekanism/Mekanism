package mekanism.common.tile.interfaces;

import javax.annotation.Nonnull;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;

//TODO: Should this and the other interfaces just be directly part of TileEntityMekanism
// Having them separate makes it a little easier to keep track of
//TODO: 1.14 this interface may not even be needed
public interface ITileDirectional {

    default boolean isDirectional() {
        return true;
    }

    void setFacing(@Nonnull Direction direction);

    //TODO: This shouldn't be needed because the blockstate knows what directions it can go

    /**
     * Whether or not this block's orientation can be changed to a specific direction. Value of isDirectional by default
     *
     * @param facing - facing to check
     *
     * @return if the block's orientation can be changed
     */
    default boolean canSetFacing(@Nonnull Direction facing) {
        return isDirectional();
    }

    @Nonnull
    Direction getDirection();

    @Nonnull
    default Direction getOppositeDirection() {
        return getDirection().getOpposite();
    }

    //TODO
    @Nonnull
    default Direction getRightSide() {
        return MekanismUtils.getRight(getDirection());
    }

    @Nonnull
    default Direction getLeftSide() {
        return MekanismUtils.getLeft(getDirection());
    }
}