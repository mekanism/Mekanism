package mekanism.client.recipe_viewer.jei.machine;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe.FluidOptionalItemOutput;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.BaseRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.RegistryUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

public class ItemStackToFluidOptionalItemRecipeCategory extends BaseRecipeCategory<BasicItemStackToFluidOptionalItemRecipe> {

    private static final String OUTPUT_ITEM = "outputItem";

    private final GuiGauge<?> outputTank;
    private final GuiSlot outputItem;
    private final GuiSlot input;

    public ItemStackToFluidOptionalItemRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<BasicItemStackToFluidOptionalItemRecipe> recipeType, boolean isConversion) {
        super(helper, recipeType);
        input = addSlot(SlotType.INPUT, 26, 36);
        outputItem = addSlot(SlotType.OUTPUT, 110, 36);
        outputTank = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        addElement(new GuiProgress(isConversion ? () -> 1 : getSimpleProgressTimer(), ProgressType.LARGE_RIGHT, this, 54, 40));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BasicItemStackToFluidOptionalItemRecipe recipe, IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput()::getRepresentations);
        List<ItemStackTemplate> itemOutputs = new ArrayList<>();
        initFluid(builder, RecipeIngredientRole.OUTPUT, outputTank, recipe, (r, context) -> {
            List<FluidOptionalItemOutput> outputDefinition = r.getOutputDefinition(context);
            List<FluidStack> fluidOutputs = new ArrayList<>(outputDefinition.size());
            for (FluidOptionalItemOutput output : outputDefinition) {
                fluidOutputs.add(output.fluid().create());
                ItemStackTemplate optionalItem = output.optionalItem();
                if (optionalItem != null) {
                    itemOutputs.add(optionalItem);
                }
            }
            return fluidOutputs;
        });
        if (!itemOutputs.isEmpty()) {
            initItem(builder, outputItem, _ -> itemOutputs)
                  .setSlotName(OUTPUT_ITEM);
        }
    }

    @Nullable
    @Override
    public Identifier getIdentifier(BasicItemStackToFluidOptionalItemRecipe recipe) {
        //TODO - 26.2: Can we grab the context map from jei?
        ContextMap contextMap = SlotDisplayContext.fromLevel(Objects.requireNonNull(Minecraft.getInstance().level));
        List<ItemStack> representations = recipe.getInput().getRepresentations(contextMap);
        if (representations.size() == 1) {
            Identifier itemId = BuiltInRegistries.ITEM.getKeyOrNull(representations.getFirst().getItem());
            if (itemId != null) {
                return RegistryUtils.synthetic(itemId, "liquification", Mekanism.MODID);
            }
        }
        return null;
    }

    @Override
    public Codec<BasicItemStackToFluidOptionalItemRecipe> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return MekanismRecipeSerializersInternal.LIQUIFIER.value().codec().codec();
    }
}