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
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.infuse.BasicInfusionTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
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
public class InfusionRecipeData implements RecipeUpgradeData<InfusionRecipeData> {

    private final List<IChemicalTank<InfuseType, InfusionStack>> infusionTanks;

    InfusionRecipeData(ListNBT tanks) {
        int count = DataHandlerUtils.getMaxId(tanks, NBTConstants.TANK);
        infusionTanks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            infusionTanks.add(BasicInfusionTank.create(Integer.MAX_VALUE, null));
        }
        DataHandlerUtils.readTanks(infusionTanks, tanks);
    }

    InfusionRecipeData(List<IChemicalTank<InfuseType, InfusionStack>> infusionTanks) {
        this.infusionTanks = infusionTanks;
    }

    @Nullable
    @Override
    public InfusionRecipeData merge(InfusionRecipeData other) {
        List<IChemicalTank<InfuseType, InfusionStack>> allTanks = new ArrayList<>(infusionTanks.size() + other.infusionTanks.size());
        allTanks.addAll(infusionTanks);
        allTanks.addAll(other.infusionTanks);
        return new InfusionRecipeData(allTanks);
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (infusionTanks.isEmpty()) {
            return true;
        }
        Item item = stack.getItem();
        Optional<IInfusionHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY));
        List<IChemicalTank<InfuseType, InfusionStack>> infusionTanks = new ArrayList<>();
        if (capability.isPresent()) {
            IInfusionHandler infusionHandler = capability.get();
            for (int i = 0; i < infusionHandler.getInfusionTankCount(); i++) {
                int tank = i;
                infusionTanks.add(BasicInfusionTank.create(infusionHandler.getInfusionTankCapacity(tank),
                      type -> infusionHandler.isInfusionValid(tank, new InfusionStack(type, 1)), null));
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
            if (tile == null || !tile.handles(SubstanceType.INFUSION)) {
                //Something went wrong
                return false;
            }
            TileEntityMekanism mekTile = tile;
            for (int i = 0; i < tile.getInfusionTankCount(); i++) {
                int tank = i;
                infusionTanks.add(BasicInfusionTank.create(tile.getInfusionTankCapacity(tank), type -> mekTile.isInfusionValid(tank, new InfusionStack(type, 1)), null));
            }
        } else {
            return false;
        }
        if (infusionTanks.isEmpty()) {
            //We don't actually have any tanks in the output
            return true;
        }
        //TODO: Improve the logic used so that it tries to batch similar types of infuse types together first
        // and maybe make it try multiple slot combinations
        IMekanismInfusionHandler outputHandler = new IMekanismInfusionHandler() {
            @Nonnull
            @Override
            public List<? extends IChemicalTank<InfuseType, InfusionStack>> getInfusionTanks(@Nullable Direction side) {
                return infusionTanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
        boolean hasData = false;
        for (IChemicalTank<InfuseType, InfusionStack> infusionTank : this.infusionTanks) {
            if (!infusionTank.isEmpty()) {
                if (!outputHandler.insertInfusion(infusionTank.getStack(), Action.EXECUTE).isEmpty()) {
                    //If we have a remainder something failed so bail
                    return false;
                }
                hasData = true;
            }
        }
        if (hasData) {
            //We managed to transfer it all into valid slots, so save it to the stack
            ItemDataUtils.setList(stack, NBTConstants.INFUSION_TANKS, DataHandlerUtils.writeTanks(infusionTanks));
        }
        return true;
    }
}