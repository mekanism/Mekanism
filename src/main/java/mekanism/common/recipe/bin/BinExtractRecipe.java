package mekanism.common.recipe.bin;

import com.google.common.primitives.Ints;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@NothingNullByDefault
public class BinExtractRecipe extends BinRecipe {

    public static final BinExtractRecipe INSTANCE = new BinExtractRecipe();

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        ItemResource binData = findBinData(inv);
        if (binData.isEmpty()) {
            //If we didn't find a singular bin our recipe can't possibly match
            return false;
        }
        //Only match the recipe if we have items in the bin that we can extract from
        return !convertToSlot(ItemAccessUtils.sideEffectFreeAccess(binData)).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemResource binData = findBinData(inv);
        if (binData.isEmpty()) {
            //If we didn't find a singular bin our recipe can't possibly match
            return ItemStack.EMPTY;
        }
        //Display that our output will be the bottom stack
        ComponentBackedBinInventorySlot slot = convertToSlot(ItemAccessUtils.sideEffectFreeAccess(binData));
        LargeResourceStack<ItemResource> stack = slot.asStack();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemResource stored = stack.resource();
        int toExtract = Math.min(Ints.saturatedCast(stack.amount()), stored.getMaxStackSize());
        return stored.toStack(toExtract);
    }

    private ItemResource findBinData(CraftingInput inv) {
        ItemResource binData = ItemResource.EMPTY;
        //Note: We don't use inv#getItems as that may do unnecessary copies depending on impl
        for (int i = 0, slots = inv.size(); i < slots; ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof ItemBlockBin) {
                    if (!binData.isEmpty() || stackInSlot.count() > 1) {
                        //If we already have a bin, or our first bin has a stack size greater than one then this is not a bin recipe
                        return ItemResource.EMPTY;
                    }
                    binData = ItemResource.of(stackInSlot);
                } else {
                    //This recipe only allows extracting from bins, so it has to be only a bin
                    return ItemResource.EMPTY;
                }
            }
        }
        return binData;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        int slots = inv.size();
        NonNullList<ItemStack> remaining = NonNullList.withSize(slots, ItemStack.EMPTY);
        for (int i = 0; i < slots; ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (stackInSlot.getItem() instanceof ItemBlockBin) {
                ItemAccess binAccess = ItemAccess.forStack(stackInSlot.copy());
                ComponentBackedBinInventorySlot slot = convertToSlot(binAccess);
                LargeResourceStack<ItemResource> stack = slot.asStack();
                if (!stack.isEmpty()) {
                    ItemResource stored = stack.resource();
                    //Protect against any mods that might be doing transactional logic, such as if an auto crafter validates it has enough energy before calling this method
                    try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                        int toExtract = Math.min(Ints.saturatedCast(stack.amount()), stored.getMaxStackSize());
                        //Only attempt to do anything if there are items to try and remove
                        if (slot.extract(stored, toExtract, transaction, AutomationType.MANUAL) == toExtract) {
                            //If we extracted everything we expected to be able to, update the remaining, and commit the transaction
                            remaining.set(i, ItemAccessUtils.asStack(binAccess));
                            transaction.commit();
                        }
                    }
                }
                break;
            }
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<BinExtractRecipe> getSerializer() {
        return MekanismRecipeSerializersInternal.BIN_EXTRACT.get();
    }
}