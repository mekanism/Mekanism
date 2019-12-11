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
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
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
        if (tileEntity.hasSecondaryResourceBar()) {
            ySize += 11;
        }
        if (tileEntity.tier == FactoryTier.ULTIMATE) {
            xSize += 34;
        }
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiUpgradeTab(this, tileEntity, resource));
        addButton(new GuiRecipeType(this, tileEntity, resource));
        addButton(new GuiSideConfigurationTab(this, tileEntity, resource));
        addButton(new GuiTransporterConfigTab(this, tileEntity, resource));
        addButton(new GuiSortingTab(this, tileEntity, resource));
        addButton(new GuiVerticalPowerBar(this, tileEntity, resource, xSize - 12, 16));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.using"), ": ", EnergyDisplay.of(tileEntity.lastUsage), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy()))
        ), this, resource));

        if (tileEntity.hasSecondaryResourceBar()) {
            ChemicalInfoProvider<? extends Chemical<?>> provider = null;
            if (tileEntity instanceof TileEntityMetallurgicInfuserFactory) {
                provider = GuiVerticalChemicalBar.getProvider(((TileEntityMetallurgicInfuserFactory) tileEntity).getInfusionTank());
            } else if (tileEntity instanceof TileEntityItemStackGasToItemStackFactory) {
                provider = GuiVerticalChemicalBar.getProvider(((TileEntityItemStackGasToItemStackFactory) tileEntity).getGasTank());
            }
            if (provider != null) {
                int barX = tileEntity.tier == FactoryTier.ULTIMATE ? 25 : 7;
                addButton(new GuiHorizontalChemicalBar<>(this, provider, resource, barX, 76, 140));
                //TODO: Move left and make wider for ultimate factory??
                addButton(new GuiDumpButton<>(this, tileEntity, resource, barX + 141, 76,
                      () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(1)))));
            }
        }

        int baseX = tileEntity.tier == FactoryTier.BASIC ? 55 : tileEntity.tier == FactoryTier.ADVANCED ? 35 : tileEntity.tier == FactoryTier.ELITE ? 29 : 27;
        int baseXMult = tileEntity.tier == FactoryTier.BASIC ? 38 : tileEntity.tier == FactoryTier.ADVANCED ? 26 : 19;
        for (int i = 0; i < tileEntity.tier.processes; i++) {
            int cacheIndex = i;
            addButton(new GuiVerticalProgress(this, new IProgressInfoHandler() {
                @Override
                public double getProgress() {
                    return tileEntity.getScaledProgress(1, cacheIndex);
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
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 4, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), tileEntity.tier == FactoryTier.ULTIMATE ? 26 : 8,
              tileEntity.hasSecondaryResourceBar() ? 85 : tileEntity instanceof TileEntitySawingFactory ? 95 : 75, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tileEntity.hasSecondaryResourceBar()) {
            if (button == 0 || InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                double xAxis = mouseX - guiLeft;
                double yAxis = mouseY - guiTop;
                //TODO: Hovering over the secondary bar??
                if (xAxis > 8 && xAxis < 168 && yAxis > 78 && yAxis < 83) {
                    ItemStack stack = minecraft.player.inventory.getItemStack();
                    if (!stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(1)));
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
        if (tileEntity.hasSecondaryResourceBar()) {
            if (tileEntity.tier == FactoryTier.ULTIMATE) {
                return MekanismUtils.getResource(ResourceType.GUI, "wide_empty_tall.png");
            }
            return MekanismUtils.getResource(ResourceType.GUI, "empty_tall.png");
        }
        if (tileEntity.tier == FactoryTier.ULTIMATE) {
            return MekanismUtils.getResource(ResourceType.GUI, "wide_empty.png");
        }
        return MekanismUtils.getResource(ResourceType.GUI, "null.png");
    }

    @Override
    public int getWidth() {
        return xSize;
    }
}