
package thermalexpansion.api.item;

import java.util.Map;
import java.util.TreeMap;

import net.minecraft.item.ItemStack;

public final class ItemRegistry {

    private static final Map<String, ItemStack> registry = new TreeMap<String, ItemStack>();

    /**
     * Returns an ItemStack containing the item that corresponds to the provided name.
     * 
     * @param name
     *            Name of the item.
     * @param qty
     *            Requested quantity of the item.
     */
    public static ItemStack getItem(String name, int qty) {

        ItemStack result = registry.get(name);
        if (result != null) {
            result = result.copy();
            result.stackSize = qty;
        }
        return result;
    }

    /**
     * Registers a new item with the ItemRegistry.
     * 
     * @param name
     *            Name of the item.
     * @param item
     *            ItemStack representing the item.
     */
    public static void registerItem(String name, ItemStack item) {

        registry.put(name, item);
    }

    /**
     * Print a list of all currently registered items to the console.
     */
    public static void printItemNames() {

        System.out.println("Printing all registered Thermal Expansion items:");
        for (String itemName : registry.keySet()) {
            System.out.println(itemName);
        }
    }

    // String identifiers for obtaining Thermal Expansion Items:

    // dustIron - Dust obtained by pulverizing Iron Ore.
    // dustGold - Dust obtained by pulverizing Gold Ore.
    // dustObsidian - Dust obtained by pulverizing Obsidian.
    // dustCopper - Dust obtained by pulverizing Copper Ore.
    // dustTin - Dust obtained by pulverizing Tin Ore.
    // dustSilver - Dust obtained by pulverizing Silver Ore.
    // dustLead - Dust obtained by pulverizing Lead Ore.
    // dustNickel - Dust obtained by pulverizing Ferrous Ore.
    // dustPlatinum - Dust obtained as a secondary output from pulverizing Ferrous Ore.
    // dustElectrum - Dust obtained by crafting Gold and Silver Dusts together.
    // dustInvar - Dust obtained by crafting 2 Iron Dusts and Ferrous Dust.
    // dustBronze - Uncraftable
    // dustBrass - Uncraftable

    // ingotCopper - Ingot obtained by smelting Copper Dust.
    // ingotTin - Ingot obtained by smelting Tin Dust.
    // ingotSilver - Ingot obtained by smelting Silver Dust.
    // ingotLead - Ingot obtained by smelting Lead Dust.
    // ingotNickel - Ingot obtained by smelting Ferrous Dust.
    // ingotPlatinum - Ingot obtained by smelting Shiny Dust.
    // ingotElectrum - Ingot obtained by smelting Electrum Dust.
    // ingotInvar - Ingot obtained by smelting Invar Dust.

    // nuggetCopper - Nugget obtained from Copper Ingots.
    // nuggetTin - Nugget obtained from Tin Ingots.
    // nuggetSilver - Nugget obtained from Silver Ingots.
    // nuggetLead - Nugget obtained from Lead Ingots.
    // nuggetNickel - Nugget obtained from Ferrous Ingots.
    // nuggetPlatinum - Nugget obtained from Shiny Ingots.
    // nuggetElectrum - Nugget obtained from Electrum Ingots.
    // nuggetInvar - Nugget obtained from Invar Ingots.

    // crystalSulfur - Sulfur
    // crystalNiter - Niter

    // woodchips - Woodchips obtained by putting logs in a Pulverizer.
    // sawdust - Sawdust obtained
    // through the Sawmill.
    // sawdustCompressed - Sawdust compressed into one item.
    // slag - Slag obtained in Smelter which can be used to create rockwool.
    // slagRich - Rich Slag obtained in Smelter which can be used to boost ore output.

    // pneumaticServo - Used in Thermal Expansion recipes for machines that do not use power.
    // powerCoilGold - Used in Thermal Expansion recipes for machines that receive power.
    // powerCoilSilver - Used in Thermal Expansion recipes for machines that send power.
    // powerCoilElectrum - Used in Thermal Expansion recipes for machines that both send/receive
    // power.

    // gearCopper - Copper Gear.
    // gearTin - Tin Gear.
    // gearInvar - Invar Gear.

    // wrench - Cresent Hammer, rotates and dismantles things.
    // multimeter - Multimeter, used to read Conduits, Liquiducts, and Tesseracts.

    // schematic - Schematic, used in the Assembler.

    // machineFrame - Used as a crafting recipe in many Thermal Expansion machines.
    // energyCellFrameEmpty - Redstone Energy Cell before it has been filled with Liquid Redstone.
    // energyCellFrameFull - Redstone Energy Cell after it has been filled with Liquid Redstone, but
    // before it can be placed in the world.
    // energyConduitEmpty - Redstone Energy Conduit before it is filled with Liquid Redstone.
    // tesseractFrameEmpty - Tesseract before it has been filled with Liquid Ender.
    // tesseractFrameFull - Tesseract after it has been filled with Liquid Ender, but before it can
    // be placed in the world.
    // lampFrame - Glowstone Illuminator before it has been filled with Liquid Glowstone.

}
