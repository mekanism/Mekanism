package mekanism.common.tile.interfaces;

import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;

public interface ITileDirectional {

    default boolean isDirectional() {
        return true;
    }

    void setFacing(Direction direction);

    Direction getDirection();

    default Direction getOppositeDirection() {
        return getDirection().getOpposite();
    }

    default Direction getRightSide() {
        return MekanismUtils.getRight(getDirection());
    }

    default Direction getLeftSide() {
        return MekanismUtils.getLeft(getDirection());
    }
}