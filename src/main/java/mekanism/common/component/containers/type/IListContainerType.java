package mekanism.common.component.containers.type;

import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.common.component.containers.IAttachedContainers;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jspecify.annotations.Nullable;

public interface IListContainerType<TYPE, CONTAINER extends ValueIOSerializable, ATTACHED extends IAttachedContainers<TYPE, ATTACHED>> extends IContainerType<CONTAINER, ATTACHED> {

    List<CONTAINER> getContainers(TileEntityMekanism tile);

    default List<TYPE> getAttachedContents(DataComponentGetter componentGetter) {
        return getOrEmpty(componentGetter).containers();
    }

    @Override
    default void copyToTile(TileEntityMekanism tile, DataComponentGetter componentGetter) {
        ATTACHED attachedData = componentGetter.get(getComponentType());
        if (attachedData != null) {
            copyToContainers(getContainers(tile), attachedData);
        }
    }

    default void copyToContainers(List<CONTAINER> containers, DataComponentGetter componentGetter) {
        ATTACHED attachedData = componentGetter.get(getComponentType());
        if (attachedData != null) {
            copyToContainers(containers, attachedData);
        }
    }

    void copyToContainers(List<CONTAINER> containers, ATTACHED attached);

    @Override
    default void copyFromTile(TileEntityMekanism tile, DataComponentMap.Builder builder) {
        List<CONTAINER> containers = getContainers(tile);
        if (!containers.isEmpty()) {
            ATTACHED attachedData = copyFromTile(tile, containers);
            if (attachedData != null) {
                builder.set(getComponentType(), attachedData);
            }
        }
    }

    @Nullable
    default ATTACHED copyFromTile(TileEntityMekanism tile, List<CONTAINER> containers) {
        return attachedCopyOf(containers);
    }

    @Nullable
    ATTACHED attachedCopyOf(List<CONTAINER> containers);

    default void attachCopyToStack(List<CONTAINER> containers, ItemStack stack) {
        ATTACHED attached = attachedCopyOf(containers);
        if (attached != null) {
            stack.set(getComponentType(), attached);
        }
    }

    @Override
    default void saveTo(ValueOutput output, TileEntityMekanism tile) {
        saveTo(output, getContainers(tile));
    }

    default void saveTo(ValueOutput output, List<? extends CONTAINER> containers) {
        if (containers.isEmpty()) {//Nothing to save, just skip
            return;
        }
        String containerTag = getTag();
        ValueOutputList storedContainers = output.childrenList(containerTag);
        for (int container = 0, size = containers.size(); container < size; container++) {
            ValueOutput storedContainer = storedContainers.addChild();
            containers.get(container).serialize(storedContainer);
            if (!storedContainer.isEmpty()) {
                storedContainer.putByte(SerializationConstants.CONTAINER, (byte) container);
            } else {
                storedContainers.discardLast();
            }
        }
        if (storedContainers.isEmpty()) {
            output.discard(containerTag);
        }
    }

    @Override
    default void readFrom(ValueInput input, TileEntityMekanism tile) {
        readFrom(input, getContainers(tile));
    }

    default void readFrom(ValueInput input, List<? extends CONTAINER> containers) {
        ValueInputList storedContents = input.childrenListOrEmpty(getTag());
        int size = containers.size();
        for (ValueInput storedContent : storedContents) {
            byte id = storedContent.getByteOr(SerializationConstants.CONTAINER, (byte) -1);
            if (id >= 0 && id < size) {
                containers.get(id).deserialize(storedContent);
            }
        }
    }
}