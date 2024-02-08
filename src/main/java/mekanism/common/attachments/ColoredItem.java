package mekanism.common.attachments;

import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class ColoredItem implements INBTSerializable<IntTag> {

    @Nullable
    private EnumColor color;

    public ColoredItem(IAttachmentHolder attachmentHolder) {
        loadLegacyData(attachmentHolder);
    }

    @Deprecated//TODO - 1.21: Remove this legacy way of loading data
    private void loadLegacyData(IAttachmentHolder attachmentHolder) {
        if (attachmentHolder instanceof ItemStack stack && !stack.isEmpty()) {
            ItemDataUtils.getAndRemoveData(stack, NBTConstants.COLOR, CompoundTag::getInt)
                  .map(EnumColor::byIndexStatic)
                  .ifPresent(value -> color = value);
        }
    }

    public int getTint() {
        if (color != null) {
            int[] rgbCode = color.getRgbCode();
            return FastColor.ARGB32.color(255, rgbCode[0], rgbCode[1], rgbCode[2]);
        }
        return 0xFF555555;
    }

    @Nullable
    public EnumColor getColor() {
        return color;
    }

    public void setColor(@Nullable EnumColor color) {
        this.color = color;
    }

    public boolean isCompatible(ColoredItem other) {
        return color == other.color;
    }

    @Nullable
    @Override
    public IntTag serializeNBT() {
        if (this.color == null) {
            return null;
        }
        return IntTag.valueOf(this.color.ordinal());
    }

    @Override
    public void deserializeNBT(IntTag nbt) {
        this.color = EnumColor.byIndexStatic(nbt.getAsInt());
    }
}