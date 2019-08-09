package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiColorButton;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.transporter.TOreDictFilter;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiTOreDictFilter extends GuiOreDictFilter<TOreDictFilter, TileEntityLogisticalSorter> {

    public GuiTOreDictFilter(PlayerEntity player, TileEntityLogisticalSorter tile, int index) {
        super(player, tile);
        origFilter = (TOreDictFilter) tileEntity.filters.get(index);
        filter = ((TOreDictFilter) tileEntity.filters.get(index)).clone();
        updateStackList(filter.getOreDictName());
    }

    public GuiTOreDictFilter(PlayerEntity player, TileEntityLogisticalSorter tile) {
        super(player, tile);
        isNew = true;
        filter = new TOreDictFilter();
    }

    @Override
    protected void addButtons() {
        buttons.add(saveButton = new Button(guiLeft + 47, guiTop + 62, 60, 20, LangUtils.localize("gui.save"),
              onPress -> {
                  if (!text.getText().isEmpty()) {
                      setText();
                  }
                  if (filter.getOreDictName() != null && !filter.getOreDictName().isEmpty()) {
                      if (isNew) {
                          Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tileEntity), filter));
                      } else {
                          Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), false, origFilter, filter));
                      }
                      sendPacketToServer(0);
                  } else {
                      status = EnumColor.DARK_RED + LangUtils.localize("gui.oredictFilter.noKey");
                      ticker = 20;
                  }
              }));
        buttons.add(deleteButton = new Button(guiLeft + 109, guiTop + 62, 60, 20, LangUtils.localize("gui.delete"),
              onPress -> {
                  Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), true, origFilter, null));
                  sendPacketToServer(0);
              }));
        buttons.add(backButton = new GuiButtonDisableableImage(guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation(),
              onPress -> sendPacketToServer(isNew ? 5 : 0)));
        buttons.add(defaultButton = new GuiButtonDisableableImage(guiLeft + 11, guiTop + 64, 11, 11, 199, 11, -11, getGuiLocation(),
              onPress -> filter.allowDefault = !filter.allowDefault));
        buttons.add(checkboxButton = new GuiButtonDisableableImage(guiLeft + 131, guiTop + 47, 12, 12, 187, 12, -12, getGuiLocation(),
              onPress -> setText()));
        buttons.add(colorButton = new GuiColorButton(guiLeft + 12, guiTop + 44, 16, 16, () -> filter.color,
              onPress -> {
                  if (InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                      filter.color = null;
                  } else {
                      filter.color = TransporterUtils.increment(filter.color);
                  }
              }));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler.sendToServer(new PacketLogisticalSorterGui(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), guiID, 0, 0));
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTOreDictFilter.png");
    }

    @Override
    protected void updateStackList(String oreName) {
        iterStacks = OreDictCache.getOreDictStacks(oreName, false);
        stackSwitch = 0;
        stackIndex = -1;
    }
}