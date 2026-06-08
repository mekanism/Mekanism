package mekanism.common.item;

import java.util.function.Consumer;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.component.qio.DriveContents;
import mekanism.common.component.qio.DriveMetadata;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.QIODriveTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

public class ItemQIODrive extends Item implements IQIODriveItem {

    private final QIODriveTier tier;

    public ItemQIODrive(QIODriveTier tier, Properties properties) {
        super(properties.stacksTo(1).component(MekanismDataComponents.DRIVE_METADATA, DriveMetadata.EMPTY)
              .component(MekanismDataComponents.DRIVE_CONTENTS, DriveContents.EMPTY)
        );
        this.tier = tier;
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        DriveMetadata meta = stack.getOrDefault(MekanismDataComponents.DRIVE_METADATA, DriveMetadata.EMPTY);
        tooltipAdder.accept(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
              TextUtils.format(meta.count()), TextUtils.format(getCountCapacity())));
        tooltipAdder.accept(MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
              TextUtils.format(meta.types()), TextUtils.format(getTypeCapacity())));
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        return TextComponentUtil.build(tier.getBaseTier().getColor(), super.getName(stack));
    }

    @Override
    public long getCountCapacity() {
        return tier.getMaxCount();
    }

    @Override
    public int getTypeCapacity() {
        return tier.getMaxTypes();
    }
}
