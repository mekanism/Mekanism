package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class ItemCraftingFormula extends Item {

    public ItemCraftingFormula(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack itemStack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
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

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!world.isRemote) {
                setInventory(stack, null);
                setInvalid(stack, false);
                ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
            }
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getInventory(stack) != null ? 1 : 64;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        if (getInventory(stack) == null) {
            return super.getDisplayName(stack);
        }
        if (isInvalid(stack)) {
            return TextComponentUtil.build(super.getDisplayName(stack), " ", EnumColor.DARK_RED, MekanismLang.INVALID);
        }
        return TextComponentUtil.build(super.getDisplayName(stack), " ", EnumColor.DARK_GREEN, MekanismLang.ENCODED);
    }

    public boolean isInvalid(ItemStack stack) {
        return ItemDataUtils.getBoolean(stack, NBTConstants.INVALID);
    }

    public void setInvalid(ItemStack stack, boolean invalid) {
        ItemDataUtils.setBoolean(stack, NBTConstants.INVALID, invalid);
    }

    public NonNullList<ItemStack> getInventory(ItemStack stack) {
        if (!ItemDataUtils.hasData(stack, NBTConstants.ITEMS, NBT.TAG_LIST)) {
            return null;
        }
        ListNBT tagList = ItemDataUtils.getList(stack, NBTConstants.ITEMS);
        NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte(NBTConstants.SLOT);
            if (slotID >= 0 && slotID < 9) {
                inventory.set(slotID, ItemStack.read(tagCompound));
            }
        }
        return inventory;
    }

    public void setInventory(ItemStack stack, NonNullList<ItemStack> inv) {
        if (inv == null) {
            ItemDataUtils.removeData(stack, NBTConstants.ITEMS);
            return;
        }
        ListNBT tagList = new ListNBT();
        for (int slotCount = 0; slotCount < 9; slotCount++) {
            if (!inv.get(slotCount).isEmpty()) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putByte(NBTConstants.SLOT, (byte) slotCount);
                inv.get(slotCount).write(tagCompound);
                tagList.add(tagCompound);
            }
        }
        ItemDataUtils.setList(stack, NBTConstants.ITEMS, tagList);
    }
}