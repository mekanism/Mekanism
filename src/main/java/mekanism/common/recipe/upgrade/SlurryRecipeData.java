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
import mekanism.api.block.IHasTileEntity;
import mekanism.api.chemical.slurry.BasicSlurryTank;
import mekanism.api.chemical.slurry.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class SlurryRecipeData implements RecipeUpgradeData<SlurryRecipeData> {

    private final List<ISlurryTank> slurryTanks;

    SlurryRecipeData(ListNBT tanks) {
        int count = DataHandlerUtils.getMaxId(tanks, NBTConstants.TANK);
        slurryTanks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            slurryTanks.add(BasicSlurryTank.create(Long.MAX_VALUE, null));
        }
        DataHandlerUtils.readContainers(slurryTanks, tanks);
    }

    SlurryRecipeData(List<ISlurryTank> slurryTanks) {
        this.slurryTanks = slurryTanks;
    }

    @Nullable
    @Override
    public SlurryRecipeData merge(SlurryRecipeData other) {
        List<ISlurryTank> allTanks = new ArrayList<>(slurryTanks.size() + other.slurryTanks.size());
        allTanks.addAll(slurryTanks);
        allTanks.addAll(other.slurryTanks);
        return new SlurryRecipeData(allTanks);
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (slurryTanks.isEmpty()) {
            return true;
        }
        Item item = stack.getItem();
        Optional<ISlurryHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.SLURRY_HANDLER_CAPABILITY));
        List<ISlurryTank> slurryTanks = new ArrayList<>();
        if (capability.isPresent()) {
            ISlurryHandler slurryHandler = capability.get();
            for (int i = 0; i < slurryHandler.getSlurryTankCount(); i++) {
                int tank = i;
                slurryTanks.add(BasicSlurryTank.create(slurryHandler.getSlurryTankCapacity(tank),
                      type -> slurryHandler.isSlurryValid(tank, new SlurryStack(type, 1)), null));
            }
        } else if (item instanceof BlockItem) {
            TileEntityMekanism tile = null;
            Block block = ((BlockItem) item).getBlock();
            if (block instanceof IHasTileEntity<?>) {
                TileEntity tileEntity = ((IHasTileEntity<?>) block).getTileType().create();
                if (tileEntity instanceof TileEntityMekanism) {
                    tile = (TileEntityMekanism) tileEntity;
                }
            }
            if (tile == null || !tile.handles(SubstanceType.SLURRY)) {
                //Something went wrong
                return false;
            }
            TileEntityMekanism mekTile = tile;
            for (int i = 0; i < tile.getSlurryTankCount(); i++) {
                int tank = i;
                slurryTanks.add(BasicSlurryTank.create(tile.getSlurryTankCapacity(tank), type -> mekTile.isSlurryValid(tank, new SlurryStack(type, 1)), null));
            }
        } else {
            return false;
        }
        if (slurryTanks.isEmpty()) {
            //We don't actually have any tanks in the output
            return true;
        }
        //TODO: Improve the logic used so that it tries to batch similar types of slurry together first
        // and maybe make it try multiple slot combinations
        IMekanismSlurryHandler outputHandler = new IMekanismSlurryHandler() {
            @Nonnull
            @Override
            public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
                return slurryTanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
        boolean hasData = false;
        for (ISlurryTank slurryTank : this.slurryTanks) {
            if (!slurryTank.isEmpty()) {
                if (!outputHandler.insertSlurry(slurryTank.getStack(), Action.EXECUTE).isEmpty()) {
                    //If we have a remainder something failed so bail
                    return false;
                }
                hasData = true;
            }
        }
        if (hasData) {
            //We managed to transfer it all into valid slots, so save it to the stack
            ItemDataUtils.setList(stack, NBTConstants.SLURRY_TANKS, DataHandlerUtils.writeContainers(slurryTanks));
        }
        return true;
    }
}