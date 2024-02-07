package mekanism.common.tile.interfaces;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;

public interface ISustainedData {

    void writeSustainedData(CompoundTag dataMap);

    void readSustainedData(CompoundTag dataMap);

    //Key is tile save string, value is the attachment target
    default Map<String, Holder<AttachmentType<?>>> getTileDataAttachmentRemap() {
        return Map.of();
    }

    default void readFromStack(ItemStack stack) {
    }

    default void writeToStack(ItemStack stack) {
    }
}