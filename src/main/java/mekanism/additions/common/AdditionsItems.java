package mekanism.additions.common;

import mekanism.api.gas.GasRegistry;
import mekanism.common.item.ItemMekanism;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;

public class AdditionsItems {

    public static final Item EnrichedWaterI = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedWaterIBucket");
    public static final Item EnrichedWaterII = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedWaterIIBucket");
    public static final Item EnrichedWaterIII = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedWaterIIIBucket");

    public static final Item EnrichedDihydrogenSulfidI = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedDihydrogenSulfidIBucket");
    public static final Item EnrichedDihydrogendSulfidII = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedDihydrogendSulfidIIBucket");
    public static final Item EnrichedDihydrogenSulfidIII = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("EnrichedDihydrogenSulfidIIIBucket");

    public static void register() {
        /** new fluids for GPS */
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("enrichedwater"), new ItemStack(EnrichedWaterI), FluidContainerRegistry.EMPTY_BUCKET);
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("enrichedwatersnd"), new ItemStack(EnrichedWaterII), FluidContainerRegistry.EMPTY_BUCKET);
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("enrichedwaterrd"), new ItemStack(EnrichedWaterIII), FluidContainerRegistry.EMPTY_BUCKET);

        FluidContainerRegistry.registerFluidContainer(GasRegistry.getGas("enricheddihydrogensulfidGas").getFluid(), new ItemStack(EnrichedDihydrogenSulfidI), FluidContainerRegistry.EMPTY_BUCKET);
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("enricheddihydrogensulfidsnd"), new ItemStack(EnrichedDihydrogendSulfidII), FluidContainerRegistry.EMPTY_BUCKET);
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("enricheddihydrogensulfidrd"), new ItemStack(EnrichedDihydrogenSulfidIII), FluidContainerRegistry.EMPTY_BUCKET);
    }
}
