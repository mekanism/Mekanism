package mekanism.common.tile.interfaces;

import javax.annotation.Nonnull;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;

//TODO: Remove this? Or should it be kept as a helper wrapper/ju
@Deprecated
public interface ITileDirectional {

    default boolean isDirectional() {
        return true;
    }

    void setFacing(@Nonnull Direction direction);

    //TODO: This shouldn't be needed because the blockstate knows what directions it can go

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