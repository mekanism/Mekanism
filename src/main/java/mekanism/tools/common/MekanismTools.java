package mekanism.tools.common;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.ToolsConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = MekanismTools.MODID, useMetadata = true, guiFactory = "mekanism.tools.client.gui.ToolsGuiFactory")
@Mod.EventBusSubscriber()
public class MekanismTools implements IModule {

    public static final String MODID = "mekanismtools";

    @SidedProxy(clientSide = "mekanism.tools.client.ToolsClientProxy", serverSide = "mekanism.tools.common.ToolsCommonProxy")
    public static ToolsCommonProxy proxy;

    @Instance(MekanismTools.MODID)
    public static MekanismTools instance;

    /**
     * MekanismTools version number
     */
    public static Version versionNumber = new Version(999, 999, 999);

    //Enums: Tools
    public static ToolMaterial toolOBSIDIAN;
    public static ToolMaterial toolOBSIDIAN2;
    public static ToolMaterial toolLAZULI;
    public static ToolMaterial toolLAZULI2;
    public static ToolMaterial toolOSMIUM;
    public static ToolMaterial toolOSMIUM2;
    public static ToolMaterial toolBRONZE;
    public static ToolMaterial toolBRONZE2;
    public static ToolMaterial toolGLOWSTONE;
    public static ToolMaterial toolGLOWSTONE2;
    public static ToolMaterial toolSTEEL;
    public static ToolMaterial toolSTEEL2;

    //Enums: Armor
    public static ArmorMaterial armorOBSIDIAN;
    public static ArmorMaterial armorLAZULI;
    public static ArmorMaterial armorOSMIUM;
    public static ArmorMaterial armorBRONZE;
    public static ArmorMaterial armorGLOWSTONE;
    public static ArmorMaterial armorSTEEL;

    //Can't use EnumMap as the custom ToolMaterial's do not exist yet
    public static Map<ToolMaterial, Float> AXE_DAMAGE = new HashMap<>();
    public static Map<ToolMaterial, Float> AXE_SPEED = new HashMap<>();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        // Register items and itemBlocks
        ToolsItems.registerItems(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // Register models
        proxy.registerItemRenders();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //Load the config
        proxy.loadConfiguration();
        addItems();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        //Add this module to the core list
        Mekanism.modulesLoaded.add(this);

        //Register this class to the event bus for special mob spawning (mobs with Mekanism armor/tools)
        MinecraftForge.EVENT_BUS.register(this);

        //Finalization
        Mekanism.logger.info("Loaded 'Mekanism: Tools' module.");
    }

    public void addItems() {
        //Tools
        toolOBSIDIAN = getToolMaterial("OBSIDIAN", MekanismConfig.current().tools.toolOBSIDIAN);
        toolOBSIDIAN2 = getToolMaterial("OBSIDIAN2", MekanismConfig.current().tools.toolOBSIDIAN2);
        toolLAZULI = getToolMaterial("LAZULI", MekanismConfig.current().tools.toolLAZULI);
        toolLAZULI2 = getToolMaterial("LAZULI2", MekanismConfig.current().tools.toolLAZULI2);
        toolOSMIUM = getToolMaterial("OSMIUM", MekanismConfig.current().tools.toolOSMIUM);
        toolOSMIUM2 = getToolMaterial("OSMIUM2", MekanismConfig.current().tools.toolOSMIUM2);
        toolBRONZE = getToolMaterial("BRONZE", MekanismConfig.current().tools.toolBRONZE);
        toolBRONZE2 = getToolMaterial("BRONZE2", MekanismConfig.current().tools.toolBRONZE2);
        toolGLOWSTONE = getToolMaterial("GLOWSTONE", MekanismConfig.current().tools.toolGLOWSTONE);
        toolGLOWSTONE2 = getToolMaterial("GLOWSTONE2", MekanismConfig.current().tools.toolGLOWSTONE2);
        toolSTEEL = getToolMaterial("STEEL", MekanismConfig.current().tools.toolSTEEL);
        toolSTEEL2 = getToolMaterial("STEEL2", MekanismConfig.current().tools.toolSTEEL2);

        setAxeSpeedDamage(toolOBSIDIAN, MekanismConfig.current().tools.toolOBSIDIAN);
        setAxeSpeedDamage(toolLAZULI, MekanismConfig.current().tools.toolLAZULI);
        setAxeSpeedDamage(toolOSMIUM, MekanismConfig.current().tools.toolOSMIUM);
        setAxeSpeedDamage(toolBRONZE, MekanismConfig.current().tools.toolBRONZE);
        setAxeSpeedDamage(toolGLOWSTONE, MekanismConfig.current().tools.toolGLOWSTONE);
        setAxeSpeedDamage(toolSTEEL, MekanismConfig.current().tools.toolSTEEL);

        //Armors
        armorOBSIDIAN = getArmorMaterial("OBSIDIAN", MekanismConfig.current().tools.armorOBSIDIAN, SoundEvents.ITEM_ARMOR_EQUIP_IRON);
        armorLAZULI = getArmorMaterial("LAZULI", MekanismConfig.current().tools.armorLAZULI, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND);
        armorOSMIUM = getArmorMaterial("OSMIUM", MekanismConfig.current().tools.armorOSMIUM, SoundEvents.ITEM_ARMOR_EQUIP_IRON);
        armorBRONZE = getArmorMaterial("BRONZE", MekanismConfig.current().tools.armorBRONZE, SoundEvents.ITEM_ARMOR_EQUIP_IRON);
        armorGLOWSTONE = getArmorMaterial("GLOWSTONE", MekanismConfig.current().tools.armorGLOWSTONE, SoundEvents.ITEM_ARMOR_EQUIP_IRON);
        armorSTEEL = getArmorMaterial("STEEL", MekanismConfig.current().tools.armorSTEEL, SoundEvents.ITEM_ARMOR_EQUIP_IRON);

        //Set the repair items for each armor
        setRepairItems(toolOBSIDIAN, toolOBSIDIAN2, armorOBSIDIAN, new ItemStack(MekanismItems.Ingot, 1, 0));
        setRepairItems(toolOSMIUM, toolOSMIUM2, armorOSMIUM, new ItemStack(MekanismItems.Ingot, 1, 1));
        setRepairItems(toolBRONZE, toolBRONZE2, armorBRONZE, new ItemStack(MekanismItems.Ingot, 1, 2));
        setRepairItems(toolGLOWSTONE, toolGLOWSTONE2, armorGLOWSTONE, new ItemStack(MekanismItems.Ingot, 1, 3));
        setRepairItems(toolSTEEL, toolSTEEL2, armorSTEEL, new ItemStack(MekanismItems.Ingot, 1, 4));
        setRepairItems(toolLAZULI, toolLAZULI2, armorLAZULI, new ItemStack(Items.DYE, 1, 4));

        ToolsItems.initializeItems();
    }

    private void setRepairItems(ToolMaterial material, ToolMaterial material2, ArmorMaterial armorMaterial, ItemStack repairStack) {
        material.setRepairItem(repairStack);
        material2.setRepairItem(repairStack);
        armorMaterial.setRepairItem(repairStack);
    }

    private void setAxeSpeedDamage(ToolMaterial tool, ToolsConfig.ToolBalance tool2) {
        AXE_DAMAGE.put(tool, tool2.axeAttackDamage.val());
        AXE_SPEED.put(tool, tool2.axeAttackSpeed.val());
    }

    private ToolMaterial getToolMaterial(String enumName, ToolsConfig.ToolBalance config) {
        return EnumHelper.addToolMaterial(enumName, config.harvestLevel.val(), config.maxUses.val(), config.efficiency.val(), config.damage.val(), config.enchantability.val());
    }

    private ArmorMaterial getArmorMaterial(String enumName, ToolsConfig.ArmorBalance settings, SoundEvent equipSoundEvent) {
        return EnumHelper.addArmorMaterial(enumName, "TODO", settings.durability.val(), new int[]{
              settings.feetProtection.val(),
              settings.legsProtection.val(),
              settings.chestProtection.val(),
              settings.headProtection.val(),
              }, settings.enchantability.val(), equipSoundEvent, settings.toughness.val());
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        //Furnace Recipes
        GameRegistry.addSmelting(ToolsItems.IronPaxel, new ItemStack(Items.IRON_NUGGET), 0.1F);
        GameRegistry.addSmelting(ToolsItems.GoldPaxel, new ItemStack(Items.GOLD_NUGGET), 0.1F);

        GameRegistry.addSmelting(ToolsItems.GlowstonePaxel, new ItemStack(MekanismItems.Nugget, 1, 3), 0.1F);
        GameRegistry.addSmelting(ToolsItems.GlowstonePickaxe, new ItemStack(MekanismItems.Nugget, 1, 3), 0.1F);
        GameRegistry.addSmelting(ToolsItems.GlowstoneAxe, new ItemStack(MekanismItems.Nugget, 1, 3), 0.1F);
        GameRegistry.addSmelting(ToolsItems.GlowstoneShovel, new ItemStack(MekanismItems.Nugget, 1, 3), 0.1F);
        GameRegistry.addSmelting(ToolsItems.GlowstoneHoe, new ItemStack(MekanismItems.Nugget, 1, 3), 0.1F);
        GameRegistry.addSmelting(ToolsItems.GlowstoneSword, new ItemStack(MekanismItems.Nugget, 1, 3), 0.1F);
        GameRegistry.addSmelting(ToolsItems.GlowstoneHelmet, new ItemStack(MekanismItems.Nugget, 1, 3), 0.1F);
        GameRegistry.addSmelting(ToolsItems.GlowstoneChestplate, new ItemStack(MekanismItems.Nugget, 1, 3), 0.1F);
        GameRegistry.addSmelting(ToolsItems.GlowstoneLeggings, new ItemStack(MekanismItems.Nugget, 1, 3), 0.1F);
        GameRegistry.addSmelting(ToolsItems.GlowstoneBoots, new ItemStack(MekanismItems.Nugget, 1, 3), 0.1F);

        GameRegistry.addSmelting(ToolsItems.BronzePaxel, new ItemStack(MekanismItems.Nugget, 1, 2), 0.1F);
        GameRegistry.addSmelting(ToolsItems.BronzePickaxe, new ItemStack(MekanismItems.Nugget, 1, 2), 0.1F);
        GameRegistry.addSmelting(ToolsItems.BronzeAxe, new ItemStack(MekanismItems.Nugget, 1, 2), 0.1F);
        GameRegistry.addSmelting(ToolsItems.BronzeShovel, new ItemStack(MekanismItems.Nugget, 1, 2), 0.1F);
        GameRegistry.addSmelting(ToolsItems.BronzeHoe, new ItemStack(MekanismItems.Nugget, 1, 2), 0.1F);
        GameRegistry.addSmelting(ToolsItems.BronzeSword, new ItemStack(MekanismItems.Nugget, 1, 2), 0.1F);
        GameRegistry.addSmelting(ToolsItems.BronzeHelmet, new ItemStack(MekanismItems.Nugget, 1, 2), 0.1F);
        GameRegistry.addSmelting(ToolsItems.BronzeChestplate, new ItemStack(MekanismItems.Nugget, 1, 2), 0.1F);
        GameRegistry.addSmelting(ToolsItems.BronzeLeggings, new ItemStack(MekanismItems.Nugget, 1, 2), 0.1F);
        GameRegistry.addSmelting(ToolsItems.BronzeBoots, new ItemStack(MekanismItems.Nugget, 1, 2), 0.1F);

        GameRegistry.addSmelting(ToolsItems.OsmiumPaxel, new ItemStack(MekanismItems.Nugget, 1, 1), 0.1F);
        GameRegistry.addSmelting(ToolsItems.OsmiumPickaxe, new ItemStack(MekanismItems.Nugget, 1, 1), 0.1F);
        GameRegistry.addSmelting(ToolsItems.OsmiumAxe, new ItemStack(MekanismItems.Nugget, 1, 1), 0.1F);
        GameRegistry.addSmelting(ToolsItems.OsmiumShovel, new ItemStack(MekanismItems.Nugget, 1, 1), 0.1F);
        GameRegistry.addSmelting(ToolsItems.OsmiumHoe, new ItemStack(MekanismItems.Nugget, 1, 1), 0.1F);
        GameRegistry.addSmelting(ToolsItems.OsmiumSword, new ItemStack(MekanismItems.Nugget, 1, 1), 0.1F);
        GameRegistry.addSmelting(ToolsItems.OsmiumHelmet, new ItemStack(MekanismItems.Nugget, 1, 1), 0.1F);
        GameRegistry.addSmelting(ToolsItems.OsmiumChestplate, new ItemStack(MekanismItems.Nugget, 1, 1), 0.1F);
        GameRegistry.addSmelting(ToolsItems.OsmiumLeggings, new ItemStack(MekanismItems.Nugget, 1, 1), 0.1F);
        GameRegistry.addSmelting(ToolsItems.OsmiumBoots, new ItemStack(MekanismItems.Nugget, 1, 1), 0.1F);

        GameRegistry.addSmelting(ToolsItems.ObsidianPaxel, new ItemStack(MekanismItems.Nugget, 1, 0), 0.1F);
        GameRegistry.addSmelting(ToolsItems.ObsidianPickaxe, new ItemStack(MekanismItems.Nugget, 1, 0), 0.1F);
        GameRegistry.addSmelting(ToolsItems.ObsidianAxe, new ItemStack(MekanismItems.Nugget, 1, 0), 0.1F);
        GameRegistry.addSmelting(ToolsItems.ObsidianShovel, new ItemStack(MekanismItems.Nugget, 1, 0), 0.1F);
        GameRegistry.addSmelting(ToolsItems.ObsidianHoe, new ItemStack(MekanismItems.Nugget, 1, 0), 0.1F);
        GameRegistry.addSmelting(ToolsItems.ObsidianSword, new ItemStack(MekanismItems.Nugget, 1, 0), 0.1F);
        GameRegistry.addSmelting(ToolsItems.ObsidianHelmet, new ItemStack(MekanismItems.Nugget, 1, 0), 0.1F);
        GameRegistry.addSmelting(ToolsItems.ObsidianChestplate, new ItemStack(MekanismItems.Nugget, 1, 0), 0.1F);
        GameRegistry.addSmelting(ToolsItems.ObsidianLeggings, new ItemStack(MekanismItems.Nugget, 1, 0), 0.1F);
        GameRegistry.addSmelting(ToolsItems.ObsidianBoots, new ItemStack(MekanismItems.Nugget, 1, 0), 0.1F);

        GameRegistry.addSmelting(ToolsItems.SteelPaxel, new ItemStack(MekanismItems.Nugget, 1, 4), 0.1F);
        GameRegistry.addSmelting(ToolsItems.SteelPickaxe, new ItemStack(MekanismItems.Nugget, 1, 4), 0.1F);
        GameRegistry.addSmelting(ToolsItems.SteelAxe, new ItemStack(MekanismItems.Nugget, 1, 4), 0.1F);
        GameRegistry.addSmelting(ToolsItems.SteelShovel, new ItemStack(MekanismItems.Nugget, 1, 4), 0.1F);
        GameRegistry.addSmelting(ToolsItems.SteelHoe, new ItemStack(MekanismItems.Nugget, 1, 4), 0.1F);
        GameRegistry.addSmelting(ToolsItems.SteelSword, new ItemStack(MekanismItems.Nugget, 1, 4), 0.1F);
        GameRegistry.addSmelting(ToolsItems.SteelHelmet, new ItemStack(MekanismItems.Nugget, 1, 4), 0.1F);
        GameRegistry.addSmelting(ToolsItems.SteelChestplate, new ItemStack(MekanismItems.Nugget, 1, 4), 0.1F);
        GameRegistry.addSmelting(ToolsItems.SteelLeggings, new ItemStack(MekanismItems.Nugget, 1, 4), 0.1F);
        GameRegistry.addSmelting(ToolsItems.SteelBoots, new ItemStack(MekanismItems.Nugget, 1, 4), 0.1F);
    }

    private void setStackIfEmpty(EntityLivingBase entity, EntityEquipmentSlot slot, ItemStack item) {
        if (entity.getItemStackFromSlot(slot).isEmpty()) {
            entity.setItemStackToSlot(slot, item);
        }
    }

    private void setEntityArmorWithChance(Random random, EntityLivingBase entity, Item sword, Item helmet, Item chestplate, Item leggings, Item boots) {
        if (entity instanceof EntityZombie && random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EntityEquipmentSlot.MAINHAND, new ItemStack(sword));
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EntityEquipmentSlot.HEAD, new ItemStack(helmet));
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EntityEquipmentSlot.CHEST, new ItemStack(chestplate));
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EntityEquipmentSlot.LEGS, new ItemStack(leggings));
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EntityEquipmentSlot.FEET, new ItemStack(boots));
        }
    }

    @SubscribeEvent
    public void onLivingSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        Random random = event.getWorld().rand;
        double chance = random.nextDouble();

        if (chance < MekanismConfig.current().tools.armorSpawnRate.val()) {
            int armorType = random.nextInt(4);

            EntityLivingBase entity = event.getEntityLiving();

            if (entity instanceof EntityZombie || entity instanceof EntitySkeleton) {
                if (armorType == 0) {
                    setEntityArmorWithChance(random, entity, ToolsItems.GlowstoneSword, ToolsItems.GlowstoneHelmet, ToolsItems.GlowstoneChestplate,
                          ToolsItems.GlowstoneLeggings, ToolsItems.GlowstoneBoots);
                } else if (armorType == 1) {
                    setEntityArmorWithChance(random, entity, ToolsItems.LazuliSword, ToolsItems.LazuliHelmet, ToolsItems.LazuliChestplate,
                          ToolsItems.LazuliLeggings, ToolsItems.LazuliBoots);
                } else if (armorType == 2) {
                    setEntityArmorWithChance(random, entity, ToolsItems.OsmiumSword, ToolsItems.OsmiumHelmet, ToolsItems.OsmiumChestplate,
                          ToolsItems.OsmiumLeggings, ToolsItems.OsmiumBoots);
                } else if (armorType == 3) {
                    setEntityArmorWithChance(random, entity, ToolsItems.SteelSword, ToolsItems.SteelHelmet, ToolsItems.SteelChestplate,
                          ToolsItems.SteelLeggings, ToolsItems.SteelBoots);
                } else if (armorType == 4) {
                    setEntityArmorWithChance(random, entity, ToolsItems.BronzeSword, ToolsItems.BronzeHelmet, ToolsItems.BronzeChestplate,
                          ToolsItems.BronzeLeggings, ToolsItems.BronzeBoots);
                }
            }
        }
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Tools";
    }

    @Override
    public void writeConfig(ByteBuf dataStream, MekanismConfig config) {
        config.tools.write(dataStream);
    }

    @Override
    public void readConfig(ByteBuf dataStream, MekanismConfig destConfig) {
        destConfig.tools.read(dataStream);
    }

    @Override
    public void resetClient() {
    }

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(MekanismTools.MODID) || event.getModID().equalsIgnoreCase(Mekanism.MODID)) {
            proxy.loadConfiguration();
        }
    }
}