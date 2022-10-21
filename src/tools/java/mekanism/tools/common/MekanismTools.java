package mekanism.tools.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.lib.Version;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.config.ToolsConfig.ArmorSpawnChanceConfig;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.registries.ToolsItems;
import mekanism.tools.common.registries.ToolsRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismTools.MODID)
public class MekanismTools implements IModModule {

    public static final String MODID = "mekanismtools";

    public static MekanismTools instance;

    /**
     * MekanismTools version number
     */
    public final Version versionNumber;

    public MekanismTools() {
        Mekanism.addModule(instance = this);
        MekanismToolsConfig.registerConfigs(ModLoadingContext.get());
        //Register the listener for special mob spawning (mobs with Mekanism armor/tools)
        MinecraftForge.EVENT_BUS.addListener(this::onLivingSpecialSpawn);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);
        ToolsItems.ITEMS.register(modEventBus);
        ToolsRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer());
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismTools.MODID, path);
    }

    private void onConfigLoad(ModConfigEvent configEvent) {
        //Note: We listen to both the initial load and the reload, to make sure that we fix any accidentally
        // cached values from calls before the initial loading
        ModConfig config = configEvent.getConfig();
        //Make sure it is for the same modid as us
        if (config.getModId().equals(MODID) && config instanceof MekanismModConfig mekConfig) {
            mekConfig.clearCache();
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            //Ensure our tags are all initialized
            ToolsTags.init();
        });
        registerTiers(MekanismToolsConfig.tools.bronze, MekanismToolsConfig.tools.lapisLazuli, MekanismToolsConfig.tools.osmium, MekanismToolsConfig.tools.steel,
              MekanismToolsConfig.tools.refinedGlowstone, MekanismToolsConfig.tools.refinedObsidian);
        Mekanism.logger.info("Loaded 'Mekanism: Tools' module.");
    }

    @SuppressWarnings("deprecation")
    private void registerTiers(BaseMekanismMaterial... tiers) {
        Multimap<Integer, Tier> vanillaTiers = HashMultimap.create();
        for (Tiers vanillaTier : Tiers.values()) {
            vanillaTiers.put(vanillaTier.getLevel(), vanillaTier);
        }
        for (BaseMekanismMaterial tier : tiers) {
            int level = tier.getLevel();
            Collection<Tier> equivalent = vanillaTiers.get(level);
            Collection<Tier> vanillaNext = vanillaTiers.get(level + 1);
            //If the tier is equivalent to another tier then the equivalent one should be placed in the after list
            // and if it is equivalent to a vanilla tier (like all ours are when equivalent), the next tier
            // should also specify the next tier in the before list
            TierSortingRegistry.registerTier(tier, rl(tier.getRegistryPrefix()), new ArrayList<>(equivalent), new ArrayList<>(vanillaNext));
        }
    }

    private void setStackIfEmpty(LivingEntity entity, EquipmentSlot slot, ItemStack item) {
        if (entity.getItemBySlot(slot).isEmpty()) {
            entity.setItemSlot(slot, item);
        }
    }

    private void setEntityArmorWithChance(RandomSource random, LivingEntity entity, IItemProvider sword, IItemProvider helmet, IItemProvider chestplate,
          IItemProvider leggings, IItemProvider boots, ArmorSpawnChanceConfig chanceConfig) {
        if (entity instanceof Zombie && random.nextDouble() < chanceConfig.swordChance.get()) {
            setStackIfEmpty(entity, EquipmentSlot.MAINHAND, sword.getItemStack());
        }
        if (random.nextDouble() < chanceConfig.helmetChance.get()) {
            setStackIfEmpty(entity, EquipmentSlot.HEAD, helmet.getItemStack());
        }
        if (random.nextDouble() < chanceConfig.chestplateChance.get()) {
            setStackIfEmpty(entity, EquipmentSlot.CHEST, chestplate.getItemStack());
        }
        if (random.nextDouble() < chanceConfig.leggingsChance.get()) {
            setStackIfEmpty(entity, EquipmentSlot.LEGS, leggings.getItemStack());
        }
        if (random.nextDouble() < chanceConfig.bootsChance.get()) {
            setStackIfEmpty(entity, EquipmentSlot.FEET, boots.getItemStack());
        }
    }

    private void onLivingSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Zombie || entity instanceof Skeleton || entity instanceof Piglin) {
            //Don't bother calculating random numbers unless the instanceof checks pass
            RandomSource random = event.getLevel().getRandom();
            double chance = random.nextDouble();
            if (chance < MekanismToolsConfig.tools.armorSpawnRate.get()) {
                //We can only spawn refined glowstone armor on piglins
                switch (entity instanceof Piglin ? 0 : random.nextInt(6)) {
                    case 0 -> setEntityArmorWithChance(random, entity, ToolsItems.REFINED_GLOWSTONE_SWORD, ToolsItems.REFINED_GLOWSTONE_HELMET,
                          ToolsItems.REFINED_GLOWSTONE_CHESTPLATE, ToolsItems.REFINED_GLOWSTONE_LEGGINGS, ToolsItems.REFINED_GLOWSTONE_BOOTS,
                          MekanismToolsConfig.tools.refinedGlowstoneSpawnRate);
                    case 1 -> setEntityArmorWithChance(random, entity, ToolsItems.LAPIS_LAZULI_SWORD, ToolsItems.LAPIS_LAZULI_HELMET, ToolsItems.LAPIS_LAZULI_CHESTPLATE,
                          ToolsItems.LAPIS_LAZULI_LEGGINGS, ToolsItems.LAPIS_LAZULI_BOOTS, MekanismToolsConfig.tools.lapisLazuliSpawnRate);
                    case 2 -> setEntityArmorWithChance(random, entity, ToolsItems.REFINED_OBSIDIAN_SWORD, ToolsItems.REFINED_OBSIDIAN_HELMET,
                          ToolsItems.REFINED_OBSIDIAN_CHESTPLATE, ToolsItems.REFINED_OBSIDIAN_LEGGINGS, ToolsItems.REFINED_OBSIDIAN_BOOTS,
                          MekanismToolsConfig.tools.refinedObsidianSpawnRate);
                    case 3 -> setEntityArmorWithChance(random, entity, ToolsItems.STEEL_SWORD, ToolsItems.STEEL_HELMET, ToolsItems.STEEL_CHESTPLATE,
                          ToolsItems.STEEL_LEGGINGS, ToolsItems.STEEL_BOOTS, MekanismToolsConfig.tools.steelSpawnRate);
                    case 4 -> setEntityArmorWithChance(random, entity, ToolsItems.BRONZE_SWORD, ToolsItems.BRONZE_HELMET, ToolsItems.BRONZE_CHESTPLATE,
                          ToolsItems.BRONZE_LEGGINGS, ToolsItems.BRONZE_BOOTS, MekanismToolsConfig.tools.bronzeSpawnRate);
                    case 5 -> setEntityArmorWithChance(random, entity, ToolsItems.OSMIUM_SWORD, ToolsItems.OSMIUM_HELMET, ToolsItems.OSMIUM_CHESTPLATE,
                          ToolsItems.OSMIUM_LEGGINGS, ToolsItems.OSMIUM_BOOTS, MekanismToolsConfig.tools.osmiumSpawnRate);
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