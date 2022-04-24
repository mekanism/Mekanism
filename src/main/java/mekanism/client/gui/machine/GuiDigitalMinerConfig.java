package mekanism.client.gui.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiFilterHolder;
import mekanism.client.gui.element.GuiDigitalSwitch;
import mekanism.client.gui.element.GuiDigitalSwitch.SwitchType;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.filter.miner.GuiMinerFilerSelect;
import mekanism.client.gui.element.window.filter.miner.GuiMinerItemStackFilter;
import mekanism.client.gui.element.window.filter.miner.GuiMinerMaterialFilter;
import mekanism.client.gui.element.window.filter.miner.GuiMinerModIDFilter;
import mekanism.client.gui.element.window.filter.miner.GuiMinerTagFilter;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostBlockItemConsumer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerMaterialFilter;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteractionItem;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class GuiDigitalMinerConfig extends GuiFilterHolder<MinerFilter<?>, TileEntityDigitalMiner, MekanismTileContainer<TileEntityDigitalMiner>> {

    private static final ResourceLocation INVERSE = MekanismUtils.getResource(ResourceType.GUI, "switch/inverse.png");

    private final int maxHeightLength;
    private GuiTextField radiusField, minField, maxField;

    public GuiDigitalMinerConfig(MekanismTileContainer<TileEntityDigitalMiner> container, Inventory inv, Component title) {
        super(container, inv, title);
        Level level = inv.player.level;
        maxHeightLength = Math.max(Integer.toString(level.getMinBuildHeight()).length(), Integer.toString(level.getMaxBuildHeight() - 1).length());
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new TranslationButton(this, 56, 136, 96, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> addWindow(new GuiMinerFilerSelect(this, tile))));
        addRenderableWidget(new MekanismImageButton(this, 5, 5, 11, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile))));
        addRenderableWidget(new GuiDigitalSwitch(this, 10, 115, INVERSE, tile::getInverse, MekanismLang.MINER_INVERSE.translate(),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.INVERSE_BUTTON, tile)), SwitchType.LEFT_ICON));
        addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 13, 135)).setRenderAboveSlots().setRenderHover(true)
              .stored(() -> new ItemStack(tile.getInverseReplaceTarget())).click((element, mouseX, mouseY) -> {
                  if (Screen.hasShiftDown()) {
                      updateInverseReplaceTarget(Items.AIR);
                  } else {
                      ItemStack stack = minecraft.player.containerMenu.getCarried();
                      if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                          updateInverseReplaceTarget(stack.getItem());
                      }
                  }
              }).setGhostHandler((IGhostBlockItemConsumer) ingredient -> updateInverseReplaceTarget(((ItemStack) ingredient).getItem()));
        addRenderableWidget(new MekanismImageButton(this, 35, 137, 14, 16, getButtonLocation("exclamation"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.INVERSE_REQUIRES_REPLACEMENT_BUTTON, tile)),
              getOnHover(() -> MekanismLang.MINER_REQUIRE_REPLACE_INVERSE.translate(YesNo.of(tile.getInverseRequiresReplacement())))));
        radiusField = addRenderableWidget(new GuiTextField(this, 13, 49, 38, 11));
        radiusField.setMaxLength(Integer.toString(MekanismConfig.general.minerMaxRadius.get()).length());
        radiusField.setInputValidator(InputValidator.DIGIT);
        radiusField.configureDigitalBorderInput(() -> setText(radiusField, GuiInteraction.SET_RADIUS));
        minField = addRenderableWidget(new GuiTextField(this, 13, 74, 38, 11));
        minField.setMaxLength(maxHeightLength);
        minField.setInputValidator(InputValidator.DIGIT_OR_NEGATIVE);
        minField.configureDigitalBorderInput(() -> setText(minField, GuiInteraction.SET_MIN_Y));
        maxField = addRenderableWidget(new GuiTextField(this, 13, 99, 38, 11));
        maxField.setMaxLength(maxHeightLength);
        maxField.setInputValidator(InputValidator.DIGIT_OR_NEGATIVE);
        maxField.configureDigitalBorderInput(() -> setText(maxField, GuiInteraction.SET_MAX_Y));
        // Note: We add this after all the buttons have their warnings added so that it is further down the tracker
        // so the tracker can short circuit on this type of warning and not have to check all the filters if one of
        // the ones that are currently being shown has the warning
        trackWarning(WarningType.FILTER_HAS_BLACKLISTED_ELEMENT, () -> tile.getFilters().stream().anyMatch(MinerFilter::hasBlacklistedElement));
    }

    private void updateInverseReplaceTarget(Item target) {
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteractionItem.DIGITAL_MINER_INVERSE_REPLACE_ITEM, tile, new ItemStack(target)));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    protected void addGenericTabs() {
        //Don't add the generic tabs when we are in the miner config
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.drawForegroundText(matrix, mouseX, mouseY);
        renderTitleText(matrix);
        drawScaledTextScaledBound(matrix, MekanismLang.FILTERS.translate(), 14, 22, screenTextColor(), 36, 0.8F);
        drawScaledTextScaledBound(matrix, MekanismLang.FILTER_COUNT.translate(getFilters().size()), 14, 31, screenTextColor(), 36, 0.8F);
        drawScaledTextScaledBound(matrix, MekanismLang.MINER_RADIUS.translate(tile.getRadius()), 14, 40, screenTextColor(), 36, 0.8F);
        drawScaledTextScaledBound(matrix, MekanismLang.MIN.translate(tile.getMinY()), 14, 65, screenTextColor(), 36, 0.8F);
        drawScaledTextScaledBound(matrix, MekanismLang.MAX.translate(tile.getMaxY()), 14, 90, screenTextColor(), 36, 0.8F);
    }

    @Override
    public void drawTitleText(PoseStack matrix, Component text, float y) {
        //Adjust spacing for back button
        int leftShift = 11;
        int xSize = getXSize() - leftShift;
        int maxLength = xSize - 12;
        float textWidth = getStringWidth(text);
        float scale = Math.min(1, maxLength / textWidth);
        drawScaledCenteredText(matrix, text, leftShift + xSize / 2F, y, titleTextColor(), scale);
    }

    @Override
    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof IItemStackFilter) {
            addWindow(GuiMinerItemStackFilter.edit(this, tile, (MinerItemStackFilter) filter));
        } else if (filter instanceof ITagFilter) {
            addWindow(GuiMinerTagFilter.edit(this, tile, (MinerTagFilter) filter));
        } else if (filter instanceof IMaterialFilter) {
            addWindow(GuiMinerMaterialFilter.edit(this, tile, (MinerMaterialFilter) filter));
        } else if (filter instanceof IModIDFilter) {
            addWindow(GuiMinerModIDFilter.edit(this, tile, (MinerModIDFilter) filter));
        }
    }

    @Override
    protected FilterButton addFilterButton(FilterButton button) {
        return super.addFilterButton(button).warning(WarningType.FILTER_HAS_BLACKLISTED_ELEMENT, filter -> filter instanceof MinerFilter<?> minerFilter && minerFilter.hasBlacklistedElement());
    }

    private void setText(GuiTextField field, GuiInteraction interaction) {
        if (!field.getText().isEmpty()) {
            try {
                Mekanism.packetHandler().sendToServer(new PacketGuiInteract(interaction, tile, Integer.parseInt(field.getText())));
            } catch (NumberFormatException ignored) {//Might not be valid if multiple negative signs
            }
            field.setText("");
        }
    }

    @Override
    protected List<ItemStack> getTagStacks(String tagName) {
        return TagCache.getBlockTagStacks(tagName).stacks();
    }

    @Override
    protected List<ItemStack> getModIDStacks(String tagName) {
        return TagCache.getBlockModIDStacks(tagName).stacks();
    }
}