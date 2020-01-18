package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.api.chemical.Chemical;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiRecipeType;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.GuiVerticalProgress;
import mekanism.client.gui.element.bar.GuiHorizontalChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar.ChemicalInfoProvider;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiSortingTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.tile.FactoryContainer;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory;
import mekanism.common.tile.factory.TileEntityMetallurgicInfuserFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiFactory extends GuiMekanismTile<TileEntityFactory<?>, FactoryContainer> {

    public GuiFactory(FactoryContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        if (tile.hasSecondaryResourceBar()) {
            ySize += 11;
        }
        if (tile.tier == FactoryTier.ULTIMATE) {
            xSize += 34;
        }
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tile, resource));
        addButton(new GuiSecurityTab<>(this, tile, resource));
        addButton(new GuiUpgradeTab(this, tile, resource));
        addButton(new GuiRecipeType(this, tile, resource));
        addButton(new GuiSideConfigurationTab(this, tile, resource));
        addButton(new GuiTransporterConfigTab(this, tile, resource));
        addButton(new GuiSortingTab(this, tile, resource));
        addButton(new GuiVerticalPowerBar(this, tile, resource, getXSize() - 12, 16));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.lastUsage)),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getNeededEnergy()))), this, resource));

        if (tile.hasSecondaryResourceBar()) {
            ChemicalInfoProvider<? extends Chemical<?>> provider = null;
            if (tile instanceof TileEntityMetallurgicInfuserFactory) {
                provider = GuiVerticalChemicalBar.getProvider(((TileEntityMetallurgicInfuserFactory) tile).getInfusionTank());
            } else if (tile instanceof TileEntityItemStackGasToItemStackFactory) {
                provider = GuiVerticalChemicalBar.getProvider(((TileEntityItemStackGasToItemStackFactory) tile).getGasTank());
            }
            if (provider != null) {
                int barX = tile.tier == FactoryTier.ULTIMATE ? 25 : 7;
                addButton(new GuiHorizontalChemicalBar<>(this, provider, resource, barX, 76, 140));
                //TODO: Move left and make wider for ultimate factory??
                addButton(new GuiDumpButton<>(this, tile, resource, barX + 141, 76,
                      () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(1)))));
            }
        }

        int baseX = tile.tier == FactoryTier.BASIC ? 55 : tile.tier == FactoryTier.ADVANCED ? 35 : tile.tier == FactoryTier.ELITE ? 29 : 27;
        int baseXMult = tile.tier == FactoryTier.BASIC ? 38 : tile.tier == FactoryTier.ADVANCED ? 26 : 19;
        for (int i = 0; i < tile.tier.processes; i++) {
            int cacheIndex = i;
            addButton(new GuiVerticalProgress(this, new IProgressInfoHandler() {
                @Override
                public double getProgress() {
                    return tile.getScaledProgress(1, cacheIndex);
                }
            }, resource, 4 + baseX + (i * baseXMult), 33));
        }

        for (Slot slot : container.inventorySlots) {
            GuiSlot slotElement;
            if (slot instanceof InventoryContainerSlot) {
                InventoryContainerSlot containerSlot = (InventoryContainerSlot) slot;
                ContainerSlotType slotType = containerSlot.getSlotType();
                if (slotType == ContainerSlotType.IGNORED) {
                    continue;
                }
                //Shift the slots by one as the elements include the border of the slot
                if (slotType == ContainerSlotType.INPUT) {
                    slotElement = new GuiSlot(SlotType.INPUT, this, resource, slot.xPos - 1, slot.yPos - 1);
                } else if (slotType == ContainerSlotType.OUTPUT) {
                    slotElement = new GuiSlot(SlotType.OUTPUT, this, resource, slot.xPos - 1, slot.yPos - 1);
                } else if (slotType == ContainerSlotType.POWER) {
                    slotElement = new GuiSlot(SlotType.POWER, this, resource, slot.xPos - 1, slot.yPos - 1).with(SlotOverlay.POWER);
                } else if (slotType == ContainerSlotType.EXTRA) {
                    slotElement = new GuiSlot(SlotType.EXTRA, this, resource, slot.xPos - 1, slot.yPos - 1);
                } else {//slotType == ContainerSlotType.NORMAL
                    slotElement = new GuiSlot(SlotType.NORMAL, this, resource, slot.xPos - 1, slot.yPos - 1);
                }
            } else {
                slotElement = new GuiSlot(SlotType.NORMAL, this, resource, slot.xPos - 1, slot.yPos - 1);
            }
            addButton(slotElement);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 4, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), tile.tier == FactoryTier.ULTIMATE ? 26 : 8,
              tile.hasSecondaryResourceBar() ? 85 : tile instanceof TileEntitySawingFactory ? 95 : 75, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tile.hasSecondaryResourceBar()) {
            if (button == 0 || InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                double xAxis = mouseX - getGuiLeft();
                double yAxis = mouseY - getGuiTop();
                //TODO: Hovering over the secondary bar??
                if (xAxis > 8 && xAxis < 168 && yAxis > 78 && yAxis < 83) {
                    ItemStack stack = minecraft.player.inventory.getItemStack();
                    if (!stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(1)));
                        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        //TODO: Make this instead generate background dynamically from the empty texture instead of having four blank textures
        if (tile.hasSecondaryResourceBar()) {
            if (tile.tier == FactoryTier.ULTIMATE) {
                return MekanismUtils.getResource(ResourceType.GUI, "wide_empty_tall.png");
            }
            return MekanismUtils.getResource(ResourceType.GUI, "empty_tall.png");
        }
        if (tile.tier == FactoryTier.ULTIMATE) {
            return MekanismUtils.getResource(ResourceType.GUI, "wide_empty.png");
        }
        return MekanismUtils.getResource(ResourceType.GUI, "null.png");
    }

    @Override
    public int getWidth() {
        return getXSize();
    }
}