package mekanism.common.attachments.component;

import java.util.Arrays;
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

    private final EnumColor[] inputColors = new EnumColor[EnumUtils.SIDES.length];
    private boolean strictInput;
    @Nullable
    private EnumColor outputColor;

    public AttachedEjector(IAttachmentHolder attachmentHolder) {
        loadLegacyData(attachmentHolder);
    }

    @Deprecated//TODO - 1.21: Remove this legacy way of loading data
    private void loadLegacyData(IAttachmentHolder attachmentHolder) {
        if (attachmentHolder instanceof ItemStack stack && !stack.isEmpty()) {
            ItemDataUtils.getAndRemoveData(stack, NBTConstants.COMPONENT_EJECTOR, CompoundTag::getCompound).ifPresent(this::deserializeNBT);
        }
    }

    public boolean isCompatible(AttachedEjector other) {
        if (other == this) {
            return true;
        }
        return strictInput == other.strictInput && outputColor == other.outputColor && Arrays.equals(inputColors, other.inputColors);
    }

    @Override
    public void copyFrom(TileComponentEjector component) {
        deserializeNBT(component.serialize());
    }

    @Override
    public void copyTo(TileComponentEjector component) {
        CompoundTag configNBT = serializeNBT();
        if (configNBT != null) {
            component.deserialize(configNBT);
        }
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
}