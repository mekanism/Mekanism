package mekanism.common;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.OreGas;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class MekanismFluids
{
	public static void register()
	{
		GasRegistry.register(new Gas("hydrogen", "mekanism:blocks/liquid/LiquidHydrogen")).registerFluid();
		GasRegistry.register(new Gas("oxygen", "mekanism:blocks/liquid/LiquidOxygen")).registerFluid();
		GasRegistry.register(new Gas("water", "mekanism:blocks/liquid/LiquidSteam")).registerFluid();
		GasRegistry.register(new Gas("chlorine", "mekanism:blocks/liquid/LiquidChlorine")).registerFluid();
		GasRegistry.register(new Gas("sulfurDioxideGas", "mekanism:blocks/liquid/LiquidSulfurDioxide")).registerFluid();
		GasRegistry.register(new Gas("sulfurTrioxideGas", "mekanism:blocks/liquid/LiquidSulfurTrioxide")).registerFluid();
		GasRegistry.register(new Gas("sulfuricAcid", "mekanism:blocks/liquid/LiquidSulfuricAcid")).registerFluid();
		GasRegistry.register(new Gas("hydrogenChloride", "mekanism:blocks/liquid/LiquidHydrogenChloride")).registerFluid();
		GasRegistry.register(new Gas("liquidOsmium", "mekanism:blocks/liquid/LiquidOsmium").setVisible(false));
		GasRegistry.register(new Gas("liquidStone", "mekanism:blocks/liquid/LiquidStone").setVisible(false));
		GasRegistry.register(new Gas("ethene", "mekanism:blocks/liquid/LiquidEthene").registerFluid());
		GasRegistry.register(new Gas("sodium", "mekanism:blocks/liquid/LiquidSodium").registerFluid());
		GasRegistry.register(new Gas("brine", "mekanism:blocks/liquid/LiquidBrine").registerFluid());
		GasRegistry.register(new Gas("deuterium", "mekanism:blocks/liquid/LiquidDeuterium")).registerFluid();
		GasRegistry.register(new Gas("tritium", "mekanism:blocks/liquid/LiquidTritium")).registerFluid();
		GasRegistry.register(new Gas("fusionFuelDT", "mekanism:blocks/liquid/LiquidDT")).registerFluid();
		GasRegistry.register(new Gas("lithium", "mekanism:blocks/liquid/LiquidLithium")).registerFluid();
		
		FluidRegistry.getFluid("brine").setGaseous(false);
		
		FluidRegistry.registerFluid(new Fluid("heavyWater", new ResourceLocation("mekanism:blocks/liquid/LiquidHeavyWater"), new ResourceLocation("mekanism:blocks/liquid/LiquidHeavyWater")));
		FluidRegistry.registerFluid(new Fluid("steam", new ResourceLocation("mekanism:blocks/liquid/LiquidSteam"), new ResourceLocation("mekanism:blocks/liquid/LiquidSteam")).setGaseous(true));
		
		for(Resource resource : Resource.values())
		{
			String name = resource.getName();
			
			OreGas clean = (OreGas)GasRegistry.register(new OreGas("clean" + name, "oregas." + name.toLowerCase()).setVisible(false));
			GasRegistry.register(new OreGas(name.toLowerCase(), "oregas." + name.toLowerCase()).setCleanGas(clean).setVisible(false));
		}
		
		FluidRegistry.enableUniversalBucket();
		
		FluidRegistry.addBucketForFluid(FluidRegistry.getFluid("heavywater"));
		FluidRegistry.addBucketForFluid(FluidRegistry.getFluid("brine"));
		FluidRegistry.addBucketForFluid(FluidRegistry.getFluid("lithium"));
	}
}
