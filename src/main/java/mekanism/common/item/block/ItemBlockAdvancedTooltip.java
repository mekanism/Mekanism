package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.registration.impl.ItemDeferredRegister;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: Should this just be merged with ItemBlockTooltip somehow, or maybe not extend it at all
public abstract class ItemBlockAdvancedTooltip<BLOCK extends Block & IHasDescription> extends ItemBlockTooltip<BLOCK> {

    public ItemBlockAdvancedTooltip(BLOCK block) {
        this(block, ItemDeferredRegister.getMekBaseProperties());
    }

    public ItemBlockAdvancedTooltip(BLOCK block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            addStats(stack, world, tooltip, flag);
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getLocalizedName()));
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.sneakKey.getLocalizedName(),
                  EnumColor.AQUA, MekanismKeyHandler.modeSwitchKey.getLocalizedName()));
        } else if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.modeSwitchKey)) {
            addDetails(stack, world, tooltip, flag);
        } else {
            addDescription(stack, world, tooltip, flag);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag);
}