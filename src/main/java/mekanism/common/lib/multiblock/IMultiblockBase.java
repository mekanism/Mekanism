package mekanism.common.lib.multiblock;

import mekanism.common.tile.interfaces.ITileWrapper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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

    ItemInteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack);

    Structure getStructure(MultiblockManager<?> manager);

    boolean hasStructure(Structure structure);

    void setStructure(MultiblockManager<?> manager, Structure structure);

    default Structure resetStructure(MultiblockManager<?> manager) {
        Structure structure = new Structure(this);
        setStructure(manager, structure);
        return structure;
    }

    //Not that great a name, but is used for when we go from one formed multiblock directly to another formed multiblock
    // this allows resetting some information that normally would then get reset when going to unformed and then formed again
    default void resetForFormed() {
    }
}