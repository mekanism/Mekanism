package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import com.blamejared.crafttweaker.impl.fluid.MCFluidStack;
import com.blamejared.crafttweaker.impl.helper.ItemStackHelper;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.manager.TagManagerFluid;
import com.blamejared.crafttweaker.impl.tag.manager.TagManagerItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import javax.annotation.Nullable;
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
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.InputIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.tag.CrTChemicalTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTGasTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTInfuseTypeTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTPigmentTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTSlurryTagManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

//TODO: Eventually we might want to try and support replacing but for now it isn't worth it
public abstract class MekanismRecipeHandler<RECIPE extends MekanismRecipe> implements IRecipeHandler<RECIPE> {

    protected static final Object SKIP_OPTIONAL_PARAM = new Object();

    @Override
    public abstract <U extends IRecipe<?>> boolean doesConflict(final IRecipeManager manager, final RECIPE recipe, final U other);

    protected <TYPE, INGREDIENT extends InputIngredient<TYPE>> boolean ingredientConflicts(INGREDIENT a, INGREDIENT b) {
        return a.getRepresentations().stream().anyMatch(b::testType);
    }

    @SuppressWarnings("unchecked")
    protected <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean chemicalIngredientConflicts(IChemicalStackIngredient<CHEMICAL, STACK> a,
          IChemicalStackIngredient<?, ?> b) {
        //If types of inputs match then check if they conflict
        return ChemicalType.getTypeFor(a) == ChemicalType.getTypeFor(b) && ingredientConflicts(a, (IChemicalStackIngredient<CHEMICAL, STACK>) b);
    }

    protected String buildCommandString(IRecipeManager manager, RECIPE recipe, Object... params) {
        return buildCommandString(manager, "addRecipe", recipe, params);
    }

    protected String buildCommandString(IRecipeManager manager, String method, RECIPE recipe, Object... params) {
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
        if (param instanceof ItemStack) {
            return ItemStackHelper.getCommandString((ItemStack) param);
        } else if (param instanceof FluidStack) {
            return new MCFluidStack((FluidStack) param).getCommandString();
        } else if (param instanceof GasStack) {
            return new CrTGasStack((GasStack) param).getCommandString();
        } else if (param instanceof InfusionStack) {
            return new CrTInfusionStack((InfusionStack) param).getCommandString();
        } else if (param instanceof PigmentStack) {
            return new CrTPigmentStack((PigmentStack) param).getCommandString();
        } else if (param instanceof SlurryStack) {
            return new CrTSlurryStack((SlurryStack) param).getCommandString();
        } else if (param instanceof BoxedChemicalStack) {
            return convertParam(((BoxedChemicalStack) param).getChemicalStack());
        } else if (param instanceof FloatingLong) {
            FloatingLong fl = (FloatingLong) param;
            //Note: Handled via implicit casts
            if (fl.getDecimal() == 0) {
                //No decimal, don't bother printing it
                return fl.toString(0);
            }
            //Trim any trailing zeros rather than printing them out
            return fl.toString().replaceAll("0*$", "");
        } else if (param instanceof Number || param instanceof Boolean) {//Handle integers and the like
            return param.toString();
        } else if (param instanceof ItemStackIngredient) {
            return convertIngredient((ItemStackIngredient) param);
        } else if (param instanceof FluidStackIngredient) {
            return convertIngredient((FluidStackIngredient) param);
        } else if (param instanceof GasStackIngredient) {
            return convertIngredient(CrTConstants.CLASS_GAS_STACK_INGREDIENT, CrTGasTagManager.INSTANCE, ChemicalIngredientDeserializer.GAS, (GasStackIngredient) param);
        } else if (param instanceof InfusionStackIngredient) {
            return convertIngredient(CrTConstants.CLASS_INFUSION_STACK_INGREDIENT, CrTInfuseTypeTagManager.INSTANCE, ChemicalIngredientDeserializer.INFUSION,
                  (InfusionStackIngredient) param);
        } else if (param instanceof PigmentStackIngredient) {
            return convertIngredient(CrTConstants.CLASS_PIGMENT_STACK_INGREDIENT, CrTPigmentTagManager.INSTANCE, ChemicalIngredientDeserializer.PIGMENT,
                  (PigmentStackIngredient) param);
        } else if (param instanceof SlurryStackIngredient) {
            return convertIngredient(CrTConstants.CLASS_SLURRY_STACK_INGREDIENT, CrTSlurryTagManager.INSTANCE, ChemicalIngredientDeserializer.SLURRY,
                  (SlurryStackIngredient) param);
        } else if (param instanceof List) {
            List<?> list = (List<?>) param;
            if (list.isEmpty()) {
                //Shouldn't happen
                return "Invalid (output) list, no outputs";
            }
            //Outputs sometimes are as lists, try wrapping them into a single element
            // eventually we may want to try listing them all somehow?
            return convertParam(list.get(0));
        }
        //Shouldn't happen
        return "Unimplemented: " + param;
    }

    private String getTagWithExplicitAmount(MCTag<?> tag, int amount) {
        //Explicitly include the amount rather than doing tag.withAmount(amount).getCommandString() as we want to make the values
        // that need an amount specified to be able to be implicit, have them
        return tag.getCommandString() + " * " + amount;
    }

    @Nullable
    public static String basicImplicitIngredient(Ingredient vanillaIngredient, int amount, JsonElement serialized) {
        if (serialized.isJsonObject()) {
            JsonObject serializedIngredient = serialized.getAsJsonObject();
            if (vanillaIngredient.isVanilla()) {
                if (serializedIngredient.has(JsonConstants.ITEM)) {
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(serializedIngredient.get(JsonConstants.ITEM).getAsString()));
                    return ItemStackHelper.getCommandString(new ItemStack(item, amount));
                } else if (serializedIngredient.has(JsonConstants.TAG)) {
                    return TagManagerItem.INSTANCE.getTag(serializedIngredient.get(JsonConstants.TAG).getAsString()).withAmount(amount).getCommandString();
                }
            } else if (vanillaIngredient instanceof NBTIngredient) {
                ItemStack stack = CraftingHelper.getItemStack(serializedIngredient, true);
                stack.setCount(amount);
                return ItemStackHelper.getCommandString(stack);
            }
        }
        return null;
    }

    private String convertIngredient(ItemStackIngredient ingredient) {
        if (ingredient instanceof ItemStackIngredient.Single) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            Ingredient vanillaIngredient = ((ItemStackIngredient.Single) ingredient).getInputRaw();
            int amount = JSONUtils.getAsInt(serialized, JsonConstants.AMOUNT, 1);
            String rep = basicImplicitIngredient(vanillaIngredient, amount, serialized.get(JsonConstants.INGREDIENT));
            if (rep == null) {
                rep = IIngredient.fromIngredient(vanillaIngredient).getCommandString();
                if (amount > 1) {
                    return CrTConstants.CLASS_ITEM_STACK_INGREDIENT + ".from(" + rep + ", " + amount + ")";
                }
            }
            //Note: Handled via implicit casts
            return rep;
        } else if (ingredient instanceof ItemStackIngredient.Multi) {
            ItemStackIngredient.Multi multiIngredient = (ItemStackIngredient.Multi) ingredient;
            StringBuilder builder = new StringBuilder(CrTConstants.CLASS_ITEM_STACK_INGREDIENT + ".createMulti(");
            multiIngredient.forEachIngredient(i -> {
                builder.append(convertIngredient(i)).append(", ");
                return false;
            });
            //Remove trailing comma and space
            builder.setLength(builder.length() - 2);
            builder.append(")");
            return builder.toString();
        }
        //Shouldn't happen
        return "Unimplemented itemstack ingredient: " + ingredient;
    }

    private String convertIngredient(FluidStackIngredient ingredient) {
        if (ingredient instanceof FluidStackIngredient.Single) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            //Note: Handled via implicit casts
            return new MCFluidStack(SerializerHelper.deserializeFluid(serialized)).getCommandString();
        } else if (ingredient instanceof FluidStackIngredient.Tagged) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            //Note: Handled via implicit casts
            return getTagWithExplicitAmount(TagManagerFluid.INSTANCE.getTag(serialized.get(JsonConstants.TAG).getAsString()),
                  serialized.getAsJsonPrimitive(JsonConstants.AMOUNT).getAsInt());
        } else if (ingredient instanceof FluidStackIngredient.Multi) {
            FluidStackIngredient.Multi multiIngredient = (FluidStackIngredient.Multi) ingredient;
            StringBuilder builder = new StringBuilder(CrTConstants.CLASS_FLUID_STACK_INGREDIENT + ".createMulti(");
            multiIngredient.forEachIngredient(i -> {
                builder.append(convertIngredient(i)).append(", ");
                return false;
            });
            //Remove trailing comma and space
            builder.setLength(builder.length() - 2);
            builder.append(")");
            return builder.toString();
        }
        return "Unimplemented fluidstack ingredient: " + ingredient;
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> String convertIngredient(String crtClass,
          CrTChemicalTagManager<CHEMICAL> tagManager, ChemicalIngredientDeserializer<CHEMICAL, STACK, ?> deserializer,
          IChemicalStackIngredient<CHEMICAL, STACK> ingredient) {
        if (ingredient instanceof ChemicalStackIngredient.SingleIngredient) {
            //Serialize and deserialize to get easy access to the amount
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            //Note: Handled via implicit casts
            return convertParam(deserializer.deserializeStack(serialized));
        } else if (ingredient instanceof ChemicalStackIngredient.TaggedIngredient) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            MCTag<CHEMICAL> tag = tagManager.getTag(serialized.get(JsonConstants.TAG).getAsString());
            long amount = serialized.getAsJsonPrimitive(JsonConstants.AMOUNT).getAsLong();
            if (amount > 0 && amount <= Integer.MAX_VALUE) {
                //Note: Handled via implicit casts
                return getTagWithExplicitAmount(tag, (int) amount);
            }
            //Tag with amount can only handle up to max int, so we have to do it explicitly if we have more
            return crtClass + ".from(" + tag.getCommandString() + ", " + amount + ")";
        } else if (ingredient instanceof ChemicalStackIngredient.MultiIngredient) {
            ChemicalStackIngredient.MultiIngredient<CHEMICAL, STACK, ?> multiIngredient = (ChemicalStackIngredient.MultiIngredient<CHEMICAL, STACK, ?>) ingredient;
            StringBuilder builder = new StringBuilder(crtClass + ".createMulti(");
            multiIngredient.forEachIngredient(i -> {
                builder.append(convertIngredient(crtClass, tagManager, deserializer, i)).append(", ");
                return false;
            });
            //Remove trailing comma and space
            builder.setLength(builder.length() - 2);
            builder.append(")");
            return builder.toString();
        }
        //Shouldn't happen
        return "Unimplemented chemical stack ingredient: " + ingredient;
    }
}