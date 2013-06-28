
package thermalexpansion.api;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidStack;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * This class contains some general hooks that can be useful if Thermal Expansion is installed.
 */
public class ThermalExpansionInfo {

    public static ItemStack coal = new ItemStack(Item.coal, 1, 0);
    public static ItemStack charcoal = new ItemStack(Item.coal, 1, 1);

    public static int lavaFuelValue = 18000;

    public static int getFuelValue(ItemStack theFuel) {

        if (theFuel == null) {
            return 0;
        }
        if (theFuel.isItemEqual(coal)) {
            return 4800;
        }
        if (theFuel.isItemEqual(charcoal)) {
            return 3200;
        }
        int itemId = theFuel.getItem().itemID;
        if (theFuel.getItem() instanceof ItemBlock && Block.blocksList[itemId].blockMaterial == Material.wood) {
            return 450;
        }
        if (itemId == Item.stick.itemID) {
            return 150;
        }
        if (itemId == Block.sapling.blockID) {
            return 150;
        }
        return GameRegistry.getFuelValue(theFuel) * 3 / 2;
    }

    public static int getFuelValue(LiquidStack theFuel) {

        if (theFuel.itemID == Block.lavaStill.blockID) {
            return lavaFuelValue;
        }
        return 0;
    }

}
