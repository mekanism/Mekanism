package mekanism.common.advancements;

import javax.annotation.Nonnull;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.item.predicate.MaxedModuleContainerItemPredicate;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BaseAdvancementProvider extends AdvancementProvider {

    private final String modid;

    public BaseAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper, String modid) {
        super(generator, existingFileHelper);
        this.modid = modid;
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    protected CriterionTriggerInstance hasItems(ItemLike... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    protected CriterionTriggerInstance hasMaxed(ItemRegistryObject<? extends IModuleContainerItem> item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(new MaxedModuleContainerItemPredicate<>(item.asItem()));
    }
}
