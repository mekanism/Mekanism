package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.registration.impl.ItemDeferredRegister;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

//TODO: Do we want an interface for getting the block easier with the correct type
public class ItemBlockColoredName extends BlockItem {

    public <BLOCK extends Block & IColoredBlock> ItemBlockColoredName(BLOCK block) {
        this(block, ItemDeferredRegister.getMekBaseProperties());
    }

    public <BLOCK extends Block & IColoredBlock> ItemBlockColoredName(BLOCK block, Item.Properties properties) {
        super(block, properties);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        EnumColor color = getColor(stack);
        if (color == EnumColor.BLACK) {
            color = EnumColor.DARK_GRAY;
        }
        return TextComponentUtil.build(color, super.getDisplayName(stack));
    }

    private EnumColor getColor(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockColoredName) {
            return ((IColoredBlock) ((ItemBlockColoredName) item).getBlock()).getColor();
        }
        return EnumColor.BLACK;
    }
}