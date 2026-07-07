package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import mekanism.api.SerializationConstants;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
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
              ).build();
    }

    public static final Codec<MaxedModuleContainerItemPredicate> CODEC = BuiltInRegistries.ITEM.holderByNameCodec()
          .xmap(MaxedModuleContainerItemPredicate::new, pred -> pred.item)
          .fieldOf(SerializationConstants.ITEM).codec();
    public static final DataComponentPredicate.Type<MaxedModuleContainerItemPredicate> TYPE = new ConcreteType<>(CODEC);

    private final Holder<Item> item;

    private MaxedModuleContainerItemPredicate(Holder<Item> item) {
        this.item = item;
    }

    @Override
    public boolean matches(DataComponentGetter data) {
        if (IModuleHelper.INSTANCE.isModuleContainer(this.item)) {
            IModuleContainer container = ModuleHelper.get().getModuleContainerUnsafe(data);
            if (container.moduleTypes().containsAll(IModuleHelper.INSTANCE.getSupported(this.item))) {
                for (IModule<?> module : container.modules()) {
                    if (module.getInstalledCount() != module.getUntypedData().getMaxStackSize()) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}