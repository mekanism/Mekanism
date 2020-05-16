package mekanism.common.recipe;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.providers.IItemProvider;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

public class Criterion {

    public static final Map<PrimaryResource, RecipeCriterion> HAS_RESOURCE_MAP = new Object2ObjectOpenHashMap<>();

    static {
        for (PrimaryResource resource : PrimaryResource.values()) {
            HAS_RESOURCE_MAP.put(resource, has(resource.getName(), MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, resource)));
        }
    }

    public static final RecipeCriterion HAS_BRONZE = has("bronze", MekanismTags.Items.INGOTS_BRONZE);
    public static final RecipeCriterion HAS_LAPIS_LAZULI = has("lapis_lazuli", Tags.Items.GEMS_LAPIS);
    public static final RecipeCriterion HAS_REFINED_GLOWSTONE = has("refined_glowstone", MekanismTags.Items.INGOTS_REFINED_GLOWSTONE);
    public static final RecipeCriterion HAS_REFINED_OBSIDIAN = has("refined_obsidian", MekanismTags.Items.INGOTS_REFINED_OBSIDIAN);
    public static final RecipeCriterion HAS_STEEL = has("steel", MekanismTags.Items.INGOTS_STEEL);
    public static final RecipeCriterion HAS_BASIC_CIRCUIT = has("basic_circuit", MekanismTags.Items.CIRCUITS_BASIC);
    public static final RecipeCriterion HAS_ADVANCED_CIRCUIT = has("advanced_circuit", MekanismTags.Items.CIRCUITS_ADVANCED);
    public static final RecipeCriterion HAS_ELITE_CIRCUIT = has("elite_circuit", MekanismTags.Items.CIRCUITS_ELITE);
    public static final RecipeCriterion HAS_ULTIMATE_CIRCUIT = has("ultimate_circuit", MekanismTags.Items.CIRCUITS_ULTIMATE);
    public static final RecipeCriterion HAS_BASIC_ALLOY = has("basic_alloy", MekanismTags.Items.ALLOYS_BASIC);
    public static final RecipeCriterion HAS_INFUSED_ALLOY = has("infused_alloy", MekanismTags.Items.ALLOYS_INFUSED);
    public static final RecipeCriterion HAS_REINFORCED_ALLOY = has("reinforced_alloy", MekanismTags.Items.ALLOYS_REINFORCED);
    public static final RecipeCriterion HAS_ATOMIC_ALLOY = has("atomic_alloy", MekanismTags.Items.ALLOYS_ATOMIC);
    public static final RecipeCriterion HAS_ENERGY_TABLET = has(MekanismItems.ENERGY_TABLET);
    public static final RecipeCriterion HAS_ELECTROLYTIC_CORE = has(MekanismItems.ELECTROLYTIC_CORE);
    public static final RecipeCriterion HAS_STEEL_CASING = has(MekanismBlocks.STEEL_CASING);
    public static final RecipeCriterion HAS_BIO_FUEL = has("bio_fuel", MekanismTags.Items.FUELS_BIO);

    public static final RecipeCriterion HAS_METALLURGIC_INFUSER = has(MekanismBlocks.METALLURGIC_INFUSER);
    public static final RecipeCriterion HAS_SOLAR_NEUTRON_ACTIVATOR = has(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
    public static final RecipeCriterion HAS_CHEMICAL_CRYSTALLIZER = has(MekanismBlocks.CHEMICAL_CRYSTALLIZER);
    public static final RecipeCriterion HAS_CHEMICAL_DISSOLUTION_CHAMBER = has(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER);
    public static final RecipeCriterion HAS_CHEMICAL_INFUSER = has(MekanismBlocks.CHEMICAL_INFUSER);
    public static final RecipeCriterion HAS_CHEMICAL_INJECTION_CHAMBER = has(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER);
    public static final RecipeCriterion HAS_CHEMICAL_OXIDIZER = has(MekanismBlocks.CHEMICAL_OXIDIZER);
    public static final RecipeCriterion HAS_CHEMICAL_WASHER = has(MekanismBlocks.CHEMICAL_WASHER);
    public static final RecipeCriterion HAS_COMBINER = has(MekanismBlocks.COMBINER);
    public static final RecipeCriterion HAS_CRUSHER = has(MekanismBlocks.CRUSHER);
    public static final RecipeCriterion HAS_ELECTROLYTIC_SEPARATOR = has(MekanismBlocks.ELECTROLYTIC_SEPARATOR);
    public static final RecipeCriterion HAS_ENRICHMENT_CHAMBER = has(MekanismBlocks.ENRICHMENT_CHAMBER);
    public static final RecipeCriterion HAS_OSMIUM_COMPRESSOR = has(MekanismBlocks.OSMIUM_COMPRESSOR);
    public static final RecipeCriterion HAS_PRECISION_SAWMILL = has(MekanismBlocks.PRECISION_SAWMILL);
    public static final RecipeCriterion HAS_PRESSURIZED_REACTION_CHAMBER = has(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER);
    public static final RecipeCriterion HAS_PURIFICATION_CHAMBER = has(MekanismBlocks.PURIFICATION_CHAMBER);
    public static final RecipeCriterion HAS_ROTARY_CONDENSENTRATOR = has(MekanismBlocks.ROTARY_CONDENSENTRATOR);
    public static final RecipeCriterion HAS_THERMAL_EVAPORATION_CONTROLLER = has(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER);
    public static final RecipeCriterion HAS_ISOTOPIC_CENTRIFUGE = has(MekanismBlocks.ISOTOPIC_CENTRIFUGE);

    private static InventoryChangeTrigger.Instance hasItem(net.minecraft.util.IItemProvider itemIn) {
        return hasItem(ItemPredicate.Builder.create().item(itemIn).build());
    }

    private static InventoryChangeTrigger.Instance hasItem(Tag<Item> tagIn) {
        return hasItem(ItemPredicate.Builder.create().tag(tagIn).build());
    }

    private static InventoryChangeTrigger.Instance hasItem(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.Instance(MinMaxBounds.IntBound.UNBOUNDED, MinMaxBounds.IntBound.UNBOUNDED, MinMaxBounds.IntBound.UNBOUNDED, predicates);
    }

    public static RecipeCriterion has(IItemProvider item) {
        return RecipeCriterion.of("has_" + item.getName(), hasItem(item));
    }

    public static RecipeCriterion has(Item item) {
        return RecipeCriterion.of("has_" + item.getRegistryName().getPath(), hasItem(item));
    }

    public static RecipeCriterion has(String name, Tag<Item> tag) {
        return RecipeCriterion.of("has_" + name, hasItem(tag));
    }
}