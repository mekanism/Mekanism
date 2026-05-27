package mekanism.common.attachments.containers.type;

import java.util.List;
import java.util.function.Supplier;
import mekanism.common.attachments.containers.IAttachedContainers;
import mekanism.common.attachments.containers.creator.IContainerCreator;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public interface IContainerType<CONTAINER extends ValueIOSerializable, ATTACHED extends IAttachedContainers<?, ATTACHED>> {

    DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> getComponentType();

    default Identifier getComponentName() {
        return getComponentType().getId();
    }

    String getTag();

    String getKey();

    //TODO - 1.21: Do we want to have create in the name instead of get
    //TODO: Add some sort of note about how the returned containers entirely ignore the size of the item access
    List<CONTAINER> getAttachmentContainersIfPresent(ItemAccess itemAccess);

    void addDefaultCreators(@Nullable IEventBus eventBus, Item item, Supplier<? extends IContainerCreator<CONTAINER, ATTACHED>> defaultCreator,
          IMekanismConfig... requiredConfigs);

    /// Visible for datagen
    int getContainerCount(Item item);

    void addDefault(Item item, DataComponentMap.Builder components);

    default  <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean supports(ITEM instance) {
        return instance.has(getComponentType()) || supports(instance.typeHolder());
    }

    boolean supports(Holder<Item> item);

    default ATTACHED getOrEmpty(ItemAccess itemAccess) {
        return getOrEmpty(itemAccess.getResource());
    }

    ATTACHED getOrEmpty(DataComponentGetter stack);

    //TODO: Add some sort of note about how the returned containers entirely ignore the size of the item access
    CONTAINER createContainer(ItemAccess attachedAccess, int containerIndex);

    ATTACHED createNewAttachment(ItemResource itemType);

    boolean canHandle(TileEntityMekanism tile);

    List<CONTAINER> getContainers(TileEntityMekanism tile);

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
                storedContainer.putByte(getKey(), (byte) container);
            } else {
                storedContainers.discardLast();
            }
        }
        if (storedContainers.isEmpty()) {
            output.discard(containerTag);
        }
    }

    default void readFrom(ValueInput input, TileEntityMekanism tile) {
        readFrom(input, getContainers(tile));
    }

    default void readFrom(ValueInput input, List<? extends CONTAINER> containers) {
        ValueInputList storedContents = input.childrenListOrEmpty(getTag());
        int size = containers.size();
        for (ValueInput storedContent : storedContents) {
            byte id = storedContent.getByteOr(getKey(), (byte) -1);
            if (id >= 0 && id < size) {
                containers.get(id).deserialize(storedContent);
            }
        }
    }

    void copy(CONTAINER from, CONTAINER to);

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

    void copyFromTile(TileEntityMekanism tile, DataComponentMap.Builder builder);

    @Nullable
    ATTACHED attachedCopyOf(List<CONTAINER> containers);
}