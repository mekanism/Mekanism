package mekanism.client.recipe_viewer;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.TransactionalSlot;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.inventory.slot.FormulaicCraftingSlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import net.minecraft.world.inventory.Slot;

public class RVTransferUtils {

    public static List<Slot> getFormulaicInputSlots(FormulaicAssemblicatorContainer container) {
        List<Slot> slots = new ArrayList<>();
        for (TransactionalSlot playerSlot : container.getPlayerSlots()) {
            slots.add(playerSlot);
        }
        for (InventoryContainerSlot slot : container.getInventoryContainerSlots()) {
            if (slot.getInventorySlot() instanceof InputInventorySlot) {
                slots.add(slot);
            }
        }
        return slots;
    }

    public static List<Slot> getFormulaicCraftingSlots(FormulaicAssemblicatorContainer container) {
        List<Slot> slots = new ArrayList<>(9);
        for (InventoryContainerSlot slot : container.getInventoryContainerSlots()) {
            if (slot.getInventorySlot() instanceof FormulaicCraftingSlot) {
                slots.add(slot);
            }
        }
        return slots;
    }
}