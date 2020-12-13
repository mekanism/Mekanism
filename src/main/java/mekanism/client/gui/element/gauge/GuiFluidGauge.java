package mekanism.client.gui.element.gauge;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.PacketDropperUse.TankType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiFluidGauge extends GuiTankGauge<FluidStack, IExtendedFluidTank> {

    private ITextComponent label;
    private Supplier<IExtendedFluidTank> tankSupplier;

    public GuiFluidGauge(ITankInfoHandler<IExtendedFluidTank> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(type, gui, x, y, sizeX, sizeY, handler, TankType.FLUID_TANK);
        //Ensure it isn't null
        setDummyType(FluidStack.EMPTY);
    }

    public GuiFluidGauge(Supplier<IExtendedFluidTank> tankSupplier, Supplier<List<IExtendedFluidTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y) {
        this(tankSupplier, tanksSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
    }

    public GuiFluidGauge(Supplier<IExtendedFluidTank> tankSupplier, Supplier<List<IExtendedFluidTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        this(new ITankInfoHandler<IExtendedFluidTank>() {
            @Nullable
            @Override
            public IExtendedFluidTank getTank() {
                return tankSupplier.get();
            }

            @Override
            public int getTankIndex() {
                IExtendedFluidTank tank = getTank();
                return tank == null ? -1 : tanksSupplier.get().indexOf(tank);
            }
        }, type, gui, x, y, sizeX, sizeY);
        this.tankSupplier = tankSupplier;
    }

    @Override
    protected GaugeInfo getGaugeColor() {
        IExtendedFluidTank tank;
        if (gui() instanceof GuiMekanismTile && tankSupplier != null && (tank = tankSupplier.get()) != null) {
            TileEntityMekanism tile = ((GuiMekanismTile<?, ?>) gui()).getContainer().getTileEntity();
            if (tile instanceof ISideConfiguration) {
                DataType dataType = ((ISideConfiguration) tile).getActiveDataType(tank);
                if (dataType != null) {
                    return GaugeInfo.get(dataType);
                }
            }
        }
        return GaugeInfo.STANDARD;
    }

    public GuiFluidGauge setLabel(ITextComponent label) {
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
        IExtendedFluidTank tank = getTank();
        if (tank == null || tank.isEmpty() || tank.getCapacity() == 0) {
            return 0;
        }
        if (tank.getFluidAmount() == Integer.MAX_VALUE) {
            return height - 2;
        }
        float scale = (float) tank.getFluidAmount() / (float) tank.getCapacity();
        return Math.round(scale * (height - 2));
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy || getTank() == null) {
            return MekanismRenderer.getFluidTexture(dummyType, FluidType.STILL);
        }
        FluidStack fluid = getTank().getFluid();
        return MekanismRenderer.getFluidTexture(fluid.isEmpty() ? dummyType : fluid, FluidType.STILL);
    }

    @Override
    public ITextComponent getLabel() {
        return label;
    }

    @Override
    public List<ITextComponent> getTooltipText() {
        if (dummy) {
            return Collections.singletonList(TextComponentUtil.build(dummyType));
        }
        IExtendedFluidTank tank = getTank();
        if (tank == null || tank.isEmpty()) {
            return Collections.singletonList(MekanismLang.EMPTY.translate());
        }
        int amount = tank.getFluidAmount();
        FluidStack fluidStack = tank.getFluid();
        if (amount == Integer.MAX_VALUE) {
            return Collections.singletonList(MekanismLang.GENERIC_STORED.translate(fluidStack, MekanismLang.INFINITE));
        }
        return Collections.singletonList(MekanismLang.GENERIC_STORED_MB.translate(fluidStack, formatInt(amount)));
    }

    @Override
    protected void applyRenderColor() {
        MekanismRenderer.color(dummy || getTank() == null ? dummyType : getTank().getFluid());
    }

    @Nullable
    @Override
    public Object getIngredient() {
        return getTank().isEmpty() ? null : getTank().getFluid();
    }
}