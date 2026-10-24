package mekanism.client.gui.element.bar;

import com.google.common.primitives.Ints;
import java.util.List;
import java.util.Optional;
import mekanism.api.fluid.IFluidTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public class GuiFluidBar extends GuiTankBar<FluidResource, IFluidTank> {

    @Nullable
    private GhostSlot<FluidStack> ghostRecipeData;

    public GuiFluidBar(IGuiWrapper gui, ResourceTankInfoProvider<FluidResource, IFluidTank> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(gui, infoProvider, x, y, width, height, horizontal);
    }

    @Override
    protected TankType getType() {
        return TankType.FLUID_TANK;
    }

    @Override
    protected int getRenderColor(FluidResource resource) {
        return MekanismRenderer.color(resource);
    }

    @Override
    protected TextureAtlasSprite getIcon(FluidResource resource) {
        return MekanismRenderer.getFluidTexture(resource, FluidTextureType.STILL);
    }

    @Override
    protected List<Component> getContentsTooltips(FluidResource resource, long amount, TooltipContext context, @Nullable Player player, TooltipFlag tooltipFlag) {
        FluidStack stack;
        if (ghostRecipeData != null) {
            stack = ghostRecipeData.getCurrent();
            resource = FluidResource.of(stack);
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
    protected void renderBarContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        if (ghostRecipeData == null) {
            super.renderBarContents(guiGraphics, mouseX, mouseY, partialTicks, handlerLevel);
        } else {
            FluidStack current = ghostRecipeData.getCurrent();
            renderBarGhostContents(guiGraphics, MekanismRenderer.getFluidTexture(current, FluidTextureType.STILL), MekanismRenderer.getColorARGB(current),
                  ghostRecipeData.getCurrent().amount(), ghostRecipeData.extraData());
        }
    }

    @Override
    protected Object toIngredientStack(FluidResource resource, long amount) {
        return resource.toStack(Ints.saturatedCast(amount));
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        if (ghostRecipeData != null) {
            return Optional.of(ghostRecipeData.getCurrent());
        }
        return super.getIngredient(mouseX, mouseY);
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        ghostRecipeData = ((GuiFluidBar) element).ghostRecipeData;
    }

    @Override
    protected boolean isRenderingGhost() {
        return ghostRecipeData != null;
    }

    @Override
    public void setGhostData(SlotDisplay contents, Iterable<SlotDisplay> recipeContext, ContextMap context, SlotSelectTime slotSelectTime, boolean isResultSlot) {
        List<FluidStack> entries = contents.resolve(context, FluidStackContentsFactory.INSTANCE).toList();
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