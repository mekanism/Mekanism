package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.interfaces.IHasDescription;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockTooltip<BLOCK extends Block & IHasDescription> extends ItemBlockMekanism<BLOCK> {

    private final boolean hasDetails;
    private EnumColor textColor;

    public ItemBlockTooltip(BLOCK block, Item.Properties properties) {
        this(block, false, properties, null);
    }

    public ItemBlockTooltip(BLOCK block, Item.Properties properties, EnumColor color) {
        this(block, false, properties, color);
    }

    public ItemBlockTooltip(BLOCK block, boolean hasDetails, Properties properties) {
        this(block, hasDetails, properties, null);
    }

    public ItemBlockTooltip(BLOCK block, boolean hasDetails, Properties properties, EnumColor color) {
        super(block, properties);
        this.hasDetails = hasDetails;
        this.textColor = color;
    }

    @Override
    public EnumColor getTextColor(ItemStack stack) {
        return textColor != null ? textColor : super.getTextColor(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltip.add(getBlock().getDescription().translate());
        } else if (hasDetails && MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.detailsKey)) {
            addDetails(stack, world, tooltip, flag.isAdvanced());
        } else {
            addStats(stack, world, tooltip, flag.isAdvanced());
            if (hasDetails) {
                tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getLocalizedName()));
            }
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getLocalizedName()));
        }
    }

    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
    }

    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
    }
}