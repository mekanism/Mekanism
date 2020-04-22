package mekanism.client.gui.element.bar;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.bar.GuiChemicalBar.ChemicalInfoProvider;
import mekanism.client.jei.IJEIIngredientHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketDropperUse;
import mekanism.common.network.PacketDropperUse.DropperAction;
import mekanism.common.network.PacketDropperUse.TankType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalBar<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends GuiBar<ChemicalInfoProvider<CHEMICAL, STACK>>
      implements IJEIIngredientHelper {

    private final boolean horizontal;

    public GuiChemicalBar(IGuiWrapper gui, ChemicalInfoProvider<CHEMICAL, STACK> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(AtlasTexture.LOCATION_BLOCKS_TEXTURE, gui, infoProvider, x, y, width, height);
        this.horizontal = horizontal;
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        STACK stored = getHandler().getStack();
        if (!stored.isEmpty()) {
            double level = getHandler().getLevel();
            if (level > 0) {
                CHEMICAL type = stored.getType();
                MekanismRenderer.color(type);
                TextureAtlasSprite icon = MekanismRenderer.getChemicalTexture(type);
                if (horizontal) {
                    drawTiledSprite(x + 1, y + 1, height - 2, (int) (level * (width - 2)), height - 2, icon);
                } else {
                    drawTiledSprite(x + 1, y + 1, height - 2, width - 2, (int) (level * (height - 2)), icon);
                }
                MekanismRenderer.resetColor();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            ItemStack stack = GuiTexturedElement.minecraft.player.inventory.getItemStack();
            if (guiObj instanceof GuiMekanismTile && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                TankType tankType = null;
                CHEMICAL type = getHandler().getStack().getType();
                if (type instanceof Gas) {
                    tankType = TankType.GAS_TANK;
                } else if (type instanceof InfuseType) {
                    tankType = TankType.INFUSION_TANK;
                }
                if (tankType != null) {
                    int index = getHandler().getTankIndex();
                    if (index != -1) {
                        DropperAction action;
                        if (button == 0) {
                            action = Screen.hasShiftDown() ? DropperAction.DUMP_TANK : DropperAction.FILL_DROPPER;
                        } else {
                            action = DropperAction.DRAIN_DROPPER;
                        }
                        Mekanism.packetHandler.sendToServer(new PacketDropperUse(((GuiMekanismTile<?, ?>) guiObj).getTileEntity().getPos(), action, tankType, index));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Nullable
    @Override
    public Object getIngredient() {
        STACK chemicalStack = getHandler().getStack();
        return chemicalStack.isEmpty() ? null : chemicalStack;
    }

    //Note the GuiBar.IBarInfoHandler is needed, as it cannot compile and resolve just IBarInfoHandler
    public interface ChemicalInfoProvider<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends GuiBar.IBarInfoHandler {

        @Nonnull
        STACK getStack();

        int getTankIndex();
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> ChemicalInfoProvider<CHEMICAL, STACK> getProvider(IChemicalTank<CHEMICAL, STACK> tank,
          List<? extends IChemicalTank<CHEMICAL, STACK>> tanks) {
        return new ChemicalInfoProvider<CHEMICAL, STACK>() {
            @Nonnull
            @Override
            public STACK getStack() {
                return tank.getStack();
            }

            @Override
            public int getTankIndex() {
                return tanks.indexOf(tank);
            }

            @Override
            public ITextComponent getTooltip() {
                if (tank.isEmpty()) {
                    return MekanismLang.EMPTY.translate();
                } else if (tank.getStored() == Long.MAX_VALUE) {
                    return MekanismLang.GENERIC_STORED.translate(tank.getType(), MekanismLang.INFINITE);
                }
                return MekanismLang.GENERIC_STORED.translate(tank.getType(), tank.getStored());
            }

            @Override
            public double getLevel() {
                return tank.getStored() / (double) tank.getCapacity();
            }
        };
    }
}