package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.client.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockTooltip<BLOCK extends Block> extends ItemBlockMekanism<BLOCK> {

    public ItemBlockTooltip(BLOCK block) {
        this(block, new Item.Properties());
    }

    public ItemBlockTooltip(BLOCK block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            addStats(itemstack, world, list, flag);
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) +
                     EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
        } else {
            addDescription(itemstack, world, list, flag);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
    }

    @OnlyIn(Dist.CLIENT)
    public void addDescription(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        list.addAll(MekanismUtils.splitTooltip(LangUtils.localize("tooltip.mekanism." + getRegistryName().getPath()), itemstack));
    }
}