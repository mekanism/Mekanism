package mekanism.common;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.OreGas;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.Locale;

public class MekanismFluids
{
	public static final Gas Hydrogen = new Gas("hydrogen", "mekanism:blocks/liquid/LiquidHydrogen");
	public static final Gas Oxygen = new Gas("oxygen", "mekanism:blocks/liquid/LiquidOxygen");
	public static final Gas Water = new Gas("water", "mekanism:blocks/liquid/LiquidSteam");
	public static final Gas Chlorine = new Gas("chlorine", "mekanism:blocks/liquid/LiquidChlorine");
	public static final Gas SulfurDioxide = new Gas("sulfurdioxide", "mekanism:blocks/liquid/LiquidSulfurDioxide");
	public static final Gas SulfurTrioxide = new Gas("sulfurtrioxide", "mekanism:blocks/liquid/LiquidSulfurTrioxide");
	public static final Gas SulfuricAcid = new Gas("sulfuricacid", "mekanism:blocks/liquid/LiquidSulfuricAcid");
	public static final Gas HydrogenChloride = new Gas("hydrogenchloride", "mekanism:blocks/liquid/LiquidHydrogenChloride");
	
	public static final Fluid HeavyWater = new Fluid("heavywater", new ResourceLocation("mekanism:blocks/liquid/LiquidHeavyWater"), new ResourceLocation("mekanism:blocks/liquid/LiquidHeavyWater"));
	public static final Fluid Steam = new Fluid("steam", new ResourceLocation("mekanism:blocks/liquid/LiquidSteam"), new ResourceLocation("mekanism:blocks/liquid/LiquidSteam")).setGaseous(true);
	
	//Internal gases
	public static final Gas LiquidOsmium = new Gas("liquidosmium", "mekanism:blocks/liquid/LiquidOsmium");
	public static final Gas Ethene = new Gas("ethene", "mekanism:blocks/liquid/LiquidEthene");
	public static final Gas Sodium = new Gas("sodium", "mekanism:blocks/liquid/LiquidSodium");
	public static final Gas Brine = new Gas("brine", "mekanism:blocks/liquid/LiquidBrine");
	public static final Gas Deuterium = new Gas("deuterium", "mekanism:blocks/liquid/LiquidDeuterium");
	public static final Gas Tritium = new Gas("tritium", "mekanism:blocks/liquid/LiquidTritium");
	public static final Gas FusionFuel = new Gas("fusionfuel", "mekanism:blocks/liquid/LiquidDT");
	public static final Gas Lithium = new Gas("lithium", "mekanism:blocks/liquid/LiquidLithium");
	
	public static void register()
	{
		GasRegistry.register(Hydrogen).registerFluid("liquidhydrogen");
		GasRegistry.register(Oxygen).registerFluid("liquidoxygen");
		GasRegistry.register(Water).registerFluid();
		GasRegistry.register(Chlorine).registerFluid("liquidchlorine");
		GasRegistry.register(SulfurDioxide).registerFluid("liquidsulfurdioxide");
		GasRegistry.register(SulfurTrioxide).registerFluid("liquidsulfurtrioxide");
		GasRegistry.register(SulfuricAcid).registerFluid();
		GasRegistry.register(HydrogenChloride).registerFluid("liquidhydrogenchloride");
		GasRegistry.register(Ethene).registerFluid("liquidethene");
		GasRegistry.register(Sodium).registerFluid("liquidsodium");
		GasRegistry.register(Brine).registerFluid();
		GasRegistry.register(Deuterium).registerFluid("liquiddeuterium");
		GasRegistry.register(Tritium).registerFluid("liquidtritium");
		GasRegistry.register(FusionFuel).registerFluid("liquidfusionfuel");
		GasRegistry.register(Lithium).registerFluid("liquidlithium");
		
		GasRegistry.register(LiquidOsmium).setVisible(false);
		
		FluidRegistry.registerFluid(HeavyWater);
		FluidRegistry.registerFluid(Steam);
		
		for(Resource resource : Resource.values())
		{
			String name = resource.getName();
			String nameLower = name.toLowerCase(Locale.ROOT);
			
			OreGas clean = new OreGas("clean" + name, "oregas." + nameLower);
			clean.setVisible(false).setTint(resource.tint);

			OreGas dirty = new OreGas(nameLower, "oregas." + nameLower).setCleanGas(clean);
			dirty.setVisible(false).setTint(resource.tint);

			GasRegistry.register(clean);
			GasRegistry.register(dirty);
		}
		
		FluidRegistry.enableUniversalBucket();
		
		FluidRegistry.addBucketForFluid(HeavyWater);
		FluidRegistry.addBucketForFluid(Brine.getFluid());
		FluidRegistry.addBucketForFluid(Lithium.getFluid());
	}
}
