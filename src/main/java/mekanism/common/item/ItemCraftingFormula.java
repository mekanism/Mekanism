package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCraftingFormula extends ItemMekanism {

    public static ModelResourceLocation MODEL = new ModelResourceLocation("mekanism:CraftingFormula", "inventory");
    public static ModelResourceLocation INVALID_MODEL = new ModelResourceLocation("mekanism:CraftingFormulaInvalid",
          "inventory");
    public static ModelResourceLocation ENCODED_MODEL = new ModelResourceLocation("mekanism:CraftingFormulaEncoded",
          "inventory");

    public ItemCraftingFormula() {
        super();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        NonNullList<ItemStack> inv = getInventory(itemstack);

        if (inv != null) {
            addIngredientDetails(inv, list);
        }
    }

    private void addIngredientDetails(NonNullList<ItemStack> inv, List<String> list) {
        List<ItemStack> stacks = new ArrayList<>();

        for (ItemStack stack : inv) {
            if (!stack.isEmpty()) {
                boolean found = false;

                for (ItemStack iterStack : stacks) {
                    if (InventoryUtils.canStack(stack, iterStack)) {
                        iterStack.grow(stack.getCount());
                        found = true;
                    }
                }

                if (!found) {
                    stacks.add(stack);
                }
            }
        }

        list.add(EnumColor.GREY + LangUtils.localize("tooltip.ingredients") + ":");

        for (ItemStack stack : stacks) {
            list.add(EnumColor.GREY + " - " + stack.getDisplayName() + " (" + stack.getCount() + ")");
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            if (!world.isRemote) {
                setInventory(stack, null);
                setInvalid(stack, false);

                ((EntityPlayerMP) player).sendContainerToPlayer(player.openContainer);
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getInventory(stack) != null ? 1 : 64;
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        if (getInventory(stack) == null) {
            return super.getItemStackDisplayName(stack);
        }

        return super.getItemStackDisplayName(stack) + " " + (isInvalid(stack) ? EnumColor.DARK_RED + "(" + LangUtils
              .localize("tooltip.invalid")
              : EnumColor.DARK_GREEN + "(" + LangUtils.localize("tooltip.encoded")) + ")";
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

        NBTTagList tagList = ItemDataUtils.getList(stack, "Items");
        NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);

        for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(tagCount);
            byte slotID = tagCompound.getByte("Slot");

            if (slotID >= 0 && slotID < 9) {
                inventory.set(slotID, new ItemStack(tagCompound));
            }
        }

        return inventory;
    }

    public void setInventory(ItemStack stack, NonNullList<ItemStack> inv) {
        if (inv == null) {
            ItemDataUtils.removeData(stack, "Items");
            return;
        }

        NBTTagList tagList = new NBTTagList();

        for (int slotCount = 0; slotCount < 9; slotCount++) {
            if (!inv.get(slotCount).isEmpty()) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte) slotCount);
                inv.get(slotCount).writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        ItemDataUtils.setList(stack, "Items", tagList);
    }
}
