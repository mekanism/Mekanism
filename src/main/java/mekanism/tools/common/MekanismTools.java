package mekanism.tools.common;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Random;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
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
        Materials.load();
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

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        //Furnace Recipes
        addSmeltingRecipe(ToolsItem.IRON_PAXEL, new ItemStack(Items.IRON_NUGGET));
        addSmeltingRecipe(ToolsItem.GOLD_PAXEL, new ItemStack(Items.GOLD_NUGGET));
        addSmeltingRecipe(ToolsItem.OBSIDIAN_SET, new ItemStack(MekanismItems.Nugget));
        addSmeltingRecipe(ToolsItem.OSMIUM_SET, new ItemStack(MekanismItems.Nugget, 1, 1));
        addSmeltingRecipe(ToolsItem.BRONZE_SET, new ItemStack(MekanismItems.Nugget, 1, 2));
        addSmeltingRecipe(ToolsItem.GLOWSTONE_SET, new ItemStack(MekanismItems.Nugget, 1, 3));
        addSmeltingRecipe(ToolsItem.STEEL_SET, new ItemStack(MekanismItems.Nugget, 1, 4));
    }

    private static void addSmeltingRecipe(List<ToolsItem> itemSet, ItemStack nugget) {
        itemSet.forEach(toolsItem -> addSmeltingRecipe(toolsItem, nugget));
    }

    private static void addSmeltingRecipe(ToolsItem toolsItem, ItemStack nugget) {
        GameRegistry.addSmelting(toolsItem.getItemStackAnyDamage(), nugget, 0.1F);
    }

    private void setStackIfCaseAndEmpty(EntityLivingBase entity, EntityEquipmentSlot slot, ItemStack item, boolean shouldSet) {
        if (shouldSet && entity.getItemStackFromSlot(slot).isEmpty()) {
            entity.setItemStackToSlot(slot, item);
        }
    }

    private void setEntityArmorWithChance(Random random, EntityLivingBase entity, ToolsItem sword, ToolsItem helmet, ToolsItem chestplate, ToolsItem leggings, ToolsItem boots) {
        setStackIfCaseAndEmpty(entity, EntityEquipmentSlot.MAINHAND, sword.getItemStack(), entity instanceof EntityZombie && random.nextInt(100) < 50);
        setStackIfCaseAndEmpty(entity, EntityEquipmentSlot.HEAD, helmet.getItemStack(), random.nextInt(100) < 50);
        setStackIfCaseAndEmpty(entity, EntityEquipmentSlot.CHEST, chestplate.getItemStack(), random.nextInt(100) < 50);
        setStackIfCaseAndEmpty(entity, EntityEquipmentSlot.LEGS, leggings.getItemStack(), random.nextInt(100) < 50);
        setStackIfCaseAndEmpty(entity, EntityEquipmentSlot.FEET, boots.getItemStack(), random.nextInt(100) < 50);
    }

    @SubscribeEvent
    public void onLivingSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity instanceof EntityZombie || entity instanceof EntitySkeleton) {
            //Don't bother calculating random numbers unless the instanceof checks pass
            Random random = event.getWorld().rand;
            double chance = random.nextDouble();
            if (chance < MekanismConfig.current().tools.armorSpawnRate.val()) {
                int armorType = random.nextInt(4);
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