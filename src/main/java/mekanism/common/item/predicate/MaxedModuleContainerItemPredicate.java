package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Set;
import mekanism.api.JsonConstants;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import org.jetbrains.annotations.NotNull;

public class MaxedModuleContainerItemPredicate implements ICustomItemPredicate {

    private final Set<ModuleData<?>> supportedModules;
    private final Item item;

    public MaxedModuleContainerItemPredicate(Item item) {
        this.item = item;
        this.supportedModules = IModuleHelper.INSTANCE.getSupported(this.item);
    }

    @Override
    public boolean test(@NotNull ItemStack stack) {
        if (stack.is(item)) {
            IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainerNullable(stack);
            if (container != null && container.moduleTypes().containsAll(supportedModules)) {
                for (IModule<?> module : container.modules()) {
                    if (module.getInstalledCount() != module.getData().getMaxStackSize()) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Codec<? extends ICustomItemPredicate> codec() {
        return MekanismItemPredicates.MAXED_MODULE_CONTAINER_ITEM.get();
    }

    static Codec<MaxedModuleContainerItemPredicate> makeCodec() {
        return BuiltInRegistries.ITEM.byNameCodec().comapFlatMap(item -> {
            if (IModuleHelper.INSTANCE.isModuleContainer(item)) {
                return DataResult.success(new MaxedModuleContainerItemPredicate(item));
            }
            return DataResult.error(() -> "Specified item is not a module container item.");
        }, pred -> pred.item).fieldOf(JsonConstants.ITEM).codec();
    }
}