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
        ToolsItem.registerItems(event.getRegistry());
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
        addMaterials();
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

    public void addMaterials() {
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
        addSmeltingRecipe(ToolsItem.IRON_PAXEL, new ItemStack(Items.IRON_NUGGET));
        addSmeltingRecipe(ToolsItem.GOLD_PAXEL, new ItemStack(Items.GOLD_NUGGET));

        addSmeltingRecipe(ToolsItem.GLOWSTONE_PAXEL, new ItemStack(MekanismItems.Nugget, 1, 3));
        addSmeltingRecipe(ToolsItem.GLOWSTONE_PICKAXE, new ItemStack(MekanismItems.Nugget, 1, 3));
        addSmeltingRecipe(ToolsItem.GLOWSTONE_AXE, new ItemStack(MekanismItems.Nugget, 1, 3));
        addSmeltingRecipe(ToolsItem.GLOWSTONE_SHOVEL, new ItemStack(MekanismItems.Nugget, 1, 3));
        addSmeltingRecipe(ToolsItem.GLOWSTONE_HOE, new ItemStack(MekanismItems.Nugget, 1, 3));
        addSmeltingRecipe(ToolsItem.GLOWSTONE_SWORD, new ItemStack(MekanismItems.Nugget, 1, 3));
        addSmeltingRecipe(ToolsItem.GLOWSTONE_HELMET, new ItemStack(MekanismItems.Nugget, 1, 3));
        addSmeltingRecipe(ToolsItem.GLOWSTONE_CHESTPLATE, new ItemStack(MekanismItems.Nugget, 1, 3));
        addSmeltingRecipe(ToolsItem.GLOWSTONE_LEGGINGS, new ItemStack(MekanismItems.Nugget, 1, 3));
        addSmeltingRecipe(ToolsItem.GLOWSTONE_BOOTS, new ItemStack(MekanismItems.Nugget, 1, 3));

        addSmeltingRecipe(ToolsItem.BRONZE_PAXEL, new ItemStack(MekanismItems.Nugget, 1, 2));
        addSmeltingRecipe(ToolsItem.BRONZE_PICKAXE, new ItemStack(MekanismItems.Nugget, 1, 2));
        addSmeltingRecipe(ToolsItem.BRONZE_AXE, new ItemStack(MekanismItems.Nugget, 1, 2));
        addSmeltingRecipe(ToolsItem.BRONZE_SHOVEL, new ItemStack(MekanismItems.Nugget, 1, 2));
        addSmeltingRecipe(ToolsItem.BRONZE_HOE, new ItemStack(MekanismItems.Nugget, 1, 2));
        addSmeltingRecipe(ToolsItem.BRONZE_SWORD, new ItemStack(MekanismItems.Nugget, 1, 2));
        addSmeltingRecipe(ToolsItem.BRONZE_HELMET, new ItemStack(MekanismItems.Nugget, 1, 2));
        addSmeltingRecipe(ToolsItem.BRONZE_CHESTPLATE, new ItemStack(MekanismItems.Nugget, 1, 2));
        addSmeltingRecipe(ToolsItem.BRONZE_LEGGINGS, new ItemStack(MekanismItems.Nugget, 1, 2));
        addSmeltingRecipe(ToolsItem.BRONZE_BOOTS, new ItemStack(MekanismItems.Nugget, 1, 2));

        addSmeltingRecipe(ToolsItem.OSMIUM_PAXEL, new ItemStack(MekanismItems.Nugget, 1, 1));
        addSmeltingRecipe(ToolsItem.OSMIUM_PICKAXE, new ItemStack(MekanismItems.Nugget, 1, 1));
        addSmeltingRecipe(ToolsItem.OSMIUM_AXE, new ItemStack(MekanismItems.Nugget, 1, 1));
        addSmeltingRecipe(ToolsItem.OSMIUM_SHOVEL, new ItemStack(MekanismItems.Nugget, 1, 1));
        addSmeltingRecipe(ToolsItem.OSMIUM_HOE, new ItemStack(MekanismItems.Nugget, 1, 1));
        addSmeltingRecipe(ToolsItem.OSMIUM_SWORD, new ItemStack(MekanismItems.Nugget, 1, 1));
        addSmeltingRecipe(ToolsItem.OSMIUM_HELMET, new ItemStack(MekanismItems.Nugget, 1, 1));
        addSmeltingRecipe(ToolsItem.OSMIUM_CHESTPLATE, new ItemStack(MekanismItems.Nugget, 1, 1));
        addSmeltingRecipe(ToolsItem.OSMIUM_LEGGINGS, new ItemStack(MekanismItems.Nugget, 1, 1));
        addSmeltingRecipe(ToolsItem.OSMIUM_BOOTS, new ItemStack(MekanismItems.Nugget, 1, 1));

        addSmeltingRecipe(ToolsItem.OBSIDIAN_PAXEL, new ItemStack(MekanismItems.Nugget, 1, 0));
        addSmeltingRecipe(ToolsItem.OBSIDIAN_PICKAXE, new ItemStack(MekanismItems.Nugget, 1, 0));
        addSmeltingRecipe(ToolsItem.OBSIDIAN_AXE, new ItemStack(MekanismItems.Nugget, 1, 0));
        addSmeltingRecipe(ToolsItem.OBSIDIAN_SHOVEL, new ItemStack(MekanismItems.Nugget, 1, 0));
        addSmeltingRecipe(ToolsItem.OBSIDIAN_HOE, new ItemStack(MekanismItems.Nugget, 1, 0));
        addSmeltingRecipe(ToolsItem.OBSIDIAN_SWORD, new ItemStack(MekanismItems.Nugget, 1, 0));
        addSmeltingRecipe(ToolsItem.OBSIDIAN_HELMET, new ItemStack(MekanismItems.Nugget, 1, 0));
        addSmeltingRecipe(ToolsItem.OBSIDIAN_CHESTPLATE, new ItemStack(MekanismItems.Nugget, 1, 0));
        addSmeltingRecipe(ToolsItem.OBSIDIAN_LEGGINGS, new ItemStack(MekanismItems.Nugget, 1, 0));
        addSmeltingRecipe(ToolsItem.OBSIDIAN_BOOTS, new ItemStack(MekanismItems.Nugget, 1, 0));

        addSmeltingRecipe(ToolsItem.STEEL_PAXEL, new ItemStack(MekanismItems.Nugget, 1, 4));
        addSmeltingRecipe(ToolsItem.STEEL_PICKAXE, new ItemStack(MekanismItems.Nugget, 1, 4));
        addSmeltingRecipe(ToolsItem.STEEL_AXE, new ItemStack(MekanismItems.Nugget, 1, 4));
        addSmeltingRecipe(ToolsItem.STEEL_SHOVEL, new ItemStack(MekanismItems.Nugget, 1, 4));
        addSmeltingRecipe(ToolsItem.STEEL_HOE, new ItemStack(MekanismItems.Nugget, 1, 4));
        addSmeltingRecipe(ToolsItem.STEEL_SWORD, new ItemStack(MekanismItems.Nugget, 1, 4));
        addSmeltingRecipe(ToolsItem.STEEL_HELMET, new ItemStack(MekanismItems.Nugget, 1, 4));
        addSmeltingRecipe(ToolsItem.STEEL_CHESTPLATE, new ItemStack(MekanismItems.Nugget, 1, 4));
        addSmeltingRecipe(ToolsItem.STEEL_LEGGINGS, new ItemStack(MekanismItems.Nugget, 1, 4));
        addSmeltingRecipe(ToolsItem.STEEL_BOOTS, new ItemStack(MekanismItems.Nugget, 1, 4));
    }

    private static void addSmeltingRecipe(ToolsItem item, ItemStack nugget) {
        GameRegistry.addSmelting(item.getItemStack(), nugget, 0.1F);
    }

    private void setStackIfEmpty(EntityLivingBase entity, EntityEquipmentSlot slot, ItemStack item) {
        if (entity.getItemStackFromSlot(slot).isEmpty()) {
            entity.setItemStackToSlot(slot, item);
        }
    }

    private void setEntityArmorWithChance(Random random, EntityLivingBase entity, ToolsItem sword, ToolsItem helmet, ToolsItem chestplate, ToolsItem leggings, ToolsItem boots) {
        if (entity instanceof EntityZombie && random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EntityEquipmentSlot.MAINHAND, sword.getItemStack());
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EntityEquipmentSlot.HEAD, helmet.getItemStack());
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EntityEquipmentSlot.CHEST, chestplate.getItemStack());
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EntityEquipmentSlot.LEGS, leggings.getItemStack());
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EntityEquipmentSlot.FEET, boots.getItemStack());
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
                    setEntityArmorWithChance(random, entity, ToolsItem.GLOWSTONE_SWORD, ToolsItem.GLOWSTONE_HELMET, ToolsItem.GLOWSTONE_CHESTPLATE,
                          ToolsItem.GLOWSTONE_LEGGINGS, ToolsItem.GLOWSTONE_BOOTS);
                } else if (armorType == 1) {
                    setEntityArmorWithChance(random, entity, ToolsItem.LAPIS_LAZULI_SWORD, ToolsItem.LAPIS_LAZULI_HELMET, ToolsItem.LAPIS_LAZULI_CHESTPLATE,
                          ToolsItem.LAPIS_LAZULI_LEGGINGS, ToolsItem.LAPIS_LAZULI_BOOTS);
                } else if (armorType == 2) {
                    setEntityArmorWithChance(random, entity, ToolsItem.OBSIDIAN_SWORD, ToolsItem.OBSIDIAN_HELMET, ToolsItem.OBSIDIAN_CHESTPLATE,
                          ToolsItem.OBSIDIAN_LEGGINGS, ToolsItem.OBSIDIAN_BOOTS);
                } else if (armorType == 3) {
                    setEntityArmorWithChance(random, entity, ToolsItem.STEEL_SWORD, ToolsItem.STEEL_HELMET, ToolsItem.STEEL_CHESTPLATE,
                          ToolsItem.STEEL_LEGGINGS, ToolsItem.STEEL_BOOTS);
                } else if (armorType == 4) {
                    setEntityArmorWithChance(random, entity, ToolsItem.BRONZE_SWORD, ToolsItem.BRONZE_HELMET, ToolsItem.BRONZE_CHESTPLATE,
                          ToolsItem.BRONZE_LEGGINGS, ToolsItem.BRONZE_BOOTS);
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