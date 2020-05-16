package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.tier.QIODriveTier;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemQIODrive extends Item implements IQIODriveItem {

    private QIODriveTier tier;

    public ItemQIODrive(QIODriveTier tier, Properties properties) {
        super(properties.maxStackSize(1));
        this.tier = tier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        DriveMetadata meta = DriveMetadata.load(stack);
        tooltip.add(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
              QIOFrequency.formatItemCount(meta.getCount()), QIOFrequency.formatItemCount(getCountCapacity(stack))));
        tooltip.add(MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
              QIOFrequency.formatItemCount(meta.getTypes()), QIOFrequency.formatItemCount(getTypeCapacity(stack))));
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
