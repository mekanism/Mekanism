package mekanism.common.recipe.bin;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.item.block.ItemBlockBin;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

//Note: We don't bother checking anywhere to ensure the bin's item stack size is one, as we only allow bins
// to be in stacks of one anyway. If this changes at some point, then we will need to adjust this recipe
@NothingNullByDefault
public abstract class BinRecipe extends CustomRecipe {

    protected static ComponentBackedBinInventorySlot convertToSlot(ItemAccess binAccess) {
        ItemResource resource = binAccess.getResource();
        if (!resource.isEmpty() && resource.getItem() instanceof ItemBlockBin && ContainerType.ITEM.createContainer(binAccess, 0) instanceof ComponentBackedBinInventorySlot binSlot) {
            return binSlot;
        }
        throw new IllegalStateException("Expected bin stack to have an inventory");
    }

}