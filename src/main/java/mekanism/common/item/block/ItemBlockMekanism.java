package mekanism.common.item.block;

import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.ITier;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ItemBlockMekanism<BLOCK extends Block> extends BlockItem {

    public ItemBlockMekanism(@NotNull BLOCK block, Item.Properties properties) {
        super(block, properties);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public BLOCK getBlock() {
        return (BLOCK) super.getBlock();
    }

    public ITier getTier() {
        return null;
    }

    private TextColor getTextColor(ItemStack stack) {
        ITier tier = getTier();
        if (tier == null) {
            if (getBlock() instanceof IColoredBlock coloredBlock) {
                return coloredBlock.getColor().getColor();
            }
            return null;
        }
        return tier.getBaseTier().getColor();
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        TextColor color = getTextColor(stack);
        if (color == null) {
            return super.getName(stack);
        }
        return TextComponentUtil.build(color, super.getName(stack));
    }
}