package mekanism.tools.common;

import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.lib.Version;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.registries.ToolsArmorMaterials;
import mekanism.tools.common.registries.ToolsCreativeTabs;
import mekanism.tools.common.registries.ToolsItems;
import mekanism.tools.common.registries.ToolsRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MekanismTools.MODID)
public class MekanismTools implements IModModule {

    public static final String MODID = "mekanismtools";

    public static MekanismTools instance;

    /**
     * MekanismTools version number
     */
    public final Version versionNumber;

    public MekanismTools(ModContainer modContainer, IEventBus modEventBus) {
        Mekanism.addModule(instance = this);
        //Set our version number to match the neoforge.mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(modContainer);
        MekanismToolsConfig.registerConfigs(modContainer);
        //Register the listener for special mob spawning (mobs with Mekanism armor/tools)
        NeoForge.EVENT_BUS.addListener(MobEquipmentHelper::onLivingSpecialSpawn);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(MekanismToolsConfig::onConfigLoad);
        ToolsArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        ToolsItems.ITEMS.register(modEventBus);
        ToolsCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ToolsRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Mekanism.logger.info("Loaded 'Mekanism: Tools' module.");
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Tools";
    }
}