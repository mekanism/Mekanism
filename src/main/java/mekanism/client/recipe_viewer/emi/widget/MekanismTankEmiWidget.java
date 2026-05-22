package mekanism.client.recipe_viewer.emi.widget;

import com.google.common.primitives.Ints;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public class MekanismTankEmiWidget extends SlotWidget {

    private final long capacity;
    @Nullable
    private final GuiGauge<?> gauge;

    public MekanismTankEmiWidget(EmiIngredient stack, GuiElement element, long capacity) {
        super(stack, element.getX(), element.getY());
        this.bounds = new Bounds(element.getX(), element.getY(), element.getWidth(), element.getHeight());
        this.capacity = capacity;
        if (element instanceof GuiGauge<?> g) {
            this.gauge = g;
        } else {
            this.gauge = null;
        }
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void drawStack(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EmiIngredient ingredient = getStack();
        List<EmiStack> stacks = ingredient.getEmiStacks();
        EmiStack stack = stacks.isEmpty() ? EmiStack.EMPTY : RecipeViewerUtils.getCurrent(stacks);
        if (!stack.isEmpty() && ingredient.getAmount() > 0) {
            TextureAtlasSprite sprite;
            int color;
            if (stack instanceof ChemicalEmiStack chemicalEmiStack) {
                ChemicalStack chemicalStack = chemicalEmiStack.getStack();
                color = MekanismRenderer.color(chemicalStack);
                sprite = MekanismRenderer.getChemicalTexture(chemicalStack);
            } else if (stack.getKey() instanceof Fluid fluid) {
                FluidStack fluidStack = new FluidStack(fluid.builtInRegistryHolder(), Ints.saturatedCast(ingredient.getAmount()), stack.getComponentChanges());
                color = MekanismRenderer.color(fluidStack);
                sprite = MekanismRenderer.getFluidTexture(fluidStack, FluidTextureType.STILL);
            } else {
                return;
            }
            int x = bounds.x() + 1;
            int y = bounds.y() + 1;
            int width = bounds.width() - 2;
            int height = bounds.height() - 2;
            int desiredHeight = MathUtils.clampToInt(height * (double) ingredient.getAmount() / capacity);
            if (desiredHeight < 1) {
                desiredHeight = 1;
            }
            if (desiredHeight > height) {
                desiredHeight = height;
            }
            //Tile upwards and to the right as the majority of things we render are gauges which look better when tiling upwards
            GuiUtils.drawTiledSprite(graphics, x, y, height, width, desiredHeight, sprite, 16, 16, 0, TilingDirection.UP_RIGHT, color);
        }
        if (this.gauge != null) {
            Matrix3x2fStack matrix = graphics.pose();
            matrix.pushMatrix();
            matrix.translate(this.gauge.getGuiLeft(), this.gauge.getGuiTop());
            this.gauge.drawBarOverlay(graphics);
            matrix.popMatrix();
        }
    }
}