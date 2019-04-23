package mekanism.common.content.transporter;

import java.util.List;
import mekanism.common.OreDictCache;
import mekanism.common.util.ItemRegistryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public abstract class Finder {

    public abstract boolean modifies(ItemStack stack);

    public static class FirstFinder extends Finder {

        @Override
        public boolean modifies(ItemStack stack) {
            return true;
        }
    }

    public static class OreDictFinder extends Finder {

        public String oreDictName;

        public OreDictFinder(String name) {
            oreDictName = name;
        }

        @Override
        public boolean modifies(ItemStack stack) {
            List<String> oreKeys = OreDictCache.getOreDictName(stack);

            if (oreKeys.isEmpty()) {
                return false;
            }

            for (String oreKey : oreKeys) {
                if (oreDictName.equals(oreKey) || oreDictName.equals("*")) {
                    return true;
                } else if (oreDictName.endsWith("*") && !oreDictName.startsWith("*")) {
                    if (oreKey.startsWith(oreDictName.substring(0, oreDictName.length() - 1))) {
                        return true;
                    }
                } else if (oreDictName.startsWith("*") && !oreDictName.endsWith("*")) {
                    if (oreKey.endsWith(oreDictName.substring(1))) {
                        return true;
                    }
                } else if (oreDictName.startsWith("*") && oreDictName.endsWith("*")) {
                    if (oreKey.contains(oreDictName.substring(1, oreDictName.length() - 1))) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static class ItemStackFinder extends Finder {

        public ItemStack itemType;

        public ItemStackFinder(ItemStack type) {
            itemType = type;
        }

        @Override
        public boolean modifies(ItemStack stack) {
            return itemType.getHasSubtypes() ? StackUtils.equalsWildcard(itemType, stack)
                  : itemType.getItem() == stack.getItem();
        }
    }

    public static class MaterialFinder extends Finder {

        public Material materialType;

        public MaterialFinder(Material type) {
            materialType = type;
        }

        @Override
        public boolean modifies(ItemStack stack) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) {
                return false;
            }

            return Block.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getItemDamage()).getMaterial()
                  == materialType;
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

            String id = ItemRegistryUtils.getMod(stack);

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
