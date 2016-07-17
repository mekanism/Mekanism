package mekanism.additions.common;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemMekanism;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;

public class AdditionsItems {

    public static IIconRegister register;

    public static final Item EnrichedWaterI = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedWaterI Bucket");
    public static final Item EnrichedWaterII = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedWaterII Bucket");
    public static final Item EnrichedWaterIII = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedWaterIII Bucket");

    public static final Item EnrichedDihydrogenSulfidI = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedDihydrogenSulfidI Bucket");
    public static final Item EnrichedDihydrogenSulfidII = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedDihydrogenSulfidII Bucket");
    public static final Item EnrichedDihydrogenSulfidIII = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedDihydrogenSulfidIII Bucket");

    public static void register() {

        GasRegistry.register(new Gas("enrichedwater")).registerFluid();
        GasRegistry.register(new Gas("enrichedwatersnd")).registerFluid();
        GasRegistry.register(new Gas("enrichedwaterrd")).registerFluid();
        GasRegistry.register(new Gas("dihydrogensulfid")).registerFluid();

        GasRegistry.register(new Gas("enricheddihydrogensulfidGas")).registerFluid();
        GasRegistry.register(new Gas("enricheddihydrogensulfidsnd")).registerFluid();
        GasRegistry.register(new Gas("enricheddihydrogensulfidrd")).registerFluid();

        FluidRegistry.registerFluid(new Fluid("dihydrogensulfid"));
        FluidRegistry.registerFluid(new Fluid("enricheddihydrogensulfidGas"));
        FluidRegistry.registerFluid(new Fluid("enricheddihydrogensulfidsnd"));
        FluidRegistry.registerFluid(new Fluid("enricheddihydrogensulfidrd"));

        FluidRegistry.registerFluid(new Fluid("enrichedwater"));
        FluidRegistry.registerFluid(new Fluid("enrichedwatersnd"));
        FluidRegistry.registerFluid(new Fluid("enrichedwaterrd"));




        /** new fluids for GPS */
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("enrichedwater"), new ItemStack(EnrichedWaterI), FluidContainerRegistry.EMPTY_BUCKET);
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("enrichedwatersnd"), new ItemStack(EnrichedWaterII), FluidContainerRegistry.EMPTY_BUCKET);
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("enrichedwaterrd"), new ItemStack(EnrichedWaterIII), FluidContainerRegistry.EMPTY_BUCKET);

        FluidContainerRegistry.registerFluidContainer(GasRegistry.getGas("enricheddihydrogensulfidGas").getFluid(), new ItemStack(EnrichedDihydrogenSulfidI), FluidContainerRegistry.EMPTY_BUCKET);
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("enricheddihydrogensulfidsnd"), new ItemStack(EnrichedDihydrogenSulfidII), FluidContainerRegistry.EMPTY_BUCKET);
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("enricheddihydrogensulfidrd"), new ItemStack(EnrichedDihydrogenSulfidIII), FluidContainerRegistry.EMPTY_BUCKET);
    }
}
