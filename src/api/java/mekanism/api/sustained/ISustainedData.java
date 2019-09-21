package mekanism.api.sustained;

import net.minecraft.item.ItemStack;

public interface ISustainedData {

    void writeSustainedData(ItemStack itemStack);

    void readSustainedData(ItemStack itemStack);
}