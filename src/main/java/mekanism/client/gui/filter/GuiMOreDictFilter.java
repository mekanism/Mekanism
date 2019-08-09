package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.miner.MOreDictFilter;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMOreDictFilter extends GuiOreDictFilter<MOreDictFilter, TileEntityDigitalMiner> {

    public GuiMOreDictFilter(PlayerEntity player, TileEntityDigitalMiner tile, int index) {
        super(player, tile);
        origFilter = (MOreDictFilter) tileEntity.filters.get(index);
        filter = ((MOreDictFilter) tileEntity.filters.get(index)).clone();
        updateStackList(filter.getOreDictName());
    }

    public GuiMOreDictFilter(PlayerEntity player, TileEntityDigitalMiner tile) {
        super(player, tile);
        isNew = true;
        filter = new MOreDictFilter();
    }

    @Override
    protected void addButtons() {
        buttons.add(saveButton = new Button(guiLeft + 27, guiTop + 62, 60, 20, LangUtils.localize("gui.save"),
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
        buttons.add(deleteButton = new Button(guiLeft + 89, guiTop + 62, 60, 20, LangUtils.localize("gui.delete"),
              onPress -> {
                  Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), true, origFilter, null));
                  sendPacketToServer(0);
              }));
        buttons.add(backButton = new GuiButtonDisableableImage(guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation(),
              onPress -> sendPacketToServer(isNew ? 4 : 0)));
        buttons.add(replaceButton = new GuiButtonDisableableImage(guiLeft + 148, guiTop + 45, 14, 14, 199, 14, -14, getGuiLocation(),
              onPress -> filter.requireStack = !filter.requireStack));
        buttons.add(checkboxButton = new GuiButtonDisableableImage(guiLeft + 131, guiTop + 47, 12, 12, 187, 12, -12, getGuiLocation(),
              onPress -> setText()));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler.sendToServer(new PacketDigitalMinerGui(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), guiID, 0, 0));
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiMOreDictFilter.png");
    }

    @Override
    protected void updateStackList(String oreName) {
        iterStacks = OreDictCache.getOreDictStacks(oreName, true);
        stackSwitch = 0;
        stackIndex = -1;
    }
}