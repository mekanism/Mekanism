package mekanism.common.advancements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.api.providers.IItemProvider;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.Mekanism;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public abstract class BaseAdvancementProvider implements DataProvider {

    private final DataGenerator.PathProvider pathProvider;
    private final ExistingFileHelper existingFileHelper;
    private final String modid;

    public BaseAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper, String modid) {
        this.modid = modid;
        this.existingFileHelper = existingFileHelper;
        this.pathProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, "advancements");
    }

    @NotNull
    @Override
    public String getName() {
        return "Advancements: " + modid;
    }

    @Override
    public void run(@NotNull CachedOutput cache) {
        registerAdvancements(advancement -> {
            ResourceLocation id = advancement.getId();
            if (existingFileHelper.exists(id, PackType.SERVER_DATA, ".json", "advancements")) {
                throw new IllegalStateException("Duplicate advancement " + id);
            }
            Path path = this.pathProvider.json(id);
            JsonObject json = advancement.deconstruct().serializeToJson();
            cleanAdvancementJson(json);
            try {
                existingFileHelper.trackGenerated(id, PackType.SERVER_DATA, ".json", "advancements");
                DataProvider.saveStable(cache, json, path);
            } catch (IOException ioexception) {
                Mekanism.logger.error("Couldn't save advancement {}", path, ioexception);
            }
        });
    }

    private void cleanAdvancementJson(JsonObject json) {
        //Remove requirements if they are default
        JsonArray requirementArray = GsonHelper.getAsJsonArray(json, DataGenJsonConstants.REQUIREMENTS, new JsonArray());
        for (int i = 0; i < requirementArray.size(); ++i) {
            JsonArray subRequirements = GsonHelper.convertToJsonArray(requirementArray.get(i), "requirements[" + i + "]");
            if (subRequirements.size() > 1) {
                //If there is more than one sub requirement there is some or logic going on, so we can't just let reading default it
                return;
            }
        }
        json.remove(DataGenJsonConstants.REQUIREMENTS);
    }

    protected abstract void registerAdvancements(@NotNull Consumer<Advancement> consumer);

    protected ExtendedAdvancementBuilder advancement(MekanismAdvancement advancement) {
        return ExtendedAdvancementBuilder.advancement(advancement, existingFileHelper);
    }

    public static InventoryChangeTrigger.TriggerInstance hasItems(ItemPredicate... predicates) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(predicates);
    }

    public static InventoryChangeTrigger.TriggerInstance hasAllItems(ItemLike... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    protected static ItemPredicate predicate(ItemLike... items) {
        return ItemPredicate.Builder.item().of(items).build();
    }

    @SafeVarargs
    protected static CriterionTriggerInstance hasItems(TagKey<Item>... tags) {
        return hasItems(Arrays.stream(tags)
              .map(tag -> ItemPredicate.Builder.item().of(tag).build())
              .toArray(ItemPredicate[]::new));
    }

    protected static ItemLike[] getItems(List<? extends IItemProvider> items, Predicate<Item> matcher) {
        return items.stream()
              .filter(itemProvider -> matcher.test(itemProvider.asItem()))
              .toArray(ItemLike[]::new);
    }
}