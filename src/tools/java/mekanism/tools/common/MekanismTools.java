package mekanism.tools.common;

import java.util.Random;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismModConfig;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismTools.MODID)
public class MekanismTools implements IModule {

    public static final String MODID = "mekanismtools";

    public static MekanismTools instance;

    /**
     * MekanismTools version number
     */
    public final Version versionNumber;

    public MekanismTools() {
        Mekanism.modulesLoaded.add(instance = this);
        MekanismToolsConfig.registerConfigs(ModLoadingContext.get());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);
        //Register this class to the event bus for special mob spawning (mobs with Mekanism armor/tools)
        MinecraftForge.EVENT_BUS.addListener(this::onLivingSpecialSpawn);

        ToolsItems.ITEMS.register(modEventBus);
        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer().getModInfo().getVersion());
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismTools.MODID, path);
    }

    private void onConfigLoad(ModConfig.ModConfigEvent configEvent) {
        //Note: We listen to both the initial load and the reload, so as to make sure that we fix any accidentally
        // cached values from calls before the initial loading
        ModConfig config = configEvent.getConfig();
        //Make sure it is for the same modid as us
        if (config.getModId().equals(MODID) && config instanceof MekanismModConfig) {
            ((MekanismModConfig) config).clearCache();
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Mekanism.logger.info("Loaded 'Mekanism: Tools' module.");
    }

    private void setStackIfEmpty(LivingEntity entity, EquipmentSlotType slot, ItemStack item) {
        if (entity.getItemStackFromSlot(slot).isEmpty()) {
            entity.setItemStackToSlot(slot, item);
        }
    }

    private void setEntityArmorWithChance(Random random, LivingEntity entity, IItemProvider sword, IItemProvider helmet, IItemProvider chestplate, IItemProvider leggings, IItemProvider boots) {
        if (entity instanceof ZombieEntity && random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EquipmentSlotType.MAINHAND, sword.getItemStack());
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EquipmentSlotType.HEAD, helmet.getItemStack());
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EquipmentSlotType.CHEST, chestplate.getItemStack());
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EquipmentSlotType.LEGS, leggings.getItemStack());
        }
        if (random.nextInt(100) < 50) {
            setStackIfEmpty(entity, EquipmentSlotType.FEET, boots.getItemStack());
        }
    }

    private void onLivingSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ZombieEntity || entity instanceof SkeletonEntity) {
            //Don't bother calculating random numbers unless the instanceof checks pass
            Random random = event.getWorld().getRandom();
            double chance = random.nextDouble();
            if (chance < MekanismToolsConfig.tools.armorSpawnRate.get()) {
                int armorType = random.nextInt(5);
                if (armorType == 0) {
                    setEntityArmorWithChance(random, entity, ToolsItems.REFINED_GLOWSTONE_SWORD, ToolsItems.REFINED_GLOWSTONE_HELMET, ToolsItems.REFINED_GLOWSTONE_CHESTPLATE,
                          ToolsItems.REFINED_GLOWSTONE_LEGGINGS, ToolsItems.REFINED_GLOWSTONE_BOOTS);
                } else if (armorType == 1) {
                    setEntityArmorWithChance(random, entity, ToolsItems.LAPIS_LAZULI_SWORD, ToolsItems.LAPIS_LAZULI_HELMET, ToolsItems.LAPIS_LAZULI_CHESTPLATE,
                          ToolsItems.LAPIS_LAZULI_LEGGINGS, ToolsItems.LAPIS_LAZULI_BOOTS);
                } else if (armorType == 2) {
                    setEntityArmorWithChance(random, entity, ToolsItems.REFINED_OBSIDIAN_SWORD, ToolsItems.REFINED_OBSIDIAN_HELMET, ToolsItems.REFINED_OBSIDIAN_CHESTPLATE,
                          ToolsItems.REFINED_OBSIDIAN_LEGGINGS, ToolsItems.REFINED_OBSIDIAN_BOOTS);
                } else if (armorType == 3) {
                    setEntityArmorWithChance(random, entity, ToolsItems.STEEL_SWORD, ToolsItems.STEEL_HELMET, ToolsItems.STEEL_CHESTPLATE,
                          ToolsItems.STEEL_LEGGINGS, ToolsItems.STEEL_BOOTS);
                } else if (armorType == 4) {
                    setEntityArmorWithChance(random, entity, ToolsItems.BRONZE_SWORD, ToolsItems.BRONZE_HELMET, ToolsItems.BRONZE_CHESTPLATE,
                          ToolsItems.BRONZE_LEGGINGS, ToolsItems.BRONZE_BOOTS);
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
    public void resetClient() {
    }
}