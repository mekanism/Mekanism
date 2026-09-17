package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import mekanism.api.SerializationConstants;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.ModuleHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public record MaxedModuleContainerItemPredicate(Holder<Item> item) implements DataComponentPredicate {

    public static final Codec<MaxedModuleContainerItemPredicate> CODEC = BuiltInRegistries.ITEM.holderByNameCodec()
          .xmap(MaxedModuleContainerItemPredicate::new, MaxedModuleContainerItemPredicate::item)
          .fieldOf(SerializationConstants.ITEM).codec();
    public static final DataComponentPredicate.Type<MaxedModuleContainerItemPredicate> TYPE = new ConcreteType<>(CODEC);

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