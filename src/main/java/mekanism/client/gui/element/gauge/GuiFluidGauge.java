package mekanism.client.gui.element.gauge;

import com.google.common.primitives.Ints;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.fluid.IFluidTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.util.MekanismUtils;
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

public class GuiFluidGauge extends GuiTankGauge<FluidResource, IFluidTank> {

    public static GuiFluidGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiFluidGauge gauge = new GuiFluidGauge(null, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
        gauge.dummy = true;
        return gauge;
    }

    @Nullable
    private GhostSlot<FluidStack> ghostRecipeData;

    private GuiFluidGauge(@Nullable ITankInfoHandler<IFluidTank> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(type, gui, x, y, sizeX, sizeY, handler, TankType.FLUID_TANK, FluidResource.EMPTY);
    }

    public GuiFluidGauge(Supplier<IFluidTank> tankSupplier, Supplier<List<IFluidTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y) {
        this(tankSupplier, tanksSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
    }

    public GuiFluidGauge(Supplier<IFluidTank> tankSupplier, Supplier<List<IFluidTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        this(getInfoHandler(tankSupplier, tanksSupplier), type, gui, x, y, sizeX, sizeY);
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.FLUID;
    }

    @Override
    protected FluidResource getTypeOrDummy() {
        return ghostRecipeData == null ? super.getTypeOrDummy() : FluidResource.of(ghostRecipeData.getCurrent());
    }

    @Nullable
    @Override
    public TextureAtlasSprite getIcon() {
        FluidResource type = getTypeOrDummy();
        return type.isEmpty() ? null : MekanismRenderer.getFluidTexture(type, FluidTextureType.STILL);
    }

    @Override
    public int getScaledLevel() {
        if (ghostRecipeData == null) {
            return super.getScaledLevel();
        }
        int maxMb = ghostRecipeData.extraData();
        double scale = maxMb == 0 ? 0 : ghostRecipeData.getCurrent().amount() / (double) maxMb;
        //Calculate a scaled level between 0.25 and 0.75 of the bar being filled
        return getScaledLevel(0.25 + scale / 2);
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
    protected int getRenderColor() {
        return MekanismRenderer.color(getTypeOrDummy());
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        IFluidTank tank = getContainer();
        return tank == null || tank.isEmpty() ? Optional.empty() : Optional.of(tank.resource().toStack(tank.amountAsInt()));
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