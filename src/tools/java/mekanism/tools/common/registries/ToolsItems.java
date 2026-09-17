package mekanism.tools.common.registries;

import java.util.stream.Stream;
import mekanism.api.MekanismBlockTransformers;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.material.IPaxelMaterial;
import mekanism.tools.common.material.VanillaPaxelMaterialCreator;
import mekanism.tools.common.registration.ArmorCollection;
import mekanism.tools.common.registration.ToolCollection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class ToolsItems {

    private ToolsItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismTools.MODID);

    public static final ItemRegistryObject<Item> WOOD_PAXEL = registerPaxel(MekanismToolsConfig.materials.wood);
    public static final ItemRegistryObject<Item> STONE_PAXEL = registerPaxel(MekanismToolsConfig.materials.stone);
    public static final ItemRegistryObject<Item> COPPER_PAXEL = registerPaxel(MekanismToolsConfig.materials.copper);
    public static final ItemRegistryObject<Item> IRON_PAXEL = registerPaxel(MekanismToolsConfig.materials.iron);
    public static final ItemRegistryObject<Item> GOLD_PAXEL = registerPaxel(MekanismToolsConfig.materials.gold);
    public static final ItemRegistryObject<Item> DIAMOND_PAXEL = registerPaxel(MekanismToolsConfig.materials.diamond);
    public static final ItemRegistryObject<Item> NETHERITE_PAXEL = registerPaxel(MekanismToolsConfig.materials.netherite);

    public static final ArmorCollection BRONZE_ARMOR = ArmorCollection.create(ITEMS, MekanismToolsConfig.materials.bronze);
    public static final ToolCollection BRONZE_TOOLS = ToolCollection.create(ITEMS, MekanismToolsConfig.materials.bronze);

    public static final ArmorCollection LAPIS_LAZULI_ARMOR = ArmorCollection.create(ITEMS, MekanismToolsConfig.materials.lapisLazuli);
    public static final ToolCollection LAPIS_LAZULI_TOOLS = ToolCollection.create(ITEMS, MekanismToolsConfig.materials.lapisLazuli);

    public static final ArmorCollection OSMIUM_ARMOR = ArmorCollection.create(ITEMS, MekanismToolsConfig.materials.osmium);
    public static final ToolCollection OSMIUM_TOOLS = ToolCollection.create(ITEMS, MekanismToolsConfig.materials.osmium);

    public static final ArmorCollection REFINED_GLOWSTONE_ARMOR = ArmorCollection.create(ITEMS, MekanismToolsConfig.materials.refinedGlowstone);
    public static final ToolCollection REFINED_GLOWSTONE_TOOLS = ToolCollection.create(ITEMS, MekanismToolsConfig.materials.refinedGlowstone);

    public static final ArmorCollection REFINED_OBSIDIAN_ARMOR = ArmorCollection.create(ITEMS, MekanismToolsConfig.materials.refinedObsidian);
    public static final ToolCollection REFINED_OBSIDIAN_TOOLS = ToolCollection.create(ITEMS, MekanismToolsConfig.materials.refinedObsidian);

    public static final ArmorCollection STEEL_ARMOR = ArmorCollection.create(ITEMS, MekanismToolsConfig.materials.steel);
    public static final ToolCollection STEEL_TOOLS = ToolCollection.create(ITEMS, MekanismToolsConfig.materials.steel);

    private static ItemRegistryObject<Item> registerPaxel(VanillaPaxelMaterialCreator material) {
        return ITEMS.registerSimple(material.getRegistryPrefix() + "_paxel", properties -> {
            if (material.toToolMaterial() == ToolMaterial.NETHERITE) {
                properties.fireResistant();
            }
            return paxel(properties, material).component(ToolsDataComponents.DISPLAY_HP, Unit.INSTANCE);
        });
    }

    public static Item.Properties setCommonProperties(Item.Properties properties, BaseMekanismMaterial material) {
        properties.component(ToolsDataComponents.DISPLAY_HP, Unit.INSTANCE);
        if (!material.burnsInFire()) {
            return properties.fireResistant();
        }
        return properties;
    }

    public static Item.Properties paxel(Item.Properties properties, IPaxelMaterial material) {
        //TODO - 26.3: This should probably disable for the same duration as an axe
        return properties.tool(material.toToolMaterial(), ToolsTags.Blocks.MINEABLE_WITH_PAXEL, material.getPaxelDamage(), material.getPaxelAtkSpeed(), 0)
              .delayedComponent(DataComponents.BLOCK_TRANSFORMER, context -> context.getOrThrow(MekanismBlockTransformers.PAXEL))
              //Durability must go after tool to set it to the correct value, rather than one that goes off of the base tool material
              .durability(material.getPaxelDurability());
    }

    public static Stream<ItemRegistryObject<Item>> vanillaPaxels() {
        return Stream.of(WOOD_PAXEL, STONE_PAXEL, COPPER_PAXEL, IRON_PAXEL, GOLD_PAXEL, DIAMOND_PAXEL, NETHERITE_PAXEL);
    }
}