package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Set;
import mekanism.api.SerializationConstants;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MaxedModuleContainerItemPredicate implements ItemSubPredicate {

    public static final Codec<MaxedModuleContainerItemPredicate> CODEC = BuiltInRegistries.ITEM.holderByNameCodec().comapFlatMap(item -> {
        if (IModuleHelper.INSTANCE.isModuleContainer(item)) {
            return DataResult.success(new MaxedModuleContainerItemPredicate(item));
        }
        return DataResult.error(() -> "Specified item is not a module container item.");
    }, pred -> pred.item).fieldOf(SerializationConstants.ITEM).codec();
    public static final ItemSubPredicate.Type<MaxedModuleContainerItemPredicate> TYPE = new ItemSubPredicate.Type<>(CODEC);

    private final Set<ModuleData<?>> supportedModules;
    private final Holder<Item> item;

    public MaxedModuleContainerItemPredicate(Holder<Item> item) {
        this.item = item;
        this.supportedModules = IModuleHelper.INSTANCE.getSupported(this.item);
    }

    @Override
    public boolean matches(@NotNull ItemStack stack) {
        if (stack.is(item)) {
            IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
            if (container != null && container.moduleTypes().containsAll(supportedModules)) {
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