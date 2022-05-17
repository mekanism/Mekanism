package mekanism.common.recipe.upgrade.chemical;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.recipe.upgrade.RecipeUpgradeData;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalRecipeData<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>,
      HANDLER extends IChemicalHandler<CHEMICAL, STACK>> implements RecipeUpgradeData<ChemicalRecipeData<CHEMICAL, STACK, TANK, HANDLER>> {

    protected final List<TANK> tanks;

    protected ChemicalRecipeData(ListTag tanks) {
        int count = DataHandlerUtils.getMaxId(tanks, NBTConstants.TANK);
        this.tanks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.tanks.add(getTankBuilder().createDummy(Long.MAX_VALUE));
        }
        DataHandlerUtils.readContainers(this.tanks, tanks);
    }

    protected ChemicalRecipeData(List<TANK> tanks) {
        this.tanks = tanks;
    }

    @Nullable
    @Override
    public ChemicalRecipeData<CHEMICAL, STACK, TANK, HANDLER> merge(ChemicalRecipeData<CHEMICAL, STACK, TANK, HANDLER> other) {
        List<TANK> allTanks = new ArrayList<>(tanks.size() + other.tanks.size());
        allTanks.addAll(tanks);
        allTanks.addAll(other.tanks);
        return create(allTanks);
    }

    protected abstract ChemicalRecipeData<CHEMICAL, STACK, TANK, HANDLER> create(List<TANK> tanks);

    protected abstract SubstanceType getSubstanceType();

    protected abstract ChemicalTankBuilder<CHEMICAL, STACK, TANK> getTankBuilder();

    protected abstract HANDLER getOutputHandler(List<TANK> tanks);

    protected abstract Capability<HANDLER> getCapability();

    protected abstract Predicate<@NonNull CHEMICAL> cloneValidator(HANDLER handler, int tank);

    protected abstract HANDLER getHandlerFromTile(TileEntityMekanism tile);

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (this.tanks.isEmpty()) {
            return true;
        }
        HANDLER handler;
        Optional<HANDLER> capability = stack.getCapability(getCapability()).resolve();
        if (capability.isPresent()) {
            handler = capability.get();
        } else if (stack.getItem() instanceof BlockItem blockItem) {
            TileEntityMekanism tile = null;
            Block block = blockItem.getBlock();
            if (block instanceof IHasTileEntity<?> hasTileEntity) {
                BlockEntity tileEntity = hasTileEntity.createDummyBlockEntity();
                if (tileEntity instanceof TileEntityMekanism) {
                    tile = (TileEntityMekanism) tileEntity;
                }
            }
            if (tile == null || !tile.handles(getSubstanceType())) {
                //Something went wrong
                return false;
            }
            handler = getHandlerFromTile(tile);
        } else {
            return false;
        }
        int tankCount = handler.getTanks();
        if (tankCount == 0) {
            //We don't actually have any tanks in the output
            return true;
        }
        List<TANK> tanks = new ArrayList<>();
        for (int tank = 0; tank < tankCount; tank++) {
            //TODO: Do we need to also clone the attribute validator
            tanks.add(getTankBuilder().create(handler.getTankCapacity(tank), cloneValidator(handler, tank), null));
        }
        //TODO: Improve the logic used so that it tries to batch similar types of chemicals together first
        // and maybe make it try multiple slot combinations
        HANDLER outputHandler = getOutputHandler(tanks);
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
            ItemDataUtils.writeContainers(stack, getSubstanceType().getContainerTag(), tanks);
        }
        return true;
    }
}