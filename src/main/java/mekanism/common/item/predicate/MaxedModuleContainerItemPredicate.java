package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.Set;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class MaxedModuleContainerItemPredicate<ITEM extends Item & IModuleContainerItem> implements ICustomItemPredicate {
    private final Set<ModuleData<?>> supportedModules;
    private final ITEM item;

    public MaxedModuleContainerItemPredicate(ITEM item) {
        this.item = item;
        this.supportedModules = IModuleHelper.INSTANCE.getSupported(new ItemStack(item));
    }

    @Override
    public boolean test(@NotNull ItemStack stack) {
        if (stack.getItem() == item) {
            Reference2IntMap<ModuleData<?>> installedCounts = ModuleHelper.get().loadAllCounts(stack);
            if (installedCounts.keySet().containsAll(supportedModules)) {
                for (Reference2IntMap.Entry<ModuleData<?>> entry : installedCounts.reference2IntEntrySet()) {
                    if (entry.getIntValue() != entry.getKey().getMaxStackSize()) {
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

    static Codec<MaxedModuleContainerItemPredicate<?>> makeCodec() {
        return ForgeRegistries.ITEMS.getCodec().fieldOf("item").codec().comapFlatMap(item->{
            if (item instanceof IModuleContainerItem) {
                return DataResult.success(new MaxedModuleContainerItemPredicate<>((Item & IModuleContainerItem) item));
            }
            return DataResult.error(()->"Specified item is not a module container item.");
        }, pred->pred.item);
    }
}