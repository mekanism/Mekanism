package mekanism.common.attachments.component;

import java.util.Arrays;
import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class AttachedEjector implements IAttachedComponent<TileComponentEjector> {

    @Deprecated
    public static AttachedEjector createWithLegacy(IAttachmentHolder attachmentHolder) {
        AttachedEjector ejector = create();
        //TODO - 1.21: Remove this legacy way of loading data
        if (attachmentHolder instanceof ItemStack stack && !stack.isEmpty()) {
            ItemDataUtils.getAndRemoveData(stack, NBTConstants.COMPONENT_EJECTOR, CompoundTag::getCompound).ifPresent(ejector::deserializeNBT);
        }
        return ejector;
    }

    public static AttachedEjector create() {
        return new AttachedEjector(new EnumColor[EnumUtils.SIDES.length], false, null);
    }

    private final EnumColor[] inputColors;
    private boolean strictInput;
    @Nullable
    private EnumColor outputColor;

    private AttachedEjector(EnumColor[] inputColors, boolean strictInput, @Nullable EnumColor outputColor) {
        this.inputColors = inputColors;
        this.strictInput = strictInput;
        this.outputColor = outputColor;
    }

    public boolean isCompatible(AttachedEjector other) {
        if (other == this) {
            return true;
        }
        return strictInput == other.strictInput && outputColor == other.outputColor && Arrays.equals(inputColors, other.inputColors);
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag ejectorNBT = TileComponentEjector.serialize(strictInput, inputColors, outputColor);
        return ejectorNBT.isEmpty() ? null : ejectorNBT;
    }

    @Override
    public void deserializeNBT(CompoundTag ejectorNBT) {
        TileComponentEjector.deserialize(ejectorNBT, strict -> strictInput = strict, output -> outputColor = output, inputColors);
    }

    @Nullable
    public AttachedEjector copy(IAttachmentHolder holder) {
        if (!strictInput && outputColor == null && Arrays.stream(inputColors).allMatch(Objects::isNull)) {
            return null;
        }
        return new AttachedEjector(Arrays.copyOf(inputColors, inputColors.length), strictInput, outputColor);
    }
}