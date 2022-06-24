package mekanism.common.item.predicate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Set;
import mekanism.api.JsonConstants;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.ModuleData;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class MaxedModuleContainerItemPredicate<ITEM extends Item & IModuleContainerItem> extends CustomItemPredicate {

    public static final ResourceLocation ID = Mekanism.rl("maxed_module_container");

    private final Set<ModuleData<?>> supportedModules;
    private final ITEM item;

    public MaxedModuleContainerItemPredicate(ITEM item) {
        this.item = item;
        this.supportedModules = MekanismAPI.getModuleHelper().getSupported(new ItemStack(item));
    }

    @Override
    protected ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean matches(@NotNull ItemStack stack) {
        if (stack.getItem() == item) {
            Object2IntMap<ModuleData<?>> installedCounts = ModuleHelper.INSTANCE.loadAllCounts(stack);
            if (installedCounts.keySet().containsAll(supportedModules)) {
                for (Object2IntMap.Entry<ModuleData<?>> entry : installedCounts.object2IntEntrySet()) {
                    if (entry.getIntValue() != entry.getKey().getMaxStackSize()) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public JsonObject serializeToJson() {
        JsonObject object = super.serializeToJson();
        object.addProperty(JsonConstants.ITEM, RegistryUtils.getName(item).toString());
        return object;
    }

    public static MaxedModuleContainerItemPredicate<?> fromJson(JsonObject json) {
        String itemName = GsonHelper.getAsString(json, JsonConstants.ITEM);
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item instanceof IModuleContainerItem) {
            return new MaxedModuleContainerItemPredicate<>((Item & IModuleContainerItem) item);
        }
        throw new JsonParseException("Specified item is not a module container item.");
    }
}