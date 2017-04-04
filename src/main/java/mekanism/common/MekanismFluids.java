package mekanism.common;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.OreGas;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class MekanismFluids
{
	public static final Gas Hydrogen = new Gas("hydrogen", "mekanism:blocks/liquid/LiquidHydrogen");
	public static final Gas Oxygen = new Gas("oxygen", "mekanism:blocks/liquid/LiquidOxygen");
	public static final Gas Water = new Gas("water", "mekanism:blocks/liquid/LiquidSteam");
	public static final Gas Chlorine = new Gas("chlorine", "mekanism:blocks/liquid/LiquidChlorine");
	public static final Gas SulfurDioxide = new Gas("sulfurDioxideGas", "mekanism:blocks/liquid/LiquidSulfurDioxide");
	public static final Gas SulfurTrioxide = new Gas("sulfurTrioxideGas", "mekanism:blocks/liquid/LiquidSulfurTrioxide");
	public static final Gas SulfuricAcid = new Gas("sulfuricAcid", "mekanism:blocks/liquid/LiquidSulfuricAcid");
	public static final Gas HydrogenChloride = new Gas("hydrogenChloride", "mekanism:blocks/liquid/LiquidHydrogenChloride");
	
	
	//Internal gases
	public static final Gas LiquidOsmium = new Gas("liquidOsmium", "mekanism:blocks/liquid/LiquidOsmium");
	public static final Gas LiquidStone = new Gas("liquidStone", "mekanism:blocks/liquid/LiquidStone");
	public static final Gas Ethene = new Gas("ethene", "mekanism:blocks/liquid/LiquidEthene");
	public static final Gas Sodium = new Gas("sodium", "mekanism:blocks/liquid/LiquidSodium");
	public static final Gas Brine = new Gas("brine", "mekanism:blocks/liquid/LiquidBrine");
	public static final Gas Deuterium = new Gas("deuterium", "mekanism:blocks/liquid/LiquidDeuterium");
	public static final Gas Tritium = new Gas("tritium", "mekanism:blocks/liquid/LiquidTritium");
	public static final Gas FusionFuel = new Gas("fusionFuelDT", "mekanism:blocks/liquid/LiquidDT");
	public static final Gas Lithium = new Gas("lithium", "mekanism:blocks/liquid/LiquidLithium");
	
	public static void register()
	{
		GasRegistry.register(Hydrogen).registerFluid();
		GasRegistry.register(Oxygen).registerFluid();
		GasRegistry.register(Water).registerFluid();
		GasRegistry.register(Chlorine).registerFluid();
		GasRegistry.register(SulfurDioxide).registerFluid();
		GasRegistry.register(SulfurTrioxide).registerFluid();
		GasRegistry.register(SulfuricAcid).registerFluid();
		GasRegistry.register(HydrogenChloride).registerFluid();
		GasRegistry.register(Ethene).registerFluid();
		GasRegistry.register(Sodium).registerFluid();
		GasRegistry.register(Brine).registerFluid();
		GasRegistry.register(Deuterium).registerFluid();
		GasRegistry.register(Tritium).registerFluid();
		GasRegistry.register(FusionFuel).registerFluid();
		GasRegistry.register(Lithium).registerFluid();
		
		GasRegistry.register(LiquidOsmium).setVisible(false);
		GasRegistry.register(LiquidStone).setVisible(false);
		
		FluidRegistry.registerFluid(new Fluid("heavyWater", new ResourceLocation("mekanism:blocks/liquid/LiquidHeavyWater"), new ResourceLocation("mekanism:blocks/liquid/LiquidHeavyWater")));
		FluidRegistry.registerFluid(new Fluid("steam", new ResourceLocation("mekanism:blocks/liquid/LiquidSteam"), new ResourceLocation("mekanism:blocks/liquid/LiquidSteam")).setGaseous(true));
		
		for(Resource resource : Resource.values())
		{
			String name = resource.getName();
			
			OreGas clean = (OreGas)GasRegistry.register(new OreGas("clean" + name, "oregas." + name.toLowerCase()).setVisible(false));
			GasRegistry.register(new OreGas(name.toLowerCase(), "oregas." + name.toLowerCase()).setCleanGas(clean).setVisible(false));
		}
		
		FluidRegistry.enableUniversalBucket();
		
		FluidRegistry.addBucketForFluid(FluidRegistry.getFluid("heavyWater"));
		FluidRegistry.addBucketForFluid(FluidRegistry.getFluid("brine"));
		FluidRegistry.addBucketForFluid(FluidRegistry.getFluid("lithium"));
	}
}
