package mekanism.api.recipes.inputs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.infuse.InfuseObject;
import mekanism.common.InfuseStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandler;

public class InputHelper {

    //TODO: 1.14, evaluate using the IItemHandler variant instead
    public static IInputHandler<@NonNull ItemStack> getInputHandler(@Nonnull NonNullList<ItemStack> inventory, int slot) {
        return new IInputHandler<@NonNull ItemStack>() {

            @Override
            public @NonNull ItemStack getInput() {
                return inventory.get(slot);
            }

            @Override
            public @NonNull ItemStack getRecipeInput(InputIngredient<@NonNull ItemStack> recipeIngredient) {
                ItemStack input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return ItemStack.EMPTY;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@NonNull ItemStack recipeInput, int operations) {
                if (!recipeInput.isEmpty()) {
                    //TODO: Should this be done in some other way than shrink, such as via an IItemHandler, 1.14
                    //TODO: If this would make the stack empty, we should just set the inventory slot to empty instead of using shrink
                    getInput().shrink(recipeInput.getCount() * operations);
                }
            }

            @Override
            public int operationsCanSupport(InputIngredient<@NonNull ItemStack> recipeIngredient, int currentMax) {
                if (currentMax == 0) {
                    //Short circuit that if we already can't perform any operations, just return
                    return 0;
                }
                ItemStack recipeInput = getRecipeInput(recipeIngredient);
                if (recipeInput.isEmpty()) {
                    //If the input is empty that means there is no ingredient that matches
                    return 0;
                }
                return Math.min(getInput().getCount() / recipeInput.getCount(), currentMax);
            }
        };
    }

    public static IInputHandler<@NonNull ItemStack> getInputHandler(@Nonnull IItemHandler inventory, int slot) {
        return new IInputHandler<@NonNull ItemStack>() {

            @Override
            public @NonNull ItemStack getInput() {
                return inventory.getStackInSlot(slot);
            }

            @Override
            public @NonNull ItemStack getRecipeInput(InputIngredient<@NonNull ItemStack> recipeIngredient) {
                ItemStack input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return ItemStack.EMPTY;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@NonNull ItemStack recipeInput, int operations) {
                if (!recipeInput.isEmpty()) {
                    //TODO: Should we check if it failed
                    inventory.extractItem(slot, recipeInput.getCount() * operations, false);
                }
            }

            @Override
            public int operationsCanSupport(InputIngredient<@NonNull ItemStack> recipeIngredient, int currentMax) {
                if (currentMax == 0) {
                    //Short circuit that if we already can't perform any operations, just return
                    return 0;
                }
                ItemStack recipeInput = getRecipeInput(recipeIngredient);
                if (recipeInput.isEmpty()) {
                    //If the input is empty that means there is no ingredient that matches
                    return 0;
                }
                return Math.min(getInput().getCount() / recipeInput.getCount(), currentMax);
            }
        };
    }

    public static IInputHandler<@NonNull GasStack> getInputHandler(@Nonnull GasTank gasTank) {
        return new IInputHandler<@NonNull GasStack>() {

            //TODO: 1.14, remove this nullable annotation and instead make it so that tank.getGas returns an empty gas stack if it is empty
            @Nullable
            @Override
            public @NonNull GasStack getInput() {
                return gasTank.getGas();
            }

            @Nullable
            @Override
            public @NonNull GasStack getRecipeInput(InputIngredient<@NonNull GasStack> recipeIngredient) {
                GasStack input = getInput();
                //TODO: Make this be an is empty check, and return empty instead of null
                if (input == null) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return null;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@NonNull GasStack recipeInput, int operations) {
                if (recipeInput == null || recipeInput.amount == 0) {
                    //Something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                GasStack inputGas = getInput();
                if (inputGas == null || inputGas.amount == 0) {
                    //Something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                gasTank.draw(recipeInput.amount * operations, true);
            }

            @Override
            public int operationsCanSupport(InputIngredient<@NonNull GasStack> recipeIngredient, int currentMax) {
                if (currentMax == 0) {
                    //Short circuit that if we already can't perform any operations, just return
                    return 0;
                }
                GasStack recipeInput = getRecipeInput(recipeIngredient);
                //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
                if (recipeInput == null || recipeInput.amount == 0) {
                    //If the input is empty that means there is no ingredient that matches
                    return 0;
                }
                //TODO: 1.14, inline this getInput().getAmount() call
                GasStack inputGas = getInput();
                if (inputGas == null || inputGas.amount == 0) {
                    return 0;
                }
                return Math.min(inputGas.amount / recipeInput.amount, currentMax);
            }
        };
    }

    public static IInputHandler<@NonNull FluidStack> getInputHandler(@Nonnull FluidTank fluidTank) {
        return new IInputHandler<@NonNull FluidStack>() {

            //TODO: 1.14, remove this nullable annotation and instead make it so that tank.getGas returns an empty fluid stack if it is empty
            @Nullable
            @Override
            public @NonNull FluidStack getInput() {
                return fluidTank.getFluid();
            }

            @Nullable
            @Override
            public @NonNull FluidStack getRecipeInput(InputIngredient<@NonNull FluidStack> recipeIngredient) {
                FluidStack input = getInput();
                //TODO: Make this be an is empty check, and return empty instead of null
                if (input == null) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return null;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@NonNull FluidStack recipeInput, int operations) {
                if (recipeInput == null || recipeInput.amount == 0) {
                    //Something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                FluidStack inputFluid = getInput();
                if (inputFluid == null || inputFluid.amount == 0) {
                    //Something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                fluidTank.drain(recipeInput.amount * operations, true);
            }

            @Override
            public int operationsCanSupport(InputIngredient<@NonNull FluidStack> recipeIngredient, int currentMax) {
                if (currentMax == 0) {
                    //Short circuit that if we already can't perform any operations, just return
                    return 0;
                }
                FluidStack recipeInput = getRecipeInput(recipeIngredient);
                //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
                if (recipeInput == null || recipeInput.amount == 0) {
                    //If the input is empty that means there is no ingredient that matches
                    return 0;
                }
                //TODO: 1.14, inline this getInput().getAmount() call
                FluidStack inputFluid = getInput();
                if (inputFluid == null || inputFluid.amount == 0) {
                    return 0;
                }
                //TODO: Simulate the drain?
                return Math.min(inputFluid.amount / recipeInput.amount, currentMax);
            }
        };
    }

    public static IInputHandler<@NonNull InfuseObject> getInputHandler(@Nonnull InfuseStorage infuseStorage) {
        return new IInputHandler<@NonNull InfuseObject>() {

            //TODO: 1.14, remove this nullable annotation and instead make it so that tank.getGas returns an empty infuse stack if it is empty
            @Nullable
            @Override
            public @NonNull InfuseObject getInput() {
                if (infuseStorage.isEmpty()) {
                    return null;
                }
                return new InfuseObject(infuseStorage.getType(), infuseStorage.getAmount());
            }

            @Nullable
            @Override
            public @NonNull InfuseObject getRecipeInput(InputIngredient<@NonNull InfuseObject> recipeIngredient) {
                InfuseObject input = getInput();
                //TODO: Make this be an is empty check, and return empty instead of null
                if (input == null) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return null;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@NonNull InfuseObject recipeInput, int operations) {
                if (infuseStorage.isEmpty()) {
                    //Something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                infuseStorage.subtract(new InfuseObject(recipeInput.type, recipeInput.getAmount() * operations));
            }

            @Override
            public int operationsCanSupport(InputIngredient<@NonNull InfuseObject> recipeIngredient, int currentMax) {
                if (currentMax == 0 || infuseStorage.isEmpty()) {
                    //Short circuit that if we already can't perform any operations, just return
                    return 0;
                }
                InfuseObject recipeInput = getRecipeInput(recipeIngredient);
                //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
                if (recipeInput == null || recipeInput.isEmpty()) {
                    //If the input is empty that means there is no ingredient that matches
                    return 0;
                }
                //TODO: 1.14, inline this getInput().getAmount() call
                InfuseObject inputInfusion = getInput();
                if (inputInfusion == null || inputInfusion.isEmpty()) {
                    return 0;
                }
                //TODO: Simulate the drain?
                return Math.min(inputInfusion.getAmount() / recipeInput.getAmount(), currentMax);
            }
        };
    }
}