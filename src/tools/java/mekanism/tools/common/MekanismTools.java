package mekanism.tools.common;

import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.lib.Version;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.registries.ToolsCreativeTabs;
import mekanism.tools.common.registries.ToolsDataComponents;
import mekanism.tools.common.registries.ToolsItems;
import mekanism.tools.common.registries.ToolsRecipeSerializers;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import org.jspecify.annotations.Nullable;

@Mod(MekanismTools.MODID)
public class MekanismTools implements IModModule {

    public static final String MODID = MekanismAPI.TOOLS_MODID;

    @Nullable
    public static MekanismTools instance;

    /// MekanismTools version number
    public final Version versionNumber;

    public MekanismTools(ModContainer modContainer, IEventBus modEventBus) {
        Mekanism.addModule(instance = this);
        //Set our version number to match the neoforge.mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(modContainer);
        MekanismToolsConfig.registerConfigs(modContainer);
        //Register the listener for special mob spawning (mobs with Mekanism armor/tools)
        NeoForge.EVENT_BUS.addListener(FinalizeSpawnEvent.class, MobEquipmentHelper::onLivingSpecialSpawn);

        modEventBus.addListener(FMLCommonSetupEvent.class, this::commonSetup);
        modEventBus.addListener(ModConfigEvent.class, MekanismToolsConfig::onConfigLoad);
        ToolsItems.ITEMS.register(modEventBus);
        ToolsDataComponents.register(modEventBus);
        ToolsCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ToolsRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
    }

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
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