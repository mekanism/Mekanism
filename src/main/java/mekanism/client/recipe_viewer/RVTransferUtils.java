package mekanism.client.recipe_viewer;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.TransactionalSlot;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.inventory.slot.FormulaicCraftingSlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import net.minecraft.world.inventory.Slot;

public class RVTransferUtils {

    private static final Int2ObjectMap<BooleanConsumer> pendingTransfers = new Int2ObjectOpenHashMap<>();

    public static void clearPendingTransfers() {
        pendingTransfers.clear();
    }

    public static void registerTransfer(int transferId, BooleanConsumer completion) {
        pendingTransfers.put(transferId, completion);
    }

    public static void completeTransfer(int transferId, boolean successful) {
        BooleanConsumer completion = pendingTransfers.remove(transferId);
        if (completion != null) {
            completion.accept(successful);
        }
    }

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