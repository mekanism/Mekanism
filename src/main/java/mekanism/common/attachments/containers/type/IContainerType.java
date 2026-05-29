package mekanism.common.attachments.containers.type;

import java.util.function.Supplier;
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
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public interface IContainerType<CONTAINER extends ValueIOSerializable, ATTACHED> {

    DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> getComponentType();

    default Identifier getComponentName() {
        return getComponentType().getId();
    }

    String getTag();

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

    void saveTo(ValueOutput output, TileEntityMekanism tile);

    void readFrom(ValueInput input, TileEntityMekanism tile);

    void copy(CONTAINER from, CONTAINER to);

    void copyToTile(TileEntityMekanism tile, DataComponentGetter componentGetter);

    void copyFromTile(TileEntityMekanism tile, DataComponentMap.Builder builder);
}