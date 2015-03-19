package mekanism.client.nei;

import mekanism.client.gui.GuiChemicalCrystallizer;
import mekanism.client.gui.GuiChemicalDissolutionChamber;
import mekanism.client.gui.GuiChemicalInfuser;
import mekanism.client.gui.GuiChemicalInjectionChamber;
import mekanism.client.gui.GuiChemicalOxidizer;
import mekanism.client.gui.GuiChemicalWasher;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.client.gui.GuiPRC;
import mekanism.client.gui.GuiPrecisionSawmill;
import mekanism.client.gui.GuiPurificationChamber;
import mekanism.client.gui.GuiRotaryCondensentrator;
import mekanism.client.gui.GuiSolarEvaporationController;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import net.minecraft.item.ItemStack;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.guihook.GuiContainerManager;

public class NEIMekanismConfig implements IConfigureNEI
{
	@Override
	public void loadConfig()
	{
		API.registerRecipeHandler(new EnrichmentChamberRecipeHandler());
		API.registerUsageHandler(new EnrichmentChamberRecipeHandler());

		API.registerRecipeHandler(new OsmiumCompressorRecipeHandler());
		API.registerUsageHandler(new OsmiumCompressorRecipeHandler());

		API.registerRecipeHandler(new CrusherRecipeHandler());
		API.registerUsageHandler(new CrusherRecipeHandler());

		API.registerRecipeHandler(new CombinerRecipeHandler());
		API.registerUsageHandler(new CombinerRecipeHandler());

		API.registerRecipeHandler(new MetallurgicInfuserRecipeHandler());
		API.registerUsageHandler(new MetallurgicInfuserRecipeHandler());

		API.registerRecipeHandler(new PurificationChamberRecipeHandler());
		API.registerUsageHandler(new PurificationChamberRecipeHandler());

		API.registerRecipeHandler(new ChemicalInjectionChamberRecipeHandler());
		API.registerUsageHandler(new ChemicalInjectionChamberRecipeHandler());

		API.registerRecipeHandler(new MekanismRecipeHandler());
		API.registerUsageHandler(new MekanismRecipeHandler());

		API.registerRecipeHandler(new ChemicalOxidizerRecipeHandler());
		API.registerUsageHandler(new ChemicalOxidizerRecipeHandler());

		API.registerRecipeHandler(new ChemicalInfuserRecipeHandler());
		API.registerUsageHandler(new ChemicalInfuserRecipeHandler());

		API.registerRecipeHandler(new RotaryCondensentratorRecipeHandler());
		API.registerUsageHandler(new RotaryCondensentratorRecipeHandler());

		API.registerRecipeHandler(new ElectrolyticSeparatorRecipeHandler());
		API.registerUsageHandler(new ElectrolyticSeparatorRecipeHandler());

		API.registerRecipeHandler(new PrecisionSawmillRecipeHandler());
		API.registerUsageHandler(new PrecisionSawmillRecipeHandler());

		API.registerRecipeHandler(new SolarEvaporationRecipeHandler());
		API.registerUsageHandler(new SolarEvaporationRecipeHandler());

		API.registerRecipeHandler(new ChemicalDissolutionChamberRecipeHandler());
		API.registerUsageHandler(new ChemicalDissolutionChamberRecipeHandler());

		API.registerRecipeHandler(new ChemicalWasherRecipeHandler());
		API.registerUsageHandler(new ChemicalWasherRecipeHandler());

		API.registerRecipeHandler(new ChemicalCrystallizerRecipeHandler());
		API.registerUsageHandler(new ChemicalCrystallizerRecipeHandler());
		
		API.registerRecipeHandler(new PRCRecipeHandler());
		API.registerUsageHandler(new PRCRecipeHandler());
		
		API.registerRecipeHandler(new SolarNeutronRecipeHandler());
		API.registerUsageHandler(new SolarNeutronRecipeHandler());

		API.setGuiOffset(GuiEnrichmentChamber.class, 16, 6);
		API.setGuiOffset(GuiOsmiumCompressor.class, 16, 6);
		API.setGuiOffset(GuiCrusher.class, 16, 6);
		API.setGuiOffset(GuiCombiner.class, 16, 6);
		API.setGuiOffset(GuiPurificationChamber.class, 16, 6);
		API.setGuiOffset(GuiChemicalInjectionChamber.class, 16, 6);
		API.setGuiOffset(GuiMetallurgicInfuser.class, 5, 15);
		API.setGuiOffset(GuiChemicalOxidizer.class, ChemicalOxidizerRecipeHandler.xOffset, ChemicalOxidizerRecipeHandler.yOffset);
		API.setGuiOffset(GuiChemicalInfuser.class, ChemicalInfuserRecipeHandler.xOffset, ChemicalInfuserRecipeHandler.yOffset);
		API.setGuiOffset(GuiRotaryCondensentrator.class, RotaryCondensentratorRecipeHandler.xOffset, RotaryCondensentratorRecipeHandler.yOffset);
		API.setGuiOffset(GuiElectrolyticSeparator.class, ElectrolyticSeparatorRecipeHandler.xOffset, ElectrolyticSeparatorRecipeHandler.yOffset);
		API.setGuiOffset(GuiPrecisionSawmill.class, 16, 6);
		API.setGuiOffset(GuiSolarEvaporationController.class, SolarEvaporationRecipeHandler.xOffset, SolarEvaporationRecipeHandler.yOffset);
		API.setGuiOffset(GuiChemicalDissolutionChamber.class, ChemicalDissolutionChamberRecipeHandler.xOffset, ChemicalDissolutionChamberRecipeHandler.yOffset);
		API.setGuiOffset(GuiChemicalWasher.class, ChemicalWasherRecipeHandler.xOffset, ChemicalWasherRecipeHandler.yOffset);
		API.setGuiOffset(GuiChemicalCrystallizer.class, ChemicalCrystallizerRecipeHandler.xOffset, ChemicalCrystallizerRecipeHandler.yOffset);
		API.setGuiOffset(GuiPRC.class, PRCRecipeHandler.xOffset, PRCRecipeHandler.yOffset);
		API.setGuiOffset(GuiSolarEvaporationController.class, SolarEvaporationRecipeHandler.xOffset, SolarEvaporationRecipeHandler.yOffset);
		API.setGuiOffset(GuiSolarNeutronActivator.class, SolarNeutronRecipeHandler.xOffset, SolarNeutronRecipeHandler.yOffset);
		
		GuiContainerManager.addSlotClickHandler(new MekanismSlotClickHandler());
		
		API.registerNEIGuiHandler(new ElementBoundHandler());

		API.hideItem(new ItemStack(MekanismBlocks.BoundingBlock));
		API.hideItem(new ItemStack(MekanismItems.ItemProxy));
	}

	@Override
	public String getName()
	{
		return "Mekanism NEI Plugin";
	}

	@Override
	public String getVersion()
	{
		return "8.0.0";
	}
}
