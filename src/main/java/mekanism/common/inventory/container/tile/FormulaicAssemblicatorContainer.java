package mekanism.common.inventory.container.tile;

import static mekanism.common.tile.TileEntityFormulaicAssemblicator.SLOT_CRAFT_MATRIX_FIRST;
import static mekanism.common.tile.TileEntityFormulaicAssemblicator.SLOT_ENERGY;
import static mekanism.common.tile.TileEntityFormulaicAssemblicator.SLOT_FORMULA;
import static mekanism.common.tile.TileEntityFormulaicAssemblicator.SLOT_INPUT_FIRST;
import static mekanism.common.tile.TileEntityFormulaicAssemblicator.SLOT_OUTPUT_FIRST;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.inventory.slot.SlotSpecific;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FormulaicAssemblicatorContainer extends MekanismTileContainer<TileEntityFormulaicAssemblicator> {

    public FormulaicAssemblicatorContainer(int id, PlayerInventory inv, TileEntityFormulaicAssemblicator tile) {
        super(MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR, id, inv, tile);
    }

    public FormulaicAssemblicatorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFormulaicAssemblicator.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (ChargeUtils.canBeDischarged(slotStack)) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 34, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.getItem() instanceof ItemCraftingFormula) {
                if (slotID != 1) {
                    if (!mergeItemStack(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 34, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID >= 2 && slotID <= 19) {
                if (!mergeItemStack(slotStack, 34, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (tile.formula == null || tile.formula.isIngredient(tile.getWorld(), slotStack)) {
                if (!mergeItemStack(slotStack, 2, 20, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID >= 34 && slotID <= 60) {
                if (!mergeItemStack(slotStack, 61, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID > 60) {
                if (!mergeItemStack(slotStack, 34, 60, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 34, inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
            if (slotStack.getCount() == 0) {
                currentSlot.putStack(ItemStack.EMPTY);
            } else {
                currentSlot.onSlotChanged();
            }
            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            currentSlot.onTake(player, slotStack);
        }
        return stack;
    }

    @Override
    protected void addSlots() {
        addSlot(new SlotDischarge(tile, SLOT_ENERGY, 152, 76));
        addSlot(new SlotSpecific(tile, SLOT_FORMULA, 6, 26, ItemCraftingFormula.class));
        for (int slotY = 0; slotY < 2; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlot(new Slot(tile, slotX + slotY * 9 + SLOT_INPUT_FIRST, 8 + slotX * 18, 98 + slotY * 18));
            }
        }
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 3; slotX++) {
                addSlot(new Slot(tile, slotX + slotY * 3 + SLOT_CRAFT_MATRIX_FIRST, 26 + slotX * 18, 17 + slotY * 18) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return !tile.autoMode;
                    }

                    @Override
                    public boolean canTakeStack(PlayerEntity player) {
                        return !tile.autoMode;
                    }

                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public boolean isEnabled() {
                        return !tile.autoMode;
                    }
                });
            }
        }

        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 2; slotX++) {
                addSlot(new SlotOutput(tile, slotX + slotY * 2 + SLOT_OUTPUT_FIRST, 116 + slotX * 18, 17 + slotY * 18));
            }
        }
    }

    @Override
    protected int getInventoryOffset() {
        return 148;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.formulaic_assemblicator");
    }
}