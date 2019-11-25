package mekanism.tools.common;

import java.util.Random;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.tools.common.config.MekanismToolsConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
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
        //modEventBus.addListener((FMLModIdMappingEvent event) -> ToolsItem.remapItems());
        modEventBus.addListener(this::onLivingSpecialSpawn);

        ToolsItem.ITEMS.register(modEventBus);

        //Register this class to the event bus for special mob spawning (mobs with Mekanism armor/tools)
        //TODO: Is the modEventBus stuff above used instead of this
        //MinecraftForge.EVENT_BUS.register(this);

        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer().getModInfo().getVersion());

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
                    setEntityArmorWithChance(random, entity, ToolsItem.REFINED_GLOWSTONE_SWORD, ToolsItem.REFINED_GLOWSTONE_HELMET, ToolsItem.REFINED_GLOWSTONE_CHESTPLATE,
                          ToolsItem.REFINED_GLOWSTONE_LEGGINGS, ToolsItem.REFINED_GLOWSTONE_BOOTS);
                } else if (armorType == 1) {
                    setEntityArmorWithChance(random, entity, ToolsItem.LAPIS_LAZULI_SWORD, ToolsItem.LAPIS_LAZULI_HELMET, ToolsItem.LAPIS_LAZULI_CHESTPLATE,
                          ToolsItem.LAPIS_LAZULI_LEGGINGS, ToolsItem.LAPIS_LAZULI_BOOTS);
                } else if (armorType == 2) {
                    setEntityArmorWithChance(random, entity, ToolsItem.REFINED_OBSIDIAN_SWORD, ToolsItem.REFINED_OBSIDIAN_HELMET, ToolsItem.REFINED_OBSIDIAN_CHESTPLATE,
                          ToolsItem.REFINED_OBSIDIAN_LEGGINGS, ToolsItem.REFINED_OBSIDIAN_BOOTS);
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

    /*@Override
    public void writeConfig(PacketBuffer dataStream, MekanismConfig config) {
        config.tools.write(dataStream);
    }

    @Override
    public void readConfig(PacketBuffer dataStream, MekanismConfig destConfig) {
        destConfig.tools.read(dataStream);
    }*/

    @Override
    public void resetClient() {
    }

    //TODO
    /*private void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(MekanismTools.MODID) || event.getModID().equalsIgnoreCase(Mekanism.MODID)) {
            proxy.loadConfiguration();
        }
    }*/
}