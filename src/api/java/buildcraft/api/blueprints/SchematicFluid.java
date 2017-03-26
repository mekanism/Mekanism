/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import net.minecraft.block.BlockLiquid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;

@Deprecated
public class SchematicFluid extends SchematicBlock {

    private final ItemStack fluidItem;

    public SchematicFluid(FluidStack fluidStack) {
        this.fluidItem = new ItemStack(fluidStack.getFluid().getBlock(), 1);
    }

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, NonNullList<ItemStack> requirements) {
        if (getLevel() == 0) {
            requirements.add(fluidItem);
        }
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        // cancel requirements reading
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        if (getLevel() == 0) {
            return state == context.world().getBlockState(pos) && (context.world().getBlockState(pos).getValue(BlockLiquid.LEVEL)) == 0;
        } else {
            return state == context.world().getBlockState(pos);
        }
    }

    @Override
    public void rotateLeft(IBuilderContext context) {

    }

    @Override
    public boolean doNotBuild() {
        return getLevel() != 0;
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, NonNullList<ItemStack> stacks) {
        if (getLevel() == 0) {
            context.world().setBlockState(pos, state, 3);
        }
    }

    @Override
    public void postProcessing(IBuilderContext context, BlockPos pos) {
        if (getLevel() != 0) {
            context.world().setBlockState(pos, state, 3);
        }
    }

    @Override
    public NonNullList<ItemStack> getStacksToDisplay(NonNullList<ItemStack> stackConsumed) {
        NonNullList<ItemStack> result = NonNullList.create();
        result.add(fluidItem);
        return result;
    }

    @Override
    public int getEnergyRequirement(NonNullList<ItemStack> stacksUsed) {
        return 1;// * BuilderAPI.BUILD_ENERGY;
    }

    public int getLevel() {
        return state.getValue(BlockLiquid.LEVEL);
    }
}
