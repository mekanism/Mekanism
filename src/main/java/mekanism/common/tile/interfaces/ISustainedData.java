package mekanism.common.tile.interfaces;

import java.util.Map;
import net.minecraft.world.item.ItemStack;

public interface ISustainedData {

    void writeSustainedData(ItemStack itemStack);

    void readSustainedData(ItemStack itemStack);

    //Key is tile save string, value is sustained data string
    Map<String, String> getTileDataRemap();
}