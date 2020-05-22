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
import mekanism.api.chemical.pigment.BasicPigmentTank;
import mekanism.api.chemical.pigment.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.PigmentStack;
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
public class PigmentRecipeData implements RecipeUpgradeData<PigmentRecipeData> {

    private final List<IPigmentTank> pigmentTanks;

    PigmentRecipeData(ListNBT tanks) {
        int count = DataHandlerUtils.getMaxId(tanks, NBTConstants.TANK);
        pigmentTanks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            pigmentTanks.add(BasicPigmentTank.create(Long.MAX_VALUE, null));
        }
        DataHandlerUtils.readContainers(pigmentTanks, tanks);
    }

    PigmentRecipeData(List<IPigmentTank> pigmentTanks) {
        this.pigmentTanks = pigmentTanks;
    }

    @Nullable
    @Override
    public PigmentRecipeData merge(PigmentRecipeData other) {
        List<IPigmentTank> allTanks = new ArrayList<>(pigmentTanks.size() + other.pigmentTanks.size());
        allTanks.addAll(pigmentTanks);
        allTanks.addAll(other.pigmentTanks);
        return new PigmentRecipeData(allTanks);
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (pigmentTanks.isEmpty()) {
            return true;
        }
        Item item = stack.getItem();
        Optional<IPigmentHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.PIGMENT_HANDLER_CAPABILITY));
        List<IPigmentTank> pigmentTanks = new ArrayList<>();
        if (capability.isPresent()) {
            IPigmentHandler pigmentHandler = capability.get();
            for (int i = 0; i < pigmentHandler.getPigmentTankCount(); i++) {
                int tank = i;
                pigmentTanks.add(BasicPigmentTank.create(pigmentHandler.getPigmentTankCapacity(tank),
                      type -> pigmentHandler.isPigmentValid(tank, new PigmentStack(type, 1)), null));
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
            if (tile == null || !tile.handles(SubstanceType.PIGMENT)) {
                //Something went wrong
                return false;
            }
            TileEntityMekanism mekTile = tile;
            for (int i = 0; i < tile.getPigmentTankCount(); i++) {
                int tank = i;
                pigmentTanks.add(BasicPigmentTank.create(tile.getPigmentTankCapacity(tank), type -> mekTile.isPigmentValid(tank, new PigmentStack(type, 1)), null));
            }
        } else {
            return false;
        }
        if (pigmentTanks.isEmpty()) {
            //We don't actually have any tanks in the output
            return true;
        }
        //TODO: Improve the logic used so that it tries to batch similar types of pigment together first
        // and maybe make it try multiple slot combinations
        IMekanismPigmentHandler outputHandler = new IMekanismPigmentHandler() {
            @Nonnull
            @Override
            public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
                return pigmentTanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
        boolean hasData = false;
        for (IPigmentTank pigmentTank : this.pigmentTanks) {
            if (!pigmentTank.isEmpty()) {
                if (!outputHandler.insertPigment(pigmentTank.getStack(), Action.EXECUTE).isEmpty()) {
                    //If we have a remainder something failed so bail
                    return false;
                }
                hasData = true;
            }
        }
        if (hasData) {
            //We managed to transfer it all into valid slots, so save it to the stack
            ItemDataUtils.setList(stack, NBTConstants.PIGMENT_TANKS, DataHandlerUtils.writeContainers(pigmentTanks));
        }
        return true;
    }
}