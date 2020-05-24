package mekanism.common.lib.multiblock;

import mekanism.common.tile.interfaces.ITileWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;

public interface IMultiblockBase extends ITileWrapper {

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
}
