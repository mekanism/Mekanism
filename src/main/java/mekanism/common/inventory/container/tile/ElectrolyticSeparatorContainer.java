package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.api.gas.IGasItem;
import mekanism.common.MekanismFluids;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotStorageTank;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class ElectrolyticSeparatorContainer extends MekanismTileContainer<TileEntityElectrolyticSeparator> {

    public ElectrolyticSeparatorContainer(int id, PlayerInventory inv, TileEntityElectrolyticSeparator tile) {
        super(MekanismContainerTypes.ELECTROLYTIC_SEPARATOR, id, inv, tile);
    }

    public ElectrolyticSeparatorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityElectrolyticSeparator.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotID != 0 && slotID != 1 && slotID != 2 && slotID != 3) {
                if (isCorrectFluid(slotStack)) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotStack.getItem() instanceof IGasItem) {
                    if (((IGasItem) slotStack.getItem()).getGas(slotStack) != null) {
                        if (((IGasItem) slotStack.getItem()).getGas(slotStack).getGas() == MekanismFluids.Hydrogen) {
                            if (!mergeItemStack(slotStack, 1, 2, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else if (((IGasItem) slotStack.getItem()).getGas(slotStack).getGas() == MekanismFluids.Oxygen) {
                            if (!mergeItemStack(slotStack, 2, 3, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    } else if (((IGasItem) slotStack.getItem()).getGas(slotStack) == null) {
                        if (!mergeItemStack(slotStack, 1, 2, false)) {
                            if (!mergeItemStack(slotStack, 2, 3, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                } else if (ChargeUtils.canBeDischarged(slotStack)) {
                    if (!mergeItemStack(slotStack, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID <= 30) {
                    if (!mergeItemStack(slotStack, 31, inventorySlots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
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

    public boolean isCorrectFluid(ItemStack itemStack) {
        return Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(itemStack);
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 0, 26, 35));
        addSlot(new SlotStorageTank(tile, 1, 59, 52));
        addSlot(new SlotStorageTank(tile, 2, 101, 52));
        addSlot(new SlotDischarge(tile, 3, 143, 35));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.electrolytic_separator");
    }
}