package mekanism.common;

import java.util.Locale;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.OreGas;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class MekanismFluids {

    public static final Gas Hydrogen = new Gas("hydrogen", 0xFFFFFF);
    public static final Gas Oxygen = new Gas("oxygen", 0x6CE2FF);
    public static final Gas Water = new Gas("water", "mekanism:blocks/liquid/LiquidSteam");
    public static final Gas Chlorine = new Gas("chlorine", 0xCFE800);
    public static final Gas SulfurDioxide = new Gas("sulfurdioxide", 0xA99D90);
    public static final Gas SulfurTrioxide = new Gas("sulfurtrioxide", 0xCE6C6C);
    public static final Gas SulfuricAcid = new Gas("sulfuricacid", 0x82802B);
    public static final Gas HydrogenChloride = new Gas("hydrogenchloride", 0xA8F1E9);

    public static final Fluid HeavyWater = new Fluid("heavywater",
          new ResourceLocation("mekanism:blocks/liquid/LiquidHeavyWater"),
          new ResourceLocation("mekanism:blocks/liquid/LiquidHeavyWater"));
    public static final Fluid Steam = new Fluid("steam", new ResourceLocation("mekanism:blocks/liquid/LiquidSteam"),
          new ResourceLocation("mekanism:blocks/liquid/LiquidSteam")).setGaseous(true);

    //Internal gases
    public static final Gas LiquidOsmium = new Gas("liquidosmium", 0x9090A3);
    public static final Gas Ethene = new Gas("ethene", 0xEACCF9);
    public static final Gas Sodium = new Gas("sodium", 0xE9FEF4);
    public static final Gas Brine = new Gas("brine", 0xFEEF9C);
    public static final Gas Deuterium = new Gas("deuterium", 0xFF3232);
    public static final Gas Tritium = new Gas("tritium", 0x64FF70);
    public static final Gas FusionFuel = new Gas("fusionfuel", 0x7E007D);
    public static final Gas Lithium = new Gas("lithium", 0xEBA400);

    public static void register() {
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

        for (Resource resource : Resource.values()) {
            String name = resource.getName();
            String nameLower = name.toLowerCase(Locale.ROOT);
            //Clean
            OreGas clean = new OreGas("clean" + name, "oregas." + nameLower, resource.tint);
            GasRegistry.register(clean);
            //Dirty
            GasRegistry.register(new OreGas(nameLower, "oregas." + nameLower, resource.tint, clean));
        }

        FluidRegistry.enableUniversalBucket();

        FluidRegistry.addBucketForFluid(HeavyWater);
        FluidRegistry.addBucketForFluid(Brine.getFluid());
        FluidRegistry.addBucketForFluid(Lithium.getFluid());
    }
}
