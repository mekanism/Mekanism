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

public class ItemBlockTooltip<BLOCK extends Block & IHasDescription> extends ItemBlockMekanism<BLOCK> {

    public ItemBlockTooltip(BLOCK block) {
        this(block, ItemDeferredRegister.getMekBaseProperties());
    }

    public ItemBlockTooltip(BLOCK block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            addStats(stack, world, tooltip, flag);
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getLocalizedName()));
        } else {
            addDescription(stack, world, tooltip, flag);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
    }

    @OnlyIn(Dist.CLIENT)
    public void addDescription(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        //TODO: Previously had a max width thing, is this needed or does vanilla handle it
        tooltip.add(getBlock().getDescription().translate());
    }
}