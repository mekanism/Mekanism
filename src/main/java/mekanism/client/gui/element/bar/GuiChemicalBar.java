package mekanism.client.gui.element.bar;

import com.google.common.primitives.Ints;
import java.util.List;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackContentsFactory;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

public class GuiChemicalBar extends GuiTankBar<ChemicalResource, IChemicalTank> {

    @Nullable
    private GhostSlot<ChemicalStack> ghostRecipeData;

    public GuiChemicalBar(IGuiWrapper gui, ResourceTankInfoProvider<ChemicalResource, IChemicalTank> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(gui, infoProvider, x, y, width, height, horizontal);
    }

    @Override
    protected TankType getType() {
        return TankType.CHEMICAL_TANK;
    }

    @Override
    protected List<Component> getContentsTooltips(ChemicalResource resource, long amount, TooltipContext context, @Nullable Player player, TooltipFlag tooltipFlag) {
        ChemicalStack stack;
        if (ghostRecipeData != null) {
            stack = ghostRecipeData.getCurrent();
            resource = ChemicalResource.of(stack);
            amount = stack.amount();
        } else {
            stack = resource.toStack(Ints.saturatedCast(amount));
        }
        List<Component> tooltip = super.getContentsTooltips(resource, amount, context, player, tooltipFlag);
        if (!stack.isEmpty()) {
            MekanismUtils.appendAllButFirst(tooltip, stack.getTooltipLines(context, player, tooltipFlag));
        }
        return tooltip;
    }

    @Override
    protected int getRenderColor(ChemicalResource resource) {
        return MekanismRenderer.color(resource);
    }

    @Override
    protected TextureAtlasSprite getIcon(ChemicalResource resource) {
        return MekanismRenderer.getChemicalTexture(resource);
    }

    @Override
    protected void renderBarContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        if (ghostRecipeData == null) {
            super.renderBarContents(guiGraphics, mouseX, mouseY, partialTicks, handlerLevel);
        } else {
            ChemicalStack current = ghostRecipeData.getCurrent();
            renderBarGhostContents(guiGraphics, MekanismRenderer.getChemicalTexture(current), MekanismRenderer.color(current),
                  ghostRecipeData.getCurrent().amount(), ghostRecipeData.extraData());
        }
    }

    @Override
    protected Object toIngredientStack(ChemicalResource resource, long amount) {
        return resource.toStack(Ints.saturatedCast(amount));
    }

    @Override
    protected boolean isRenderingGhost() {
        return ghostRecipeData != null;
    }

    @Override
    public void setGhostData(SlotDisplay contents, Iterable<SlotDisplay> recipeContext, ContextMap context, SlotSelectTime slotSelectTime, boolean isResultSlot) {
        List<ChemicalStack> entries = contents.resolve(context, ChemicalStackContentsFactory.INSTANCE).toList();
        if (entries.isEmpty()) {
            ghostRecipeData = null;
        } else {
            ghostRecipeData = GhostSlot.createWithMaxTank(entries, recipeContext, context, slotSelectTime, isResultSlot);
        }
    }

    @Override
    public void clearGhostData() {
        ghostRecipeData = null;
    }
}