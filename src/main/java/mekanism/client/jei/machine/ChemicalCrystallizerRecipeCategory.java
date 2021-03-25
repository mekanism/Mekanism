package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.client.gui.element.custom.GuiCrystallizerScreen;
import mekanism.client.gui.element.custom.GuiCrystallizerScreen.IOreInfo;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;

public class ChemicalCrystallizerRecipeCategory extends BaseRecipeCategory<ChemicalCrystallizerRecipe> {

    //Note: We use a weak hashmap so that when the recipe stops existing either due to disconnecting from the server
    // or because of a reload, then it can be properly garbage collected, but until then we keep track of the pairing
    // between the recipe and the ingredient group JEI has so that we can ensure we draw the correct information
    private final Map<ChemicalCrystallizerRecipe, IGuiIngredientGroup<? extends ChemicalStack<?>>> ingredients = new WeakHashMap<>();
    private final GuiCrystallizerScreen screen;
    private final OreInfo oreInfo;
    private final GuiGauge<?> gauge;
    private final GuiSlot output;

    public ChemicalCrystallizerRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_CRYSTALLIZER, 5, 3, 147, 79);
        screen = addElement(new GuiCrystallizerScreen(this, 31, 13, oreInfo = new OreInfo()));
        gauge = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 4));
        addSlot(SlotType.INPUT, 8, 65).with(SlotOverlay.PLUS);
        output = addSlot(SlotType.OUTPUT, 129, 57);
        addSimpleProgress(ProgressType.LARGE_RIGHT, 53, 61);
    }

    @Override
    public Class<? extends ChemicalCrystallizerRecipe> getRecipeClass() {
        return ChemicalCrystallizerRecipe.class;
    }

    @Override
    public void draw(ChemicalCrystallizerRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our ore info
        IGuiIngredientGroup<? extends ChemicalStack<?>> group = ingredients.get(recipe);
        if (group != null) {
            oreInfo.currentRecipe = recipe;
            oreInfo.ingredient = group.getGuiIngredients().get(0);
        }
        super.draw(recipe, matrixStack, mouseX, mouseY);
        oreInfo.currentRecipe = null;
        oreInfo.ingredient = null;
    }

    @Override
    public void setIngredients(ChemicalCrystallizerRecipe recipe, IIngredients ingredients) {
        IChemicalStackIngredient<?, ?> input = recipe.getInput();
        if (input instanceof GasStackIngredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(((GasStackIngredient) input).getRepresentations()));
        } else if (input instanceof InfusionStackIngredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_INFUSION, Collections.singletonList(((InfusionStackIngredient) input).getRepresentations()));
        } else if (input instanceof PigmentStackIngredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_PIGMENT, Collections.singletonList(((PigmentStackIngredient) input).getRepresentations()));
        } else if (input instanceof SlurryStackIngredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_SLURRY, Collections.singletonList(((SlurryStackIngredient) input).getRepresentations()));
        }
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ChemicalCrystallizerRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        initItem(itemStacks, 0, false, output, recipe.getOutputDefinition());
        IChemicalStackIngredient<?, ?> input = recipe.getInput();
        if (input instanceof GasStackIngredient) {
            initChemical(recipeLayout, recipe, MekanismJEI.TYPE_GAS, (GasStackIngredient) input);
        } else if (input instanceof InfusionStackIngredient) {
            initChemical(recipeLayout, recipe, MekanismJEI.TYPE_INFUSION, (InfusionStackIngredient) input);
        } else if (input instanceof PigmentStackIngredient) {
            initChemical(recipeLayout, recipe, MekanismJEI.TYPE_PIGMENT, (PigmentStackIngredient) input);
        } else if (input instanceof SlurryStackIngredient) {
            SlurryStackIngredient slurryInput = (SlurryStackIngredient) input;
            initChemical(recipeLayout, recipe, MekanismJEI.TYPE_SLURRY, slurryInput);
            Set<ITag<Item>> tags = new HashSet<>();
            for (SlurryStack slurryStack : slurryInput.getRepresentations()) {
                Slurry slurry = slurryStack.getType();
                if (!slurry.isIn(MekanismTags.Slurries.DIRTY)) {
                    ITag<Item> oreTag = slurry.getOreTag();
                    if (oreTag != null) {
                        tags.add(oreTag);
                    }
                }
            }
            if (tags.size() == 1) {
                //TODO: Eventually come up with a better way to do this to allow for if there outputs based on the input and multiple input types
                tags.stream().findFirst().ifPresent(tag -> {
                    itemStacks.init(1, false, screen.getSlotX(), screen.y);
                    itemStacks.set(1, tag.getValues().stream().map(ItemStack::new).collect(Collectors.toList()));
                });
            }
        }
    }

    private <STACK extends ChemicalStack<?>> void initChemical(IRecipeLayout recipeLayout, ChemicalCrystallizerRecipe recipe, IIngredientType<STACK> type,
          IChemicalStackIngredient<?, STACK> ingredient) {
        IGuiIngredientGroup<STACK> stacks = recipeLayout.getIngredientsGroup(type);
        initChemical(stacks, 0, true, gauge, ingredient.getRepresentations());
        this.ingredients.put(recipe, stacks);
    }

    private static class OreInfo implements IOreInfo {

        @Nullable
        private ChemicalCrystallizerRecipe currentRecipe;
        @Nullable
        private IGuiIngredient<? extends ChemicalStack<?>> ingredient;

        @Nonnull
        @Override
        public BoxedChemicalStack getInputChemical() {
            if (ingredient == null) {
                return BoxedChemicalStack.EMPTY;
            }
            ChemicalStack<?> displayedIngredient = ingredient.getDisplayedIngredient();
            return displayedIngredient == null || displayedIngredient.isEmpty() ? BoxedChemicalStack.EMPTY : BoxedChemicalStack.box(displayedIngredient);
        }

        @Nullable
        @Override
        public ChemicalCrystallizerRecipe getRecipe() {
            return currentRecipe;
        }
    }
}