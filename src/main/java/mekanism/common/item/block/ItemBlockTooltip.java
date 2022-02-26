package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.util.InventoryUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ItemBlockTooltip<BLOCK extends Block & IHasDescription> extends ItemBlockMekanism<BLOCK> {

    private final boolean hasDetails;

    public ItemBlockTooltip(BLOCK block, Item.Properties properties) {
        this(block, false, properties);
    }

    public ItemBlockTooltip(BLOCK block, boolean hasDetails, Properties properties) {
        super(block, properties);
        this.hasDetails = hasDetails;
    }

    @Override
    public void onDestroyed(@Nonnull ItemEntity item, @Nonnull DamageSource damageSource) {
        //Try to drop the inventory contents if we are a block item that persists our inventory
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltip.add(getBlock().getDescription().translate());
        } else if (hasDetails && MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addDetails(stack, world, tooltip, flag);
        } else {
            addStats(stack, world, tooltip, flag);
            if (hasDetails) {
                tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
        }
    }

    protected void addStats(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
    }

    protected void addDetails(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
    }
}