package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Set;
import mekanism.api.SerializationConstants;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class MaxedModuleContainerItemPredicate implements DataComponentPredicate {

    public static <T extends Item & IModuleContainerItem> ItemPredicate build(HolderLookup.Provider registry, ItemRegistryObject<T> item) {
        return ItemPredicate.Builder.item()
              .of(registry.lookupOrThrow(Registries.ITEM), item)
              .withComponents(
                    DataComponentMatchers.Builder.components()
                          .partial(TYPE, new MaxedModuleContainerItemPredicate(item))
                          .build()
              )
              .build();
    }

    public static final Codec<MaxedModuleContainerItemPredicate> CODEC = BuiltInRegistries.ITEM.holderByNameCodec().comapFlatMap(item -> {
        if (IModuleHelper.INSTANCE.isModuleContainer(item)) {
            return DataResult.success(new MaxedModuleContainerItemPredicate(item));
        }
        return DataResult.error(() -> "Specified item is not a module container item.");
    }, pred -> pred.item).fieldOf(SerializationConstants.ITEM).codec();
    public static final DataComponentPredicate.Type<MaxedModuleContainerItemPredicate> TYPE = new ConcreteType<>(CODEC);

    private final Set<ModuleData<?>> supportedModules;
    private final Holder<Item> item;

    public MaxedModuleContainerItemPredicate(Holder<Item> item) {
        this.item = item;
        this.supportedModules = IModuleHelper.INSTANCE.getSupported(this.item);
    }

    @Override
    public boolean matches(DataComponentGetter stack) {
        IModuleContainer container = ModuleHelper.get().getModuleContainerUnsafe(stack);
        if (container.moduleTypes().containsAll(supportedModules)) {
            for (IModule<?> module : container.modules()) {
                if (module.getInstalledCount() != module.getUntypedData().getMaxStackSize()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}