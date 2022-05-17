package mekanism.common.tile.interfaces;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;

public interface ISustainedData {

    void writeSustainedData(CompoundTag dataMap);

    void readSustainedData(CompoundTag dataMap);

    //Key is tile save string, value is sustained data string
    Map<String, String> getTileDataRemap();
}