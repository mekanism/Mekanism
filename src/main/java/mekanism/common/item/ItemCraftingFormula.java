package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemCraftingFormula extends ItemMekanism {

    public static ModelResourceLocation MODEL = new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "crafting_formula"), "inventory");
    public static ModelResourceLocation INVALID_MODEL = new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "crafting_formula_invalid"), "inventory");
    public static ModelResourceLocation ENCODED_MODEL = new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "crafting_formula_encoded"), "inventory");

    public ItemCraftingFormula() {
        super("crafting_formula");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        NonNullList<ItemStack> inv = getInventory(itemstack);
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
            tooltip.add(TextComponentUtil.build(EnumColor.GRAY, Translation.of("tooltip.mekanism.ingredients"), ":"));
            for (ItemStack stack : stacks) {
                tooltip.add(TextComponentUtil.build(EnumColor.GRAY, " - ", stack.getDisplayName(), " (" + stack.getCount() + ")"));
            }
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
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
            return TextComponentUtil.build(super.getDisplayName(stack), " ", EnumColor.DARK_RED, "(", Translation.of("tooltip.mekanism.invalid"), ")");
        }
        return TextComponentUtil.build(super.getDisplayName(stack), " ", EnumColor.DARK_GREEN, "(", Translation.of("tooltip.mekanism.encoded"), ")");
    }

    public boolean isInvalid(ItemStack stack) {
        return ItemDataUtils.getBoolean(stack, "invalid");
    }

    public void setInvalid(ItemStack stack, boolean invalid) {
        ItemDataUtils.setBoolean(stack, "invalid", invalid);
    }

    public NonNullList<ItemStack> getInventory(ItemStack stack) {
        if (!ItemDataUtils.hasData(stack, "Items")) {
            return null;
        }
        ListNBT tagList = ItemDataUtils.getList(stack, "Items");
        NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < 9) {
                inventory.set(slotID, ItemStack.read(tagCompound));
            }
        }
        return inventory;
    }

    public void setInventory(ItemStack stack, NonNullList<ItemStack> inv) {
        if (inv == null) {
            ItemDataUtils.removeData(stack, "Items");
            return;
        }
        ListNBT tagList = new ListNBT();
        for (int slotCount = 0; slotCount < 9; slotCount++) {
            if (!inv.get(slotCount).isEmpty()) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putByte("Slot", (byte) slotCount);
                inv.get(slotCount).write(tagCompound);
                tagList.add(tagCompound);
            }
        }
        ItemDataUtils.setList(stack, "Items", tagList);
    }
}