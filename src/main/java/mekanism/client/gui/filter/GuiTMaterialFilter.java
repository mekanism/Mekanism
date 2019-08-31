package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.ColorButton;
import mekanism.client.gui.button.DisableableImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.common.Mekanism;
import mekanism.common.content.transporter.TMaterialFilter;
import mekanism.common.inventory.container.tile.filter.LSMaterialFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiTMaterialFilter extends GuiMaterialFilter<TMaterialFilter, TileEntityLogisticalSorter, LSMaterialFilterContainer> {

    public GuiTMaterialFilter(LSMaterialFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    protected void addButtons() {
        addButton(saveButton = new TranslationButton(guiLeft + 47, guiTop + 62, 60, 20, "gui.mekanism.save", onPress -> {
            if (!filter.getMaterialItem().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(ClickedTileButton.BACK_BUTTON);
            } else {
                status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.mekanism.itemFilter.noItem"));
                ticker = 20;
            }
        }));
        addButton(deleteButton = new TranslationButton(guiLeft + 109, guiTop + 62, 60, 20, "gui.mekanism.delete", onPress -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.BACK_BUTTON);
        }));
        addButton(new DisableableImageButton(guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation(),
              onPress -> sendPacketToServer(isNew ? ClickedTileButton.LS_SELECT_FILTER_TYPE : ClickedTileButton.BACK_BUTTON)));
        addButton(new DisableableImageButton(guiLeft + 11, guiTop + 64, 11, 11, 198, 11, -11, getGuiLocation(),
              onPress -> filter.allowDefault = !filter.allowDefault, getOnHover("gui.mekanism.allowDefault")));
        addButton(new ColorButton(guiLeft + 12, guiTop + 44, 16, 16, this, () -> filter.color,
              onPress -> filter.color = InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? null : TransporterUtils.increment(filter.color),
              onRightClick -> filter.color = TransporterUtils.decrement(filter.color)));
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        if (!filter.getMaterialItem().isEmpty()) {
            renderScaledText(filter.getMaterialItem().getDisplayName(), 35, 41, 0x00CD00, 107);
        }
        drawTransporterForegroundLayer(filter.getMaterialItem());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && overTypeInput(mouseX - guiLeft, mouseY - guiTop)) {
            materialMouseClicked();
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "sorter_material_filter.png");
    }
}