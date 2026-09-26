package mekanism.tools.common.registries;

import java.util.stream.Stream;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;

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
        return ITEMS.registerItem(material.getRegistryPrefix() + "_paxel", properties -> {
            if (material.isFireResistant()) {
                properties = properties.fireResistant();
            }
            return paxel(properties.component(ToolsDataComponents.DISPLAY_HP, Unit.INSTANCE), material);
        });
    }

    public static Item.Properties setCommonProperties(Item.Properties properties, BaseMekanismMaterial material) {
        properties.component(ToolsDataComponents.DISPLAY_HP, Unit.INSTANCE);
        if (!material.burnsInFire()) {
            return properties.fireResistant();
        }
        return properties;
    }

    public static Item paxel(Item.Properties properties, IPaxelMaterial material) {
        return new PaxelItem(properties
              .tool(material.toPaxelToolMaterial(), ToolsTags.Blocks.MINEABLE_WITH_PAXEL, material.paxelDamage(), material.paxelAtkSpeed(), material.paxelDisableBlockingSeconds())
              .delayedComponent(DataComponents.BLOCK_TRANSFORMER, context -> context.getOrThrow(BlockTransformers.AXE))
        );
    }

    public static Stream<ItemRegistryObject<Item>> vanillaPaxels() {
        return Stream.of(WOOD_PAXEL, STONE_PAXEL, COPPER_PAXEL, IRON_PAXEL, GOLD_PAXEL, DIAMOND_PAXEL, NETHERITE_PAXEL);
    }

    private static class PaxelItem extends Item {

        public PaxelItem(Item.Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            InteractionResult result = super.useOn(context);
            if (result == InteractionResult.PASS) {
                //If using it as an axe failed, try to use it as a shovel
                result = context.getLevel().registryAccess().getOrThrow(BlockTransformers.SHOVEL).value().transformBlock(context);
            }
            return result;
        }
    }
}