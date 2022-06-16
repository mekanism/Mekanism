package mekanism.common.advancements;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.item.predicate.MaxedModuleContainerItemPredicate;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class BaseAdvancementProvider extends AdvancementProvider {

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

    @Override
    protected final void registerAdvancements(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper) {
        registerAdvancements(advancement -> {
            consumer.accept(advancement);
            existingFileHelper.trackGenerated(advancement.getId(), PackType.SERVER_DATA, ".json", "advancements");
        });
    }

    protected abstract void registerAdvancements(Consumer<Advancement> consumer);

    protected CriterionTriggerInstance hasMaxed(ItemRegistryObject<? extends IModuleContainerItem> item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(new MaxedModuleContainerItemPredicate<>(item.asItem()));
    }

    protected ExtendedAdvancementBuilder advancement(MekanismAdvancement advancement) {
        return ExtendedAdvancementBuilder.advancement(advancement, fileHelper);
    }
}