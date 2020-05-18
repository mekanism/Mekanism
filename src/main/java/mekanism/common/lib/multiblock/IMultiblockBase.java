package mekanism.common.lib.multiblock;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMultiblockBase {

    default MultiblockData getMultiblockData() {
        MultiblockData data = getStructure().getMultiblockData();
        if (data != null && data.isFormed()) {
            return data;
        }
        return getDefaultData();
    }

    default void setMultiblockData(MultiblockData multiblockData) {
        getStructure().setMultiblockData(multiblockData);
    }

    MultiblockData getDefaultData();

    ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack);

    Structure getStructure();

    void setStructure(Structure structure);

    default void resetStructure() {
        setStructure(new Structure(this));
    }

    BlockPos getPos();

    World getWorld();
}
