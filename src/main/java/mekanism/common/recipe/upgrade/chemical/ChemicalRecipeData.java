package mekanism.common.recipe.upgrade.chemical;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandlerWrapper;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.recipe.upgrade.RecipeUpgradeData;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalRecipeData<HANDLER, CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      implements RecipeUpgradeData<ChemicalRecipeData<HANDLER, CHEMICAL, STACK, TANK>> {

    protected final List<TANK> tanks;

    protected ChemicalRecipeData(ListNBT tanks) {
        int count = DataHandlerUtils.getMaxId(tanks, NBTConstants.TANK);
        this.tanks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.tanks.add(createTank());
        }
        DataHandlerUtils.readContainers(this.tanks, tanks);
    }

    protected ChemicalRecipeData(List<TANK> tanks) {
        this.tanks = tanks;
    }

    @Nullable
    @Override
    public ChemicalRecipeData<HANDLER, CHEMICAL, STACK, TANK> merge(ChemicalRecipeData<HANDLER, CHEMICAL, STACK, TANK> other) {
        List<TANK> allTanks = new ArrayList<>(tanks.size() + other.tanks.size());
        allTanks.addAll(tanks);
        allTanks.addAll(other.tanks);
        return create(allTanks);
    }

    protected abstract ChemicalRecipeData<HANDLER, CHEMICAL, STACK, TANK> create(List<TANK> tanks);

    protected abstract SubstanceType getSubstanceType();

    protected abstract TANK createTank();

    protected abstract TANK createTank(long capacity, Predicate<@NonNull CHEMICAL> validator);

    protected abstract IChemicalHandlerWrapper<CHEMICAL, STACK> wrap(HANDLER handler);

    protected abstract HANDLER getOutputHandler(List<TANK> tanks);

    protected abstract Capability<HANDLER> getCapability();

    protected abstract Predicate<@NonNull CHEMICAL> cloneValidator(HANDLER handler, int tank);

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (this.tanks.isEmpty()) {
            return true;
        }
        HANDLER handler;
        Optional<HANDLER> capability = MekanismUtils.toOptional(stack.getCapability(getCapability()));
        if (capability.isPresent()) {
            handler = capability.get();
        } else if (stack.getItem() instanceof BlockItem) {
            TileEntityMekanism tile = null;
            Block block = ((BlockItem) stack.getItem()).getBlock();
            if (block instanceof IHasTileEntity<?>) {
                TileEntity tileEntity = ((IHasTileEntity<?>) block).getTileType().create();
                if (tileEntity instanceof TileEntityMekanism) {
                    tile = (TileEntityMekanism) tileEntity;
                }
            }
            if (tile == null || !tile.handles(getSubstanceType())) {
                //Something went wrong
                return false;
            }
            handler = (HANDLER) tile;
        } else {
            return false;
        }
        IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper = wrap(handler);
        int tankCount = wrapper.getTanks();
        if (tankCount == 0) {
            //We don't actually have any tanks in the output
            return true;
        }
        List<TANK> tanks = new ArrayList<>();
        for (int tank = 0; tank < tankCount; tank++) {
            tanks.add(createTank(wrapper.getTankCapacity(tank), cloneValidator(handler, tank)));
        }
        //TODO: Improve the logic used so that it tries to batch similar types of chemicals together first
        // and maybe make it try multiple slot combinations
        IChemicalHandlerWrapper<CHEMICAL, STACK> outputHandler = wrap(getOutputHandler(tanks));
        boolean hasData = false;
        for (TANK tank : this.tanks) {
            if (!tank.isEmpty()) {
                if (!outputHandler.insertChemical(tank.getStack(), Action.EXECUTE).isEmpty()) {
                    //If we have a remainder something failed so bail
                    return false;
                }
                hasData = true;
            }
        }
        if (hasData) {
            //We managed to transfer it all into valid slots, so save it to the stack
            ItemDataUtils.setList(stack, getSubstanceType().getContainerTag(), DataHandlerUtils.writeContainers(tanks));
        }
        return true;
    }
}