package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidRecipeData implements RecipeUpgradeData<FluidRecipeData> {

    private final List<IExtendedFluidTank> fluidTanks;

    FluidRecipeData(ListNBT tanks) {
        int count = DataHandlerUtils.getMaxId(tanks, NBTConstants.TANK);
        fluidTanks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            fluidTanks.add(BasicFluidTank.create(Integer.MAX_VALUE, null));
        }
        DataHandlerUtils.readContainers(fluidTanks, tanks);
    }

    private FluidRecipeData(List<IExtendedFluidTank> fluidTanks) {
        this.fluidTanks = fluidTanks;
    }

    @Nullable
    @Override
    public FluidRecipeData merge(FluidRecipeData other) {
        List<IExtendedFluidTank> allTanks = new ArrayList<>(fluidTanks.size() + other.fluidTanks.size());
        allTanks.addAll(fluidTanks);
        allTanks.addAll(other.fluidTanks);
        return new FluidRecipeData(allTanks);
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (fluidTanks.isEmpty()) {
            return true;
        }
        Item item = stack.getItem();
        Optional<IFluidHandlerItem> capability = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).resolve();
        List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
        if (capability.isPresent()) {
            IFluidHandlerItem fluidHandler = capability.get();
            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                int tank = i;
                fluidTanks.add(BasicFluidTank.create(fluidHandler.getTankCapacity(tank), fluid -> fluidHandler.isFluidValid(tank, fluid), null));
            }
        } else if (item instanceof BlockItem) {
            TileEntityMekanism tile = null;
            Block block = ((BlockItem) item).getBlock();
            if (block instanceof IHasTileEntity) {
                TileEntity tileEntity = ((IHasTileEntity<?>) block).getTileType().create();
                if (tileEntity instanceof TileEntityMekanism) {
                    tile = (TileEntityMekanism) tileEntity;
                }
            }
            if (tile == null || !tile.handles(SubstanceType.FLUID)) {
                //Something went wrong
                return false;
            }
            TileEntityMekanism mekTile = tile;
            for (int i = 0; i < tile.getTanks(); i++) {
                int tank = i;
                fluidTanks.add(BasicFluidTank.create(tile.getTankCapacity(tank), fluid -> mekTile.isFluidValid(tank, fluid), null));
            }
        } else {
            return false;
        }
        if (fluidTanks.isEmpty()) {
            //We don't actually have any tanks in the output
            return true;
        }
        //TODO: Improve the logic used so that it tries to batch similar types of fluids together first
        // and maybe make it try multiple slot combinations
        IMekanismFluidHandler outputHandler = new IMekanismFluidHandler() {
            @Nonnull
            @Override
            public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
                return fluidTanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
        boolean hasData = false;
        for (IExtendedFluidTank fluidTank : this.fluidTanks) {
            if (!fluidTank.isEmpty()) {
                if (!outputHandler.insertFluid(fluidTank.getFluid(), Action.EXECUTE).isEmpty()) {
                    //If we have a remainder something failed so bail
                    return false;
                }
                hasData = true;
            }
        }
        if (hasData) {
            //We managed to transfer it all into valid slots, so save it to the stack
            ItemDataUtils.setList(stack, NBTConstants.FLUID_TANKS, DataHandlerUtils.writeContainers(fluidTanks));
        }
        return true;
    }
}