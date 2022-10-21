package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.fluid.MCFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents;
import com.blamejared.crafttweaker.api.recipe.component.DecomposedRecipeBuilder;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.component.IRecipeComponent;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.tag.manager.type.KnownTagManager;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents.ChemicalRecipeComponent;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.MultiFluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.SingleFluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.TaggedFluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator.MultiItemStackIngredient;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator.SingleItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Eventually we might want to try and support replacing but for now it isn't worth it
public abstract class MekanismRecipeHandler<RECIPE extends MekanismRecipe> implements IRecipeHandler<RECIPE> {

    protected static final Object SKIP_OPTIONAL_PARAM = new Object();

    @Override
    public abstract <U extends Recipe<?>> boolean doesConflict(final IRecipeManager<? super RECIPE> manager, final RECIPE recipe, final U other);

    protected <TYPE, INGREDIENT extends InputIngredient<TYPE>> boolean ingredientConflicts(INGREDIENT a, INGREDIENT b) {
        return a.getRepresentations().stream().anyMatch(b::testType);
    }

    @SuppressWarnings("unchecked")
    protected <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean chemicalIngredientConflicts(ChemicalStackIngredient<CHEMICAL, STACK> a,
          ChemicalStackIngredient<?, ?> b) {
        //If types of inputs match then check if they conflict
        return ChemicalType.getTypeFor(a) == ChemicalType.getTypeFor(b) && ingredientConflicts(a, (ChemicalStackIngredient<CHEMICAL, STACK>) b);
    }

    protected String buildCommandString(IRecipeManager<? super RECIPE> manager, RECIPE recipe, Object... params) {
        return buildCommandString(manager, "addRecipe", recipe, params);
    }

    protected String buildCommandString(IRecipeManager<? super RECIPE> manager, String method, RECIPE recipe, Object... params) {
        StringBuilder commandString = new StringBuilder(manager.getCommandString())
              .append('.')
              .append(method)
              .append("(\"")
              //Note: Uses path rather than entire location as we only allow adding recipes to the CrT namespace
              .append(recipe.getId().getPath())
              .append('"');
        for (Object param : params) {
            if (param != SKIP_OPTIONAL_PARAM) {
                commandString.append(", ")
                      .append(convertParam(param));
            }
        }
        return commandString.append(");").toString();
    }

    /**
     * Super simplified/watered down version of BaseCrTExampleProvider#getConversionRepresentations
     */
    private String convertParam(Object param) {
        if (param instanceof ItemStack stack) {
            return ItemStackUtil.getCommandString(stack);
        } else if (param instanceof FluidStack stack) {
            return new MCFluidStack(stack).getCommandString();
        } else if (param instanceof GasStack stack) {
            return new CrTGasStack(stack).getCommandString();
        } else if (param instanceof InfusionStack stack) {
            return new CrTInfusionStack(stack).getCommandString();
        } else if (param instanceof PigmentStack stack) {
            return new CrTPigmentStack(stack).getCommandString();
        } else if (param instanceof SlurryStack stack) {
            return new CrTSlurryStack(stack).getCommandString();
        } else if (param instanceof BoxedChemicalStack stack) {
            return convertParam(stack.getChemicalStack());
        } else if (param instanceof FloatingLong fl) {
            //Note: Handled via implicit casts
            if (fl.getDecimal() == 0) {
                //No decimal, don't bother printing it
                return fl.toString(0);
            }
            //Trim any trailing zeros rather than printing them out
            return fl.toString().replaceAll("0*$", "");
        } else if (param instanceof Number || param instanceof Boolean) {//Handle integers and the like
            return param.toString();
        } else if (param instanceof ItemStackIngredient ingredient) {
            return convertIngredient(ingredient);
        } else if (param instanceof FluidStackIngredient ingredient) {
            return convertIngredient(ingredient);
        } else if (param instanceof GasStackIngredient ingredient) {
            return convertIngredient(CrTConstants.CLASS_GAS_STACK_INGREDIENT, CrTUtils.gasTags(), ChemicalIngredientDeserializer.GAS, ingredient);
        } else if (param instanceof InfusionStackIngredient ingredient) {
            return convertIngredient(CrTConstants.CLASS_INFUSION_STACK_INGREDIENT, CrTUtils.infuseTypeTags(), ChemicalIngredientDeserializer.INFUSION, ingredient);
        } else if (param instanceof PigmentStackIngredient ingredient) {
            return convertIngredient(CrTConstants.CLASS_PIGMENT_STACK_INGREDIENT, CrTUtils.pigmentTags(), ChemicalIngredientDeserializer.PIGMENT, ingredient);
        } else if (param instanceof SlurryStackIngredient ingredient) {
            return convertIngredient(CrTConstants.CLASS_SLURRY_STACK_INGREDIENT, CrTUtils.slurryTags(), ChemicalIngredientDeserializer.SLURRY, ingredient);
        } else if (param instanceof List<?> list) {
            if (list.isEmpty()) {
                //Shouldn't happen
                return "Invalid (output) list, no outputs";
            }
            //Outputs sometimes are as lists, try wrapping them into a single element
            // eventually we may want to try listing them all somehow?
            return convertParam(list.get(0));
        } else if (param instanceof ElectrolysisRecipeOutput output) {
            return convertParam(output.left()) + ", " + convertParam(output.right());
        }
        //Shouldn't happen
        return "Unimplemented: " + param;
    }

    @Nullable
    public static String basicImplicitIngredient(Ingredient vanillaIngredient, int amount, JsonElement serialized) {
        return basicImplicitIngredient(vanillaIngredient, amount, serialized, true);
    }

    @Nullable
    public static String basicImplicitIngredient(Ingredient vanillaIngredient, int amount, JsonElement serialized, boolean handleTags) {
        if (serialized.isJsonObject()) {
            JsonObject serializedIngredient = serialized.getAsJsonObject();
            if (vanillaIngredient.isVanilla()) {
                if (serializedIngredient.has(JsonConstants.ITEM)) {
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(serializedIngredient.get(JsonConstants.ITEM).getAsString()));
                    return ItemStackUtil.getCommandString(new ItemStack(item, amount));
                } else if (handleTags && serializedIngredient.has(JsonConstants.TAG)) {
                    KnownTag<Item> tag = CrTUtils.itemTags().tag(serializedIngredient.get(JsonConstants.TAG).getAsString());
                    return amount == 1 ? tag.getCommandString() : tag.withAmount(amount).getCommandString();
                }
            } else if (vanillaIngredient instanceof StrictNBTIngredient) {
                ItemStack stack = CraftingHelper.getItemStack(serializedIngredient, true);
                stack.setCount(amount);
                return ItemStackUtil.getCommandString(stack);
            }
        }
        return null;
    }

    private String convertIngredient(ItemStackIngredient ingredient) {
        if (ingredient instanceof SingleItemStackIngredient single) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            Ingredient vanillaIngredient = single.getInputRaw();
            int amount = GsonHelper.getAsInt(serialized, JsonConstants.AMOUNT, 1);
            String rep = basicImplicitIngredient(vanillaIngredient, amount, serialized.get(JsonConstants.INGREDIENT));
            if (rep == null) {
                rep = IIngredient.fromIngredient(vanillaIngredient).getCommandString();
                if (amount > 1) {
                    return CrTConstants.CLASS_ITEM_STACK_INGREDIENT + ".from(" + rep + ", " + amount + ")";
                }
            }
            //Note: Handled via implicit casts
            return rep;
        } else if (ingredient instanceof MultiItemStackIngredient multiIngredient) {
            return convertMultiIngredient(CrTConstants.CLASS_ITEM_STACK_INGREDIENT, multiIngredient, this::convertIngredient);
        }
        //Shouldn't happen
        return "Unimplemented itemstack ingredient: " + ingredient;
    }

    private String convertIngredient(FluidStackIngredient ingredient) {
        if (ingredient instanceof SingleFluidStackIngredient) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            //Note: Handled via implicit casts
            return new MCFluidStack(SerializerHelper.deserializeFluid(serialized)).getCommandString();
        } else if (ingredient instanceof TaggedFluidStackIngredient) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            //Note: Handled via implicit casts
            return CrTUtils.fluidTags().tag(serialized.get(JsonConstants.TAG).getAsString())
                  .withAmount(serialized.getAsJsonPrimitive(JsonConstants.AMOUNT).getAsInt()).getCommandString();
        } else if (ingredient instanceof MultiFluidStackIngredient multiIngredient) {
            return convertMultiIngredient(CrTConstants.CLASS_FLUID_STACK_INGREDIENT, multiIngredient, this::convertIngredient);
        }
        return "Unimplemented fluidstack ingredient: " + ingredient;
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> String convertIngredient(String crtClass,
          KnownTagManager<CHEMICAL> tagManager, ChemicalIngredientDeserializer<CHEMICAL, STACK, ?> deserializer,
          ChemicalStackIngredient<CHEMICAL, STACK> ingredient) {
        if (ingredient instanceof SingleChemicalStackIngredient) {
            //Serialize and deserialize to get easy access to the amount
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            //Note: Handled via implicit casts
            return convertParam(deserializer.deserializeStack(serialized));
        } else if (ingredient instanceof TaggedChemicalStackIngredient) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            KnownTag<CHEMICAL> tag = tagManager.tag(serialized.get(JsonConstants.TAG).getAsString());
            long amount = serialized.getAsJsonPrimitive(JsonConstants.AMOUNT).getAsLong();
            if (amount > 0 && amount <= Integer.MAX_VALUE) {
                //Note: Handled via implicit casts
                return tag.withAmount((int) amount).getCommandString();
            }
            //Tag with amount can only handle up to max int, so we have to do it explicitly if we have more
            return crtClass + ".from(" + tag.getCommandString() + ", " + amount + ")";
        } else if (ingredient instanceof MultiChemicalStackIngredient<CHEMICAL, STACK, ?> multiIngredient) {
            return convertMultiIngredient(crtClass, multiIngredient, i -> convertIngredient(crtClass, tagManager, deserializer, i));
        }
        //Shouldn't happen
        return "Unimplemented chemical stack ingredient: " + ingredient;
    }

    private <TYPE, INGREDIENT extends InputIngredient<@NotNull TYPE>> String convertMultiIngredient(String crtClass, IMultiIngredient<TYPE, INGREDIENT> multiIngredient,
          Function<INGREDIENT, String> converter) {
        StringBuilder builder = new StringBuilder(crtClass + ".createMulti(");
        multiIngredient.forEachIngredient(i -> {
            builder.append(converter.apply(i)).append(", ");
            return false;
        });
        //Remove trailing comma and space
        builder.setLength(builder.length() - 2);
        builder.append(")");
        return builder.toString();
    }

    /**
     * Helper to generically decompose data into the proper recipe components.
     */
    protected Optional<IDecomposedRecipe> decompose(Object... importantData) {
        TypeData<ItemStackIngredient, FluidStackIngredient, ChemicalStackIngredient<?, ?>> inputs = new TypeData<>(ChemicalType::getTypeFor);
        TypeData<IItemStack, IFluidStack, ChemicalStack<?>> outputs = new TypeData<>(ChemicalType::getTypeFor);
        int duration = -1;
        FloatingLong energy = null;
        for (Object data : importantData) {
            if (data instanceof List<?> dataList) {
                if (dataList.size() != 1) {
                    //Failed, output lists must be of length one or handled manually instead of using this helper
                    return Optional.empty();
                }
                //Update data to be the element
                data = dataList.get(0);
            }
            if (data instanceof ItemStackIngredient ingredient) {
                inputs.addItem(ingredient);
            } else if (data instanceof FluidStackIngredient ingredient) {
                inputs.addFluid(ingredient);
            } else if (data instanceof ChemicalStackIngredient<?, ?> ingredient) {
                inputs.addChemical(ingredient);
            } else if (data instanceof ItemStack stack) {
                outputs.addItem(new MCItemStack(stack));
            } else if (data instanceof FluidStack stack) {
                outputs.addFluid(new MCFluidStack(stack));
            } else if (data instanceof ChemicalStack<?> stack) {
                outputs.addChemical(stack);
            } else if (data instanceof BoxedChemicalStack stack) {
                outputs.addChemical(stack.getChemicalStack());
            } else if (data instanceof PressurizedReactionRecipeOutput output) {
                if (!output.item().isEmpty()) {
                    outputs.addItem(new MCItemStack(output.item()));
                }
                if (!output.gas().isEmpty()) {
                    outputs.addChemical(output.gas());
                }
            } else if (data instanceof ElectrolysisRecipeOutput output) {
                outputs.addChemical(output.left());
                outputs.addChemical(output.right());
            } else if (data instanceof Integer i) {
                if (duration != -1) {
                    //Fail if we have multiple durations specified
                    return Optional.empty();
                }
                duration = i;
            } else if (data instanceof FloatingLong fl) {
                if (energy != null) {
                    //Fail if we have multiple energy values specified
                    return Optional.empty();
                }
                energy = fl;
            } else {
                //Fail if we have important data we don't know how to handle
                return Optional.empty();
            }
        }
        DecomposedRecipeBuilder builder = IDecomposedRecipe.builder();
        inputs.addItemToBuilder(builder, CrTRecipeComponents.ITEM.input())
              .addFluidToBuilder(builder, CrTRecipeComponents.FLUID.input());
        outputs.addItemToBuilder(builder, CrTRecipeComponents.ITEM.output())
              .addFluidToBuilder(builder, CrTRecipeComponents.FLUID.output());
        for (ChemicalRecipeComponent<?, ?, ?, ?> chemicalComponent : CrTRecipeComponents.CHEMICAL_COMPONENTS) {
            addChemicals(builder, inputs, outputs, chemicalComponent);
        }
        if (duration != -1) {
            builder.with(BuiltinRecipeComponents.Processing.TIME, duration);
        }
        if (energy != null) {
            builder.with(CrTRecipeComponents.ENERGY, energy);
        }
        return Optional.of(builder.build());
    }

    private <STACK extends ChemicalStack<?>, INGREDIENT extends ChemicalStackIngredient<?, STACK>, CRT_STACK extends ICrTChemicalStack<?, STACK, CRT_STACK>>
    void addChemicals(DecomposedRecipeBuilder builder, TypeData<?, ?, ChemicalStackIngredient<?, ?>> inputs, TypeData<?, ?, ChemicalStack<?>> outputs,
          ChemicalRecipeComponent<?, STACK, INGREDIENT, CRT_STACK> component) {
        List<INGREDIENT> data = (List<INGREDIENT>) inputs.chemicalData.getOrDefault(component.chemicalType(), Collections.emptyList());
        if (!data.isEmpty()) {
            builder.with(component.input(), data);
        }
        List<STACK> outputData = (List<STACK>) outputs.chemicalData.getOrDefault(component.chemicalType(), Collections.emptyList());
        if (!outputData.isEmpty()) {
            component.withOutput(builder, outputData);
        }
    }

    private static class TypeData<ITEM, FLUID, CHEMICAL> {

        private final List<ITEM> itemData = new ArrayList<>();
        private final List<FLUID> fluidData = new ArrayList<>();
        private final Map<ChemicalType, List<CHEMICAL>> chemicalData = new EnumMap<>(ChemicalType.class);
        private final Function<CHEMICAL, ChemicalType> typeExtractor;

        public TypeData(Function<CHEMICAL, ChemicalType> typeExtractor) {
            this.typeExtractor = typeExtractor;
        }

        private void addItem(ITEM data) {
            itemData.add(data);
        }

        private void addFluid(FLUID data) {
            fluidData.add(data);
        }

        private void addChemical(CHEMICAL data) {
            chemicalData.computeIfAbsent(typeExtractor.apply(data), type -> new ArrayList<>()).add(data);
        }

        private TypeData<ITEM, FLUID, CHEMICAL> addItemToBuilder(DecomposedRecipeBuilder builder, IRecipeComponent<ITEM> component) {
            if (!itemData.isEmpty()) {
                builder.with(component, itemData);
            }
            return this;
        }

        private TypeData<ITEM, FLUID, CHEMICAL> addFluidToBuilder(DecomposedRecipeBuilder builder, IRecipeComponent<FLUID> component) {
            if (!fluidData.isEmpty()) {
                builder.with(component, fluidData);
            }
            return this;
        }
    }
}