package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemCraftingFormula extends Item {

    public ItemCraftingFormula(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        NonNullList<ItemStack> inv = getInventory(itemStack);
        if (inv != null) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack stack : inv) {
                if (!stack.isEmpty()) {
                    boolean found = false;
                    for (ItemStack iterStack : stacks) {
                        if (InventoryUtils.areItemsStackable(stack, iterStack)) {
                            iterStack.grow(stack.getCount());
                            found = true;
                        }
                    }
                    if (!found) {
                        stacks.add(stack);
                    }
                }
            }
            tooltip.add(MekanismLang.INGREDIENTS.translateColored(EnumColor.GRAY));
            for (ItemStack stack : stacks) {
                tooltip.add(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.GRAY, stack, stack.getCount()));
            }
        }
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                setInventory(stack, null);
                setInvalid(stack, false);
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return hasInventory(stack) ? 1 : 64;
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        if (hasInventory(stack)) {
            if (isInvalid(stack)) {
                return TextComponentUtil.build(super.getName(stack), " ", EnumColor.DARK_RED, MekanismLang.INVALID);
            }
            return TextComponentUtil.build(super.getName(stack), " ", EnumColor.DARK_GREEN, MekanismLang.ENCODED);
        }
        return super.getName(stack);
    }

    public boolean isInvalid(ItemStack stack) {
        return ItemDataUtils.getBoolean(stack, NBTConstants.INVALID);
    }

    public void setInvalid(ItemStack stack, boolean invalid) {
        ItemDataUtils.setBoolean(stack, NBTConstants.INVALID, invalid);
    }

    public boolean hasInventory(ItemStack stack) {
        return ItemDataUtils.hasData(stack, NBTConstants.ITEMS, Tag.TAG_LIST);
    }

    @Nullable
    public NonNullList<ItemStack> getInventory(ItemStack stack) {
        if (!hasInventory(stack)) {
            return null;
        }
        ListTag tagList = ItemDataUtils.getList(stack, NBTConstants.ITEMS);
        NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundTag tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte(NBTConstants.SLOT);
            if (slotID >= 0 && slotID < 9) {
                inventory.set(slotID, ItemStack.of(tagCompound));
            }
        }
        return inventory;
    }

    public void setInventory(ItemStack stack, @Nullable NonNullList<ItemStack> inv) {
        if (inv == null) {
            ItemDataUtils.removeData(stack, NBTConstants.ITEMS);
            return;
        }
        ListTag tagList = new ListTag();
        for (int slotCount = 0; slotCount < 9; slotCount++) {
            ItemStack slotStack = inv.get(slotCount);
            if (!slotStack.isEmpty()) {
                CompoundTag stackTag = new CompoundTag();
                slotStack.save(stackTag);
                stackTag.putByte(NBTConstants.SLOT, (byte) slotCount);
                tagList.add(stackTag);
            }
        }
        ItemDataUtils.setListOrRemove(stack, NBTConstants.ITEMS, tagList);
    }
}