package mekanism.client.gui.element.gauge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class GuiChemicalGauge extends GuiTankGauge<Chemical, IChemicalTank> {

    public static GuiChemicalGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiChemicalGauge gauge = new GuiChemicalGauge(null, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
        gauge.dummy = true;
        return gauge;
    }

    protected Component label;

    public GuiChemicalGauge(ITankInfoHandler<IChemicalTank> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(type, gui, x, y, sizeX, sizeY, handler, TankType.CHEMICAL_TANK);
    }

    public GuiChemicalGauge(Supplier<IChemicalTank> tankSupplier, Supplier<List<IChemicalTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y) {
        this(tankSupplier, tanksSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
    }

    public GuiChemicalGauge(Supplier<IChemicalTank> tankSupplier, Supplier<List<IChemicalTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        this(new ITankInfoHandler<>() {
            @Override
            public IChemicalTank getTank() {
                return tankSupplier.get();
            }

            @Override
            public int getTankIndex() {
                IChemicalTank tank = getTank();
                return tank == null ? -1 : tanksSupplier.get().indexOf(tank);
            }
        }, type, gui, x, y, sizeX, sizeY);
    }

    public GuiChemicalGauge setLabel(Component label) {
        this.label = label;
        return this;
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }
        IChemicalTank tank = getTank();
        if (tank == null || tank.isEmpty() || tank.getCapacity() == 0) {
            return 0;
        }
        double scale = tank.getStored() / (double) tank.getCapacity();
        return MathUtils.clampToInt(Math.max(1, Math.round(scale * (height - 2))));
    }

    @Nullable
    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return MekanismRenderer.getChemicalTexture(dummyType);
        }
        IChemicalTank tank = getTank();
        return tank == null || tank.isEmpty() ? null : MekanismRenderer.getChemicalTexture(tank.getType());
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
        IChemicalTank tank = getTank();
        if (tank == null || tank.isEmpty()) {
            return Collections.singletonList(MekanismLang.EMPTY.translate());
        }
        List<Component> list = new ArrayList<>();
        long amount = tank.getStored();
        if (amount == Long.MAX_VALUE) {
            list.add(MekanismLang.GENERIC_STORED.translate(tank.getType(), MekanismLang.INFINITE));
        } else {
            list.add(MekanismLang.GENERIC_STORED_MB.translate(tank.getType(), TextUtils.format(amount)));
        }
        ChemicalUtil.addChemicalDataToTooltip(list, tank.getType(), Minecraft.getInstance().options.advancedItemTooltips);
        return list;
    }

    @Override
    protected void applyRenderColor(GuiGraphics guiGraphics) {
        if (dummy || getTank() == null) {
            MekanismRenderer.color(guiGraphics, dummyType);
        } else {
            MekanismRenderer.color(guiGraphics, getTank().getStack());
        }
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        return getTank().isEmpty() ? Optional.empty() : Optional.of(getTank().getStack());
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        return new Rect2i(getX() + 1, getY() + 1, width - 2, height - 2);
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.CHEMICAL;
    }
}