package mekanism.api;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

@NothingNullByDefault
public class DataHandlerUtils {

    private DataHandlerUtils() {
    }

    /**
     * Helper to read and load a list of handler contents from a {@link ListTag}
     */
    public static void readContents(ValueInputList storedContents, String key, List<? extends ValueIOSerializable> contents) {
        //TODO - 1.21.8: Test these read/write methods work properly
        int size = contents.size();
        for (ValueInput storedContent : storedContents) {
            byte id = storedContent.getByteOr(key, (byte) -1);
            if (id >= 0 && id < size) {
                contents.get(id).deserialize(storedContent);
            }
        }
    }

    /**
     * Helper to read and load a list of handler contents to a {@link ListTag}
     */
    public static void writeContents(ValueOutputList outputList, String key, List<? extends ValueIOSerializable> contents) {
        for (int tank = 0; tank < contents.size(); tank++) {
            ValueOutput output = outputList.addChild();
            contents.get(tank).serialize(output);
            if (!output.isEmpty()) {
                output.putByte(key, (byte) tank);
            } else {
                outputList.discardLast();
            }
        }
    }
}