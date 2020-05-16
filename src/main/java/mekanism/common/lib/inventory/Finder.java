package mekanism.common.lib.inventory;

import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class Finder {

    public abstract boolean modifies(ItemStack stack);

    public static class FirstFinder extends Finder {

        @Override
        public boolean modifies(ItemStack stack) {
            return true;
        }
    }

    public static class TagFinder extends Finder {

        public final String tagName;

        public TagFinder(String name) {
            tagName = name;
        }

        @Override
        public boolean modifies(ItemStack stack) {
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
        }
    }

    public static class ItemStackFinder extends Finder {

        public ItemStack itemType;
        private boolean strict;

        public ItemStackFinder(ItemStack itemType, boolean strict) {
            this.itemType = itemType;
            this.strict = strict;
        }

        public static ItemStackFinder strict(ItemStack itemType) {
            return new ItemStackFinder(itemType, true);
        }

        public static ItemStackFinder lenient(ItemStack itemType) {
            return new ItemStackFinder(itemType, false);
        }

        @Override
        public boolean modifies(ItemStack stack) {
            return strict ? ItemHandlerHelper.canItemStacksStack(itemType, stack) : ItemStack.areItemsEqual(itemType, stack);
        }
    }

    public static class MaterialFinder extends Finder {

        public Material materialType;

        public MaterialFinder(Material type) {
            materialType = type;
        }

        @Override
        public boolean modifies(ItemStack stack) {
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
                return false;
            }
            return Block.getBlockFromItem(stack.getItem()).getDefaultState().getMaterial() == materialType;
        }
    }

    public static class ModIDFinder extends Finder {

        public String modID;

        public ModIDFinder(String mod) {
            modID = mod;
        }

        @Override
        public boolean modifies(ItemStack stack) {
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
        }
    }
}