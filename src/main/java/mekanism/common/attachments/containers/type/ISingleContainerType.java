package mekanism.common.attachments.containers.type;

import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;

public interface ISingleContainerType<CONTAINER extends ValueIOSerializable, ATTACHED> extends IContainerType<CONTAINER, ATTACHED> {

    @Nullable
    CONTAINER getContainer(TileEntityMekanism tile);

    @Nullable
    default CONTAINER getAttachmentContainerIfPresent(ItemAccess itemAccess) {
        //TODO - 1.21: Do we want to have create in the name instead of get
        //TODO: Add some sort of note about how the returned containers entirely ignore the size of the item access
        //TODO - 1.21: Do we need to look it up in case the max size changed since we were last saved?
        if (supports(itemAccess.getResource())) {
            //Note: Bypass creating an intermediary handler object as it is not necessary
            return createContainer(itemAccess, 0);
        }
        return null;
    }

    @Override
    default void copyToTile(TileEntityMekanism tile, DataComponentGetter componentGetter) {
        ATTACHED attachedData = componentGetter.get(getComponentType());
        if (attachedData != null) {
            CONTAINER container = getContainer(tile);
            if (container != null) {
                copyToContainer(container, attachedData);
            }
        }
    }

    default void copyToContainer(CONTAINER container, DataComponentGetter componentGetter) {
        ATTACHED attachedData = componentGetter.get(getComponentType());
        if (attachedData != null) {
            copyToContainer(container, attachedData);
        }
    }

    void copyToContainer(CONTAINER container, ATTACHED attached);

    @Override
    default void copyFromTile(TileEntityMekanism tile, DataComponentMap.Builder builder) {
        CONTAINER container = getContainer(tile);
        if (container != null) {
            ATTACHED attachedData = copyFromTile(tile, container);
            if (attachedData != null) {
                builder.set(getComponentType(), attachedData);
            }
        }
    }

    @Nullable
    default ATTACHED copyFromTile(TileEntityMekanism tile, CONTAINER container) {
        return attachedCopyOf(container);
    }

    @Nullable
    ATTACHED attachedCopyOf(CONTAINER container);

    default void attachCopyToStack(CONTAINER container, ItemStack stack) {
        ATTACHED attached = attachedCopyOf(container);
        if (attached != null) {
            stack.set(getComponentType(), attached);
        }
    }

    @Override
    default void saveTo(ValueOutput output, TileEntityMekanism tile) {
        saveTo(output, getContainer(tile));
    }

    default void saveTo(ValueOutput output, @Nullable CONTAINER container) {
        if (container != null) {
            String containerTag = getTag();
            ValueOutput storedContainer = output.child(containerTag);
            container.serialize(storedContainer);
            if (storedContainer.isEmpty()) {
                output.discard(containerTag);
            }
        }
    }

    @Override
    default void readFrom(ValueInput input, TileEntityMekanism tile) {
        readFrom(input, getContainer(tile));
    }

    default void readFrom(ValueInput input, @Nullable CONTAINER container) {
        if (container != null) {
            input.child(getTag()).ifPresent(container::deserialize);
        }
    }
}