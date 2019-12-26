package mekanism.client.gui.element.tab;

import mekanism.api.TileNetworkList;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiAmplifierTab extends GuiInsetElement<TileEntityLaserAmplifier> {

    private static final ResourceLocation OFF = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "amplifier_off.png");
    private static final ResourceLocation ENTITY = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "amplifier_entity.png");
    private static final ResourceLocation CONTENTS = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "amplifier_contents.png");

    public GuiAmplifierTab(IGuiWrapper gui, TileEntityLaserAmplifier tile, ResourceLocation def) {
        super(OFF, gui, def, tile, -26, 138, 26, 18);
    }

    @Override
    protected ResourceLocation getResource() {
        switch (tile.outputMode) {
            case ENTITY_DETECTION:
                return ENTITY;
            case ENERGY_CONTENTS:
                return CONTENTS;
        }
        return super.getResource();
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(MekanismLang.REDSTONE_OUTPUT.translate(tile.outputMode), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(3)));
    }
}