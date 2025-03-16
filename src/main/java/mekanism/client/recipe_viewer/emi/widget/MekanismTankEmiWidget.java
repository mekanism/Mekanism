package mekanism.client.recipe_viewer.emi.widget;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

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
    public void drawStack(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        EmiIngredient ingredient = getStack();
        List<EmiStack> stacks = ingredient.getEmiStacks();
        EmiStack stack = stacks.isEmpty() ? EmiStack.EMPTY : RecipeViewerUtils.getCurrent(stacks);
        if (!stack.isEmpty() && ingredient.getAmount() > 0) {
            TextureAtlasSprite sprite;
            if (stack instanceof ChemicalEmiStack chemicalEmiStack) {
                ChemicalStack chemicalStack = chemicalEmiStack.getStack();
                MekanismRenderer.color(graphics, chemicalStack);
                sprite = MekanismRenderer.getChemicalTexture(chemicalStack);
            } else if (stack.getKey() instanceof Fluid fluid) {
                FluidStack fluidStack = new FluidStack(fluid.builtInRegistryHolder(), MathUtils.clampToInt(ingredient.getAmount()), stack.getComponentChanges());
                MekanismRenderer.color(graphics, fluidStack);
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
            GuiUtils.drawTiledSprite(graphics, x, y, height, width, desiredHeight, sprite, 16, 16, 0, TilingDirection.UP_RIGHT);
            MekanismRenderer.resetColor(graphics);
        }
        if (this.gauge != null) {
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(this.gauge.getGuiLeft(), this.gauge.getGuiTop(), 0);
            this.gauge.drawBarOverlay(graphics);
            pose.popPose();
        }
    }
}