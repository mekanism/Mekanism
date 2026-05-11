package mekanism.client.gui.element.gauge;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

public class GuiFluidGauge extends GuiTankGauge<FluidStack, IFluidTank> {

    private Component label;

    public GuiFluidGauge(ITankInfoHandler<IFluidTank> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(type, gui, x, y, sizeX, sizeY, handler, TankType.FLUID_TANK);
        //Ensure it isn't null
        setDummyType(FluidStack.EMPTY);
    }

    public GuiFluidGauge(Supplier<IFluidTank> tankSupplier, Supplier<List<IFluidTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y) {
        this(tankSupplier, tanksSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
    }

    public GuiFluidGauge(Supplier<IFluidTank> tankSupplier, Supplier<List<IFluidTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        this(new ITankInfoHandler<>() {
            @Nullable
            @Override
            public IFluidTank getTank() {
                return tankSupplier.get();
            }

            @Override
            public int getTankIndex() {
                IFluidTank tank = getTank();
                return tank == null ? -1 : tanksSupplier.get().indexOf(tank);
            }
        }, type, gui, x, y, sizeX, sizeY);
    }

    public GuiFluidGauge setLabel(Component label) {
        this.label = label;
        return this;
    }

    public static GuiFluidGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiFluidGauge gauge = new GuiFluidGauge(null, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
        gauge.dummy = true;
        return gauge;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.FLUID;
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }
        IFluidTank tank = getTank();
        if (tank == null || tank.isEmpty()) {
            return 0;
        }
        long capacity = tank.getCurrentLimitAsLong();
        if (capacity == 0) {
            return 0;
        } else if (tank.amountAsLong() == Long.MAX_VALUE) {
            return height - 2;
        }
        float scale = tank.amountAsLong() / (float) capacity;
        return Math.max(1, Math.round(scale * (height - 2)));
    }

    @Nullable
    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return MekanismRenderer.getFluidTexture(dummyType, FluidTextureType.STILL);
        }
        IFluidTank tank = getTank();
        return tank == null || tank.isEmpty() ? null : MekanismRenderer.getFluidTexture(tank.getResource(), FluidTextureType.STILL);
    }

    @Override
    public Component getLabel() {
        return label;
    }

    @Override
    public List<Component> getTooltipText() {
        if (dummy) {
            return Collections.singletonList(TextComponentUtil.build(dummyType));
        }
        IFluidTank tank = getTank();
        if (tank == null || tank.isEmpty()) {
            return Collections.singletonList(MekanismLang.EMPTY.translate());
        }
        int amount = tank.amount();
        FluidResource fluidType = tank.getResource();
        if (amount == Integer.MAX_VALUE) {
            return Collections.singletonList(MekanismLang.GENERIC_STORED.translate(fluidType, MekanismLang.INFINITE));
        }
        return Collections.singletonList(MekanismLang.GENERIC_STORED_MB.translate(fluidType, TextUtils.format(amount)));
    }

    @Override
    protected void applyRenderColor(GuiGraphicsExtractor guiGraphics) {
        MekanismRenderer.color(dummy || getTank() == null ? dummyType : getTank().getFluid());
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        return getTank().isEmpty() ? Optional.empty() : Optional.of(getTank().getFluid());
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        return new Rect2i(getX() + 1, getY() + 1, width - 2, height - 2);
    }
}