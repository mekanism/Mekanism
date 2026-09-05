package mekanism.tools.common.registries;

import java.util.stream.Stream;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.material.VanillaPaxelMaterialCreator;
import mekanism.tools.common.registration.ArmorCollection;
import mekanism.tools.common.registration.ToolCollection;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class ToolsItems {

    private ToolsItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismTools.MODID);

    public static final ItemRegistryObject<ItemMekanismPaxel> WOOD_PAXEL = registerPaxel(MekanismToolsConfig.materials.wood);
    public static final ItemRegistryObject<ItemMekanismPaxel> STONE_PAXEL = registerPaxel(MekanismToolsConfig.materials.stone);
    public static final ItemRegistryObject<ItemMekanismPaxel> COPPER_PAXEL = registerPaxel(MekanismToolsConfig.materials.copper);
    public static final ItemRegistryObject<ItemMekanismPaxel> IRON_PAXEL = registerPaxel(MekanismToolsConfig.materials.iron);
    public static final ItemRegistryObject<ItemMekanismPaxel> GOLD_PAXEL = registerPaxel(MekanismToolsConfig.materials.gold);
    public static final ItemRegistryObject<ItemMekanismPaxel> DIAMOND_PAXEL = registerPaxel(MekanismToolsConfig.materials.diamond);
    public static final ItemRegistryObject<ItemMekanismPaxel> NETHERITE_PAXEL = registerPaxel(MekanismToolsConfig.materials.netherite);

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

    private static ItemRegistryObject<ItemMekanismPaxel> registerPaxel(VanillaPaxelMaterialCreator material) {
        return ITEMS.registerItem(material.getRegistryPrefix() + "_paxel", properties -> {
            if (material.toToolMaterial() == ToolMaterial.NETHERITE) {
                properties.fireResistant();
            }
            return new ItemMekanismPaxel(material, properties.component(ToolsDataComponents.DISPLAY_HP, Unit.INSTANCE));
        });
    }

    public static Item.Properties setCommonProperties(Item.Properties properties, BaseMekanismMaterial material) {
        properties.component(ToolsDataComponents.DISPLAY_HP, Unit.INSTANCE);
        if (!material.burnsInFire()) {
            return properties.fireResistant();
        }
        return properties;
    }

    public static Stream<ItemRegistryObject<ItemMekanismPaxel>> vanillaPaxels() {
        return Stream.of(WOOD_PAXEL, STONE_PAXEL, COPPER_PAXEL, IRON_PAXEL, GOLD_PAXEL, DIAMOND_PAXEL, NETHERITE_PAXEL);
    }
}