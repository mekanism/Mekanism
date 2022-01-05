package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.tier.QIODriveTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemQIODrive extends Item implements IQIODriveItem {

    private final QIODriveTier tier;

    public ItemQIODrive(QIODriveTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        DriveMetadata meta = DriveMetadata.load(stack);
        tooltip.add(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
              TextUtils.format(meta.count()), TextUtils.format(getCountCapacity(stack))));
        tooltip.add(MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
              TextUtils.format(meta.types()), TextUtils.format(getTypeCapacity(stack))));
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        return TextComponentUtil.build(tier.getBaseTier().getTextColor(), super.getName(stack));
    }

    @Override
    public long getCountCapacity(ItemStack stack) {
        return tier.getMaxCount();
    }

    @Override
    public int getTypeCapacity(ItemStack stack) {
        return tier.getMaxTypes();
    }
}
