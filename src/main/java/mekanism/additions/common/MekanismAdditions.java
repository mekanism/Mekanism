package mekanism.additions.common;

import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import io.netty.buffer.ByteBuf;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.recipe.RecipeHandler;
import net.minecraftforge.fluids.FluidRegistry;

import java.io.IOException;

@Mod(modid = "MekanismAdditions", name = "MekanismAdditions", version = "9.1.0", dependencies = "required-after:Mekanism")
public class MekanismAdditions implements IModule{

    @SidedProxy(clientSide = "mekanism.additions.client.AdditionsClientProxy", serverSide = "mekanism.additions.common.AdditionsCommonProxy")
    public static AdditionsCommonProxy proxy;

    @Mod.Instance("MekanismAdditions")
    public static MekanismAdditions instance;

    /** MekanismGenerators version number */
    public static Version versionNumber = new Version(9, 1, 0);

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Additions";
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        AdditionsItems.register();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

        //Add this module to the core list
        Mekanism.modulesLoaded.add(this);

        //Load the proxy
        proxy.loadConfiguration();

        addRecipes();

        //Finalization
        Mekanism.logger.info("Loaded MekanismAdditions module.");
    }

    private void addRecipes() {
        RecipeHandler.addChemicalInfuserRecipe(new GasStack(GasRegistry.getGas("hydrogen"), 1), new GasStack(GasRegistry.getGas("enricheddihydrogensulfidGas"), 1), new GasStack(GasRegistry.getGas("enricheddihydrogensulfidsnd"), 1));
        RecipeHandler.addChemicalInfuserRecipe(new GasStack(GasRegistry.getGas("hydrogen"), 1), new GasStack(GasRegistry.getGas("enricheddihydrogensulfidsnd"), 1), new GasStack(GasRegistry.getGas("enricheddihydrogensulfidrd"), 1));

        RecipeHandler.addChemicalWasherRecipe(new GasStack(GasRegistry.getGas("enricheddihydrogensulfidGas"), 1), new GasStack(GasRegistry.getGas("enrichedwater"), 1));
        RecipeHandler.addChemicalWasherRecipe(new GasStack(GasRegistry.getGas("enricheddihydrogensulfidsnd"), 1), new GasStack(GasRegistry.getGas("enrichedwatersnd"), 1));
        RecipeHandler.addChemicalWasherRecipe(new GasStack(GasRegistry.getGas("enricheddihydrogensulfidrd"), 1), new GasStack(GasRegistry.getGas("enrichedwaterrd"), 1));

        RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("enrichedwater", 1000), 2 * usage.heavyWaterElectrolysisUsage, new GasStack(GasRegistry.getGas("deuterium"), 1), new GasStack(GasRegistry.getGas("oxygen"), 100));
        RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("enrichedwatersnd", 100), 3 * usage.heavyWaterElectrolysisUsage, new GasStack(GasRegistry.getGas("deuterium"), 1), new GasStack(GasRegistry.getGas("oxygen"), 10));
        RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("enrichedwaterrd", 10), 4 * usage.heavyWaterElectrolysisUsage, new GasStack(GasRegistry.getGas("deuterium"), 1), new GasStack(GasRegistry.getGas("oxygen"), 1));

        //Chemical Washer Recipes for Gsp
        RecipeHandler.addChemicalWasherRecipe(new GasStack(GasRegistry.getGas("sulfurDioxideGas"), 1), new GasStack(GasRegistry.getGas("enricheddihydrogensulfidGas"), 1));

        //GSP Recipes-End//
    }

    @Override
    public void writeConfig(ByteBuf dataStream) throws IOException {
        dataStream.writeDouble(usage.heavyWaterElectrolysisUsage);
    }

    @Override
    public void readConfig(ByteBuf dataStream) throws IOException {
        usage.heavyWaterElectrolysisUsage = dataStream.readDouble();
    }

    @Override
    public void resetClient() {}

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
        if(event.modID.equals("MekanismAdditions")) {
            proxy.loadConfiguration();
        }
    }
}
