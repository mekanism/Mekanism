package mekanism.common.item.block;

import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemBlockMekanism<BLOCK extends Block> extends BlockItem {

    public ItemBlockMekanism(BLOCK block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public BLOCK getBlock() {
        return (BLOCK) super.getBlock();
    }

    @Override
    public Component getName(ItemStack stack) {
        //TODO - 26.2: Can this be moved into the corresponding component or somewhere better?
        BLOCK block = getBlock();
        if (block instanceof IColoredBlock coloredBlock) {
            return TextComponentUtil.build(coloredBlock.getColor(), super.getName(stack));
        }
        BaseTier tier = Attribute.getBaseTier(block);
        if (tier == null) {
            return super.getName(stack);
        }
        return TextComponentUtil.build(tier.getTextColor(), super.getName(stack));
    }
}