package mekanism.common.lib.inventory;

import mekanism.common.lib.WildcardMatcher;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.ItemHandlerHelper;

public interface Finder {

    Finder ANY = stack -> true;

    static Finder item(Item itemType) {
        return stack -> itemType != Items.AIR && itemType == stack.getItem();
    }

    static Finder item(ItemStack itemType) {
        return stack -> ItemStack.isSame(itemType, stack);
    }

    static Finder strict(ItemStack itemType) {
        return stack -> ItemHandlerHelper.canItemStacksStack(itemType, stack);
    }

    static Finder tag(String tagName) {
        return stack -> !stack.isEmpty() && stack.getItem().getTags().stream().anyMatch(tag -> WildcardMatcher.matches(tagName, tag.toString()));
    }

    static Finder modID(String modID) {
        return stack -> !stack.isEmpty() && WildcardMatcher.matches(modID, MekanismUtils.getModId(stack));
    }

    static Finder material(Material materialType) {
        return stack -> {
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
                return false;
            }
            return Block.byItem(stack.getItem()).defaultBlockState().getMaterial() == materialType;
        };
    }

    boolean modifies(ItemStack stack);
}