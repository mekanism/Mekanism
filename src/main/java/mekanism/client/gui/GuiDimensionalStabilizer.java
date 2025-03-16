package mekanism.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.BasicColorButton;
import mekanism.client.gui.element.button.TooltipColorButton;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiVisualsTab;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiDimensionalStabilizer extends GuiMekanismTile<TileEntityDimensionalStabilizer, MekanismTileContainer<TileEntityDimensionalStabilizer>> {

    //Note: We need a component that is a space so that the line doesn't get trimmed out when splitting to fit within the screen space
    private static final Component BLANK_LINE = CommonComponents.SPACE;

    public GuiDimensionalStabilizer(MekanismTileContainer<TileEntityDimensionalStabilizer> container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> {
                  MachineEnergyContainer<TileEntityDimensionalStabilizer> energyContainer = tile.getEnergyContainer();
                  return energyContainer.getEnergyPerTick() > energyContainer.getEnergy();
              });
        addRenderableWidget(new GuiVisualsTab(this, tile));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        int tileChunkX = SectionPos.blockToSectionCoord(tile.getBlockPos().getX());
        int tileChunkZ = SectionPos.blockToSectionCoord(tile.getBlockPos().getZ());
        for (int x = -TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; x <= TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; x++) {
            int shiftedX = x + TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS;
            int chunkX = tileChunkX + x;
            for (int z = -TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; z <= TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; z++) {
                int shiftedZ = z + TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS;
                int chunkZ = tileChunkZ + z;
                if (x == 0 && z == 0) {
                    addRenderableWidget(new BasicColorButton(this, 63 + 10 * shiftedX, 19 + 10 * shiftedZ, 10, () -> EnumColor.DARK_BLUE, (element, mouseX, mouseY) -> {
                        for (int i = 1; i <= TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; i++) {
                            if (hasAtRadius(i, false)) {
                                return PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.ENABLE_RADIUS_CHUNKLOAD, tile, i));
                            }
                        }
                        return false;
                    }, (element, mouseX, mouseY) -> {
                        for (int i = TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; i > 0; i--) {
                            if (hasAtRadius(i, true)) {
                                return PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.DISABLE_RADIUS_CHUNKLOAD, tile, i));
                            }
                        }
                        return false;
                    }) {
                        private List<Component> lastInfo = Collections.emptyList();
                        @Nullable
                        private Tooltip lastTooltip;

                        @Override
                        public void updateTooltip(int mouseX, int mouseY) {
                            List<Component> tooltips = new ArrayList<>();
                            tooltips.add(MekanismLang.STABILIZER_CENTER.translate(EnumColor.INDIGO, chunkX, EnumColor.INDIGO, chunkZ));
                            //TODO: Can we eventually optimize this further such as if we know that we have 1 enabled as we are enabling either radius 2 or "3" (nothing)
                            // then even if nothing is enabled at radius 2 currently, we don't have to check radius 1 to know that we should display the text for disabling it
                            // for now it doesn't really matter as given we only support a radius of two it only checks at most the inner radius (8 extra boolean lookups)
                            for (int i = 1; i <= TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; i++) {
                                if (hasAtRadius(i, false)) {
                                    tooltips.add(BLANK_LINE);//Add an empty line for readability
                                    tooltips.add(MekanismLang.STABILIZER_ENABLE_RADIUS.translate(EnumColor.INDIGO, i, EnumColor.INDIGO, chunkX, EnumColor.INDIGO, chunkZ));
                                    break;
                                }
                            }
                            for (int i = TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; i > 0; i--) {
                                if (hasAtRadius(i, true)) {
                                    tooltips.add(BLANK_LINE);//Add an empty line for readability
                                    tooltips.add(MekanismLang.STABILIZER_DISABLE_RADIUS.translate(EnumColor.INDIGO, i, EnumColor.INDIGO, chunkX, EnumColor.INDIGO, chunkZ));
                                    break;
                                }
                            }
                            if (!tooltips.equals(lastInfo)) {
                                lastInfo = tooltips;
                                lastTooltip = TooltipUtils.create(tooltips);
                            }
                            setTooltip(lastTooltip);
                        }
                    });
                } else {
                    int packetTarget = shiftedX * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER + shiftedZ;
                    addRenderableWidget(new TooltipColorButton(this, 63 + 10 * shiftedX, 19 + 10 * shiftedZ, 10, EnumColor.DARK_BLUE,
                          () -> tile.isChunkLoadingAt(shiftedX, shiftedZ),
                          (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.TOGGLE_CHUNKLOAD, tile, packetTarget)),
                          MekanismLang.STABILIZER_TOGGLE_LOADING.translate(OnOff.ON_COLORED, EnumColor.INDIGO, chunkX, EnumColor.INDIGO, chunkZ),
                          MekanismLang.STABILIZER_TOGGLE_LOADING.translate(OnOff.OFF_COLORED, EnumColor.INDIGO, chunkX, EnumColor.INDIGO, chunkZ)
                    ));
                }
            }
        }
        addRenderableWidget(new GuiUpArrow(this, 52, 28));
    }

    private boolean hasAtRadius(int radius, boolean state) {
        for (int x = -radius; x <= radius; x++) {
            boolean skipInner = x > -radius && x < radius;
            int actualX = x + TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS;
            for (int z = -radius; z <= radius; z += skipInner ? 2 * radius : 1) {
                if (tile.isChunkLoadingAt(actualX, z + TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS) == state) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        drawScrollingString(guiGraphics, MekanismLang.NORTH_SHORT.translate(), 49, 41, TextAlignment.CENTER, titleTextColor(), 15, 2, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
