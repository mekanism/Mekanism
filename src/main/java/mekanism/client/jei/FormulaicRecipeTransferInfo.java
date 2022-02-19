package mekanism.client.jei;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.inventory.slot.FormulaicCraftingSlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;

public class FormulaicRecipeTransferInfo implements IRecipeTransferInfo<FormulaicAssemblicatorContainer> {

    @Override
    public Class<FormulaicAssemblicatorContainer> getContainerClass() {
        return FormulaicAssemblicatorContainer.class;
    }

    @Override
    public ResourceLocation getRecipeCategoryUid() {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Override
    public boolean canHandle(FormulaicAssemblicatorContainer container) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(FormulaicAssemblicatorContainer container) {
        List<Slot> slots = new ArrayList<>();
        for (InventoryContainerSlot slot : container.getInventoryContainerSlots()) {
            if (slot.getInventorySlot() instanceof FormulaicCraftingSlot) {
                slots.add(slot);
            }
        }
        return slots;
    }

    @Override
    public List<Slot> getInventorySlots(FormulaicAssemblicatorContainer container) {
        List<Slot> slots = new ArrayList<>();
        slots.addAll(container.getMainInventorySlots());
        slots.addAll(container.getHotBarSlots());
        for (InventoryContainerSlot slot : container.getInventoryContainerSlots()) {
            if (slot.getInventorySlot() instanceof InputInventorySlot) {
                slots.add(slot);
            }
        }
        return slots;
    }
}