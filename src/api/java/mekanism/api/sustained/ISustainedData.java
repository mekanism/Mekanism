package mekanism.api.sustained;

import java.util.Map;
import net.minecraft.item.ItemStack;

public interface ISustainedData {

    void writeSustainedData(ItemStack itemStack);

    void readSustainedData(ItemStack itemStack);

    //Key is tile save string, value is sustained data string
    Map<String, String> getTileDataRemap();
}