package mekanism.common.util;

import mekanism.api.resource.IResourceContainer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.resource.Resource;

public class ValueUtils {

    private ValueUtils() {
    }

    public static <RESOURCE extends Resource> void storeNonEmpty(ValueOutput output, String key, IResourceContainer<RESOURCE> container) {
        if (!container.isEmpty()) {
            container.stackHelper().storeNonEmpty(output, key, container.asStack());
        }
    }

    public static <RESOURCE extends Resource> void readOrEmpty(ValueInput input, String key, IResourceContainer<RESOURCE> container) {
        container.setContents(container.stackHelper().readOrEmpty(input, key), null);
    }
}