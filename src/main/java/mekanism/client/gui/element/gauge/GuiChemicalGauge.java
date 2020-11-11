package mekanism.client.gui.element.gauge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketDropperUse.TankType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiChemicalGauge<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      extends GuiTankGauge<CHEMICAL, TANK> {

    protected ITextComponent label;

    public GuiChemicalGauge(ITankInfoHandler<TANK> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY, TankType tankType) {
        super(type, gui, x, y, sizeX, sizeY, handler, tankType);
    }

    public GuiChemicalGauge(Supplier<TANK> tankSupplier, Supplier<List<TANK>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, TankType tankType) {
        this(tankSupplier, tanksSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2, tankType);
    }

    public GuiChemicalGauge(Supplier<TANK> tankSupplier, Supplier<List<TANK>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY,
          TankType tankType) {
        this(new ITankInfoHandler<TANK>() {
            @Nullable
            @Override
            public TANK getTank() {
                return tankSupplier.get();
            }

            @Override
            public int getTankIndex() {
                TANK tank = getTank();
                return tank == null ? -1 : tanksSupplier.get().indexOf(tank);
            }
        }, type, gui, x, y, sizeX, sizeY, tankType);
    }

    @Override
    protected GaugeInfo getGaugeColor() {
        if (guiObj instanceof GuiMekanismTile) {
            TANK tank = getTank();
            if (tank != null) {
                TileEntityMekanism tile = ((GuiMekanismTile<?, ?>) guiObj).getContainer().getTileEntity();
                if (tile instanceof ISideConfiguration) {
                    DataType dataType = ((ISideConfiguration) tile).getActiveDataType(tank);
                    if (dataType != null) {
                        return GaugeInfo.get(dataType);
                    }
                }
            }
        }
        return GaugeInfo.STANDARD;
    }

    public GuiChemicalGauge<CHEMICAL, STACK, TANK> setLabel(ITextComponent label) {
        this.label = label;
        return this;
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }
        TANK tank = getTank();
        if (tank == null || tank.isEmpty() || tank.getCapacity() == 0) {
            return 0;
        }
        double scale = tank.getStored() / (double) tank.getCapacity();
        return MathUtils.clampToInt(Math.round(scale * (height - 2)));
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return MekanismRenderer.getChemicalTexture(dummyType);
        }
        return getTank() == null || getTank().isEmpty() ? null : MekanismRenderer.getChemicalTexture(getTank().getType());
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
        TANK tank = getTank();
        if (tank == null || tank.isEmpty()) {
            return Collections.singletonList(MekanismLang.EMPTY.translate());
        }
        List<ITextComponent> list = new ArrayList<>();
        long amount = tank.getStored();
        if (amount == Long.MAX_VALUE) {
            list.add(MekanismLang.GENERIC_STORED.translate(tank.getType(), MekanismLang.INFINITE));
        } else {
            list.add(MekanismLang.GENERIC_STORED_MB.translate(tank.getType(), formatInt(amount)));
        }
        list.addAll(ChemicalUtil.getAttributeTooltips(tank.getType()));
        return list;
    }

    @Override
    protected void applyRenderColor() {
        if (dummy || getTank() == null) {
            MekanismRenderer.color(dummyType);
        } else {
            MekanismRenderer.color(getTank().getStack());
        }
    }

    @Nullable
    @Override
    public Object getIngredient() {
        return getTank().isEmpty() ? null : getTank().getStack();
    }
}