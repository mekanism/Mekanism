package mekanism.common.lib.multiblock;

import mekanism.common.tile.interfaces.ITileWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;

public interface IMultiblockBase extends ITileWrapper {

    default MultiblockData getMultiblockData(MultiblockManager<?> manager) {
        MultiblockData data = getStructure(manager).getMultiblockData();
        if (data != null && data.isFormed()) {
            return data;
        }
        return getDefaultData();
    }

    default void setMultiblockData(MultiblockManager<?> manager, MultiblockData multiblockData) {
        getStructure(manager).setMultiblockData(multiblockData);
    }

    MultiblockData getDefaultData();

    ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack);

    Structure getStructure(MultiblockManager<?> manager);

    boolean hasStructure(Structure structure);

    void setStructure(MultiblockManager<?> manager, Structure structure);

    default void resetStructure(MultiblockManager<?> manager) {
        setStructure(manager, new Structure(this));
    }
}
