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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public class GuiFluidGauge extends GuiTankGauge<FluidResource, IFluidTank> {

    public static GuiFluidGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiFluidGauge gauge = new GuiFluidGauge(null, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
        gauge.dummy = true;
        return gauge;
    }

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

    @Nullable
    @Override
    public TextureAtlasSprite getIcon() {
        FluidResource type = getTypeOrDummy();
        return type.isEmpty() ? null : MekanismRenderer.getFluidTexture(type, FluidTextureType.STILL);
    }

    @Override
    protected List<Component> getContentsTooltips(FluidResource type, long amount, TooltipContext context, @Nullable Player player, TooltipFlag tooltipFlag) {
        List<Component> tooltip = super.getContentsTooltips(type, amount, context, player, tooltipFlag);
        FluidStack stack = type.toStack(Ints.saturatedCast(amount));
        if (!stack.isEmpty()) {
            //TODO - 26.2: Re-evaluate how tooltips are done for fluids and see if this is correct (especially in relation to it potentially showing the name twice)
            // We might also want to update chemicals to be similar to this in that there is a getter on the stack rather than a method that appends to list on the stack
            tooltip.addAll(stack.getTooltipLines(context, player, tooltipFlag));
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
}