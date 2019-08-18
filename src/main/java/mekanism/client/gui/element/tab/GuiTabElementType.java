package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiTabElementType<TILE extends TileEntity, TAB extends Enum & TabType<TILE>> extends GuiTabElement<TILE> {

    private final TAB tabType;

    public GuiTabElementType(IGuiWrapper gui, TILE tile, TAB type, ResourceLocation def) {
        super(type.getResource(), gui, def, tile, type.getYPos());
        tabType = type;
    }

    @Override
    public void buttonClicked() {
        //TODO: Handle this correctly
        //NetworkHooks.openGui(ServerPlayerEntity, tabType.getProvider(tileEntity), BlockPos);
    }

    @Override
    public void displayForegroundTooltip(int xAxis, int yAxis) {
        displayTooltip(TextComponentUtil.build(tabType), xAxis, yAxis);
    }
}