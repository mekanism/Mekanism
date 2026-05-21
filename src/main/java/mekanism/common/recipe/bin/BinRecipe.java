package mekanism.common.recipe.bin;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.inventory.slot.BinInventorySlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.neoforged.neoforge.transfer.access.ItemAccess;

//Note: We don't bother checking anywhere to ensure the bin's item stack size is one, as we only allow bins
// to be in stacks of one anyway. If this changes at some point, then we will need to adjust this recipe
@NothingNullByDefault
public abstract class BinRecipe extends CustomRecipe {

    protected static ComponentBackedBinInventorySlot convertToSlot(ItemStack binStack) {
        return convertToSlot(ItemAccess.forStack(binStack));
    }

    //TODO - 26.1: Should any call sites use this instead?
    protected static ComponentBackedBinInventorySlot convertToSlot(ItemAccess binAccess) {
        ComponentBackedBinInventorySlot slot = BinInventorySlot.getForAccess(binAccess);
        if (slot == null) {
            throw new IllegalStateException("Expected bin stack to have an inventory");
        }
        return slot;
    }

}