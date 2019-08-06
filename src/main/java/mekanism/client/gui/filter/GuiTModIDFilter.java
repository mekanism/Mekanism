package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiColorButton;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.transporter.TModIDFilter;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTModIDFilter extends GuiModIDFilter<TModIDFilter, TileEntityLogisticalSorter> {

    public GuiTModIDFilter(PlayerEntity player, TileEntityLogisticalSorter tile, int index) {
        super(player, tile);
        origFilter = (TModIDFilter) tileEntity.filters.get(index);
        filter = ((TModIDFilter) tileEntity.filters.get(index)).clone();
        updateStackList(filter.getModID());
    }

    public GuiTModIDFilter(PlayerEntity player, TileEntityLogisticalSorter tile) {
        super(player, tile);
        isNew = true;
        filter = new TModIDFilter();
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTModIDFilter.png");
    }

    @Override
    protected void updateStackList(String modName) {
        iterStacks = OreDictCache.getModIDStacks(modName, false);
        stackSwitch = 0;
        stackIndex = -1;
    }

    @Override
    protected void addButtons() {
        buttonList.add(saveButton = new GuiButton(0, guiLeft + 47, guiTop + 62, 60, 20, LangUtils.localize("gui.save")));
        buttonList.add(deleteButton = new GuiButton(1, guiLeft + 109, guiTop + 62, 60, 20, LangUtils.localize("gui.delete")));
        buttonList.add(backButton = new GuiButtonDisableableImage(2, guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation()));
        buttonList.add(defaultButton = new GuiButtonDisableableImage(3, guiLeft + 11, guiTop + 64, 11, 11, 199, 11, -11, getGuiLocation()));
        buttonList.add(checkboxButton = new GuiButtonDisableableImage(4, guiLeft + 131, guiTop + 47, 12, 12, 187, 12, -12, getGuiLocation()));
        buttonList.add(colorButton = new GuiColorButton(5, guiLeft + 12, guiTop + 44, 16, 16, () -> filter.color));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), guiID, 0, 0));
    }
}