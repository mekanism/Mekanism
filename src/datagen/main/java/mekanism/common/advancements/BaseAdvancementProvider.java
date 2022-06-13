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
import net.minecraft.world.level.ItemLike;
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

    protected CriterionTriggerInstance hasItems(ItemLike... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    protected CriterionTriggerInstance hasMaxed(ItemRegistryObject<? extends IModuleContainerItem> item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(new MaxedModuleContainerItemPredicate<>(item.asItem()));
    }

    //TODO - 1.19: Change descriptions to be more descriptive of what needs to be done. Only the names should be fancy

    //TODO - 1.19: Advancements to add:
    // - Configurator
    // - Network reader
    // - Dictionary
    // - Electric Bow/flamethrower? not sure
    // - Scuba gear
    // - stone generator upgrade after dm -> preventing random holes in the ground since 2021
    // - any enriched material
    // - bins
    // - security desk (mainly multiplayer but still worthwhile)
    // - waste barrel
    // - various machines? Not sure where is a good breaking point
    // - personal storage (personal chest/barrel)
    // - logistical sorter
    // - different transmitter types just so they can figure out the names easier
    // - cardboard box
    // - set a skin on a robit?
    // Mekanism additions:
    // - Any balloon
    // - Any glow panel??
    // Mekanism Generators:
    // - various generators
    // Mekanism Tools:
    // - Paxel
    // - Shields
    // - Different tiers of gear?
}