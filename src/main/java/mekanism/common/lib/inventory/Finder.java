package mekanism.common.lib.inventory;

import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemHandlerHelper;

public interface Finder {

    public static final Finder ANY = stack -> true;

    public static Finder tag(String tagName) {
        return stack -> {
            Set<ResourceLocation> tags = stack.getItem().getTags();
            if (tags.isEmpty()) {
                return false;
            }
            for (ResourceLocation tag : tags) {
                String tagAsString = tag.toString();
                if (tagName.equals(tagAsString) || tagName.equals("*")) {
                    return true;
                } else if (tagName.endsWith("*") && !tagName.startsWith("*")) {
                    if (tagAsString.startsWith(tagName.substring(0, tagName.length() - 1))) {
                        return true;
                    }
                } else if (tagName.startsWith("*") && !tagName.endsWith("*")) {
                    if (tagAsString.endsWith(tagName.substring(1))) {
                        return true;
                    }
                } else if (tagName.startsWith("*") && tagName.endsWith("*")) {
                    if (tagAsString.contains(tagName.substring(1, tagName.length() - 1))) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    public static Finder item(ItemStack itemType) {
        return stack -> ItemStack.areItemsEqual(itemType, stack);
    }

    public static Finder strict(ItemStack itemType) {
        return stack -> ItemHandlerHelper.canItemStacksStack(itemType, stack);
    }

    public static Finder material(Material materialType) {
        return stack -> {
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
                return false;
            }
            return Block.getBlockFromItem(stack.getItem()).getDefaultState().getMaterial() == materialType;
        };
    }

    public static Finder modID(String modID) {
        return stack -> {
            if (stack.isEmpty()) {
                return false;
            }
            String id = stack.getItem().getRegistryName().getNamespace();
            if (modID.equals(id) || modID.equals("*")) {
                return true;
            } else if (modID.endsWith("*") && !modID.startsWith("*")) {
                return id.startsWith(modID.substring(0, modID.length() - 1));
            } else if (modID.startsWith("*") && !modID.endsWith("*")) {
                return id.endsWith(modID.substring(1));
            } else if (modID.startsWith("*") && modID.endsWith("*")) {
                return id.contains(modID.substring(1, modID.length() - 1));
            }
            return false;
        };
    }

    boolean modifies(ItemStack stack);
}