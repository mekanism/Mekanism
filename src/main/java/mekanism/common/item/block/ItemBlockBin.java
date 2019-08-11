package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ITieredItem;
import mekanism.common.tier.BinTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.TextComponentUtil;
import mekanism.common.util.TextComponentUtil.Translation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockBin extends ItemBlockTooltip<BlockBin> implements ITieredItem<BinTier> {

    public ItemBlockBin(BlockBin block) {
        super(block, new Item.Properties().maxStackSize(1));
    }

    @Nullable
    @Override
    public BinTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockBin) {
            return ((ItemBlockBin) item).getBlock().getTier();
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        InventoryBin inv = new InventoryBin(itemstack);
        if (inv.getItemCount() > 0) {
            tooltip.add(TextComponentUtil.build(EnumColor.BRIGHT_GREEN, inv.getItemType().getDisplayName()));
            String amountStr = inv.getItemCount() == Integer.MAX_VALUE ? LangUtils.localize("gui.infinite") : "" + inv.getItemCount();
            tooltip.add(TextComponentUtil.build(EnumColor.PURPLE, Translation.of("mekanism.tooltip.itemAmount"), ": ", EnumColor.GREY, amountStr));
        } else {
            tooltip.add(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("mekanism.gui.empty")));
        }
        BinTier tier = getTier(itemstack);
        if (tier != null) {
            int cap = tier.getStorage();
            if (cap == Integer.MAX_VALUE) {
                tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("mekanism.tooltip.capacity"), ": ", EnumColor.GREY,
                      Translation.of("mekanism.gui.infinite"), " ", Translation.of("mekanism.transmission.Items")));
            } else {
                tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("mekanism.tooltip.capacity"), ": ", EnumColor.GREY,
                      cap, " ", Translation.of("mekanism.transmission.Items")));
            }
        }
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return ItemDataUtils.hasData(stack, "newCount");
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack stack) {
        if (!ItemDataUtils.hasData(stack, "newCount")) {
            return ItemStack.EMPTY;
        }
        int newCount = ItemDataUtils.getInt(stack, "newCount");
        ItemDataUtils.removeData(stack, "newCount");
        ItemStack ret = stack.copy();
        ItemDataUtils.setInt(ret, "itemCount", newCount);
        return ret;
    }
}