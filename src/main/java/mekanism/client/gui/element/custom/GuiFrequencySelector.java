package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiConfirmationDialog;
import mekanism.client.gui.element.window.GuiConfirmationDialog.DialogType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.item.FrequencyItemContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.network.to_server.PacketGuiSetFrequency;
import mekanism.common.network.to_server.PacketGuiSetFrequency.FrequencyUpdate;
import mekanism.common.network.to_server.PacketGuiSetFrequencyColor;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;

public class GuiFrequencySelector<FREQ extends Frequency> extends GuiElement {

    private final IGuiFrequencySelector<FREQ> frequencySelector;
    private final MekanismButton publicButton;
    private final MekanismButton privateButton;
    private final MekanismButton setButton;
    private final MekanismButton deleteButton;
    private final GuiTextScrollList scrollList;
    private final GuiTextField frequencyField;
    private final int yStart;
    //Used to keep track of the last list of frequencies, by taking advantage of container sync providing a new list object
    // each time things are synced currently so that we can just do an identity compare
    private List<FREQ> lastFrequencies = Collections.emptyList();
    private boolean publicFreq = true;
    private boolean init;

    public <SELECTOR extends IGuiWrapper & IGuiFrequencySelector<FREQ>> GuiFrequencySelector(SELECTOR frequencySelector, int yStart) {
        super(frequencySelector, 27, yStart, 132, 121);
        this.frequencySelector = frequencySelector;
        this.yStart = yStart;
        boolean hasColor = frequencySelector instanceof IGuiColorFrequencySelector;
        scrollList = addChild(new GuiTextScrollList(frequencySelector, 27, yStart + 22, 122, 42));
        publicButton = addChild(new TranslationButton(frequencySelector, 27, yStart, 60, 20, APILang.PUBLIC, () -> {
            this.publicFreq = true;
            this.scrollList.clearSelection();
            updateButtons();
        }));
        privateButton = addChild(new TranslationButton(frequencySelector, 89, yStart, 60, 20, APILang.PRIVATE, () -> {
            this.publicFreq = false;
            this.scrollList.clearSelection();
            updateButtons();
        }));
        int buttonWidth = hasColor ? 50 : 60;
        setButton = addChild(new TranslationButton(frequencySelector, 27, yStart + 113, buttonWidth, 18,
              MekanismLang.BUTTON_SET, () -> {
            int selection = this.scrollList.getSelection();
            if (selection != -1) {
                Frequency frequency = getFrequencies().get(selection);
                setFrequency(frequency.getName());
            }
            //Note: We update the buttons regardless so that if something went wrong, and we don't have a selection
            // we will disable the ability to press the set button
            updateButtons();
        }));
        deleteButton = addChild(new TranslationButton(frequencySelector, 29 + buttonWidth, yStart + 113, buttonWidth, 18,
              MekanismLang.BUTTON_DELETE, () -> GuiConfirmationDialog.show(gui(), MekanismLang.FREQUENCY_DELETE_CONFIRM.translate(), () -> {
            int selection = this.scrollList.getSelection();
            if (selection != -1) {
                Frequency frequency = getFrequencies().get(selection);
                this.frequencySelector.sendRemoveFrequency(frequency.getIdentity());
                this.scrollList.clearSelection();
            }
            //Note: We update the buttons regardless so that if something went wrong, and we don't have a selection
            // we will disable the ability to press the delete button
            updateButtons();
        }, DialogType.DANGER)));
        if (hasColor) {
            addChild(new GuiSlot(SlotType.NORMAL, frequencySelector, 131, yStart + 113).setRenderAboveSlots());
            IGuiColorFrequencySelector<?> colorFrequencySelector = (IGuiColorFrequencySelector<?>) frequencySelector;
            addChild(new ColorButton(frequencySelector, 132, yStart + 114, 16, 16, () -> {
                IColorableFrequency frequency = colorFrequencySelector.getFrequency();
                return frequency == null ? null : frequency.getColor();
            }, () -> colorFrequencySelector.sendColorUpdate(true), () -> colorFrequencySelector.sendColorUpdate(false)));
        }
        frequencyField = addChild(new GuiTextField(frequencySelector, 50, yStart + 99, 98, 11));
        frequencyField.setMaxLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setBackground(BackgroundType.INNER_SCREEN);
        frequencyField.setEnterHandler(this::setFrequency);
        frequencyField.setInputValidator(InputValidator.LETTER_OR_DIGIT.or(InputValidator.FREQUENCY_CHARS));
        frequencyField.addCheckmarkButton(this::setFrequency);
        //Disable buttons that shouldn't be enabled to start due to there being no data received yet
        publicButton.active = false;
        setButton.active = false;
        deleteButton.active = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!init) {
            init = true;
            //Delay setting whether we are currently in private or public mode for a tick until we had time to sync
            // the selected frequency to the client
            FREQ frequency = frequencySelector.getFrequency();
            if (frequency != null) {
                publicFreq = frequency.isPublic();
            }
        }
        //TODO: Remove the need for this to happen each tick? For starters we managed to reduce the amount
        // of logic it runs, but it would be best to reduce it needing to validate if things changed each
        // tick to making it only run when things change
        updateButtons();
    }

    private void updateButtons() {
        List<FREQ> frequencies = getFrequencies();
        if (lastFrequencies != frequencies) {
            //If the frequencies changed
            lastFrequencies = frequencies;
            List<String> text = new ArrayList<>(frequencies.size());
            for (Frequency freq : frequencies) {
                if (publicFreq) {
                    text.add(freq.getName() + " (" + freq.getClientOwner() + ")");
                } else {
                    text.add(freq.getName());
                }
            }
            scrollList.setText(text);
        }
        if (publicFreq) {
            publicButton.active = false;
            privateButton.active = true;
        } else {
            publicButton.active = true;
            privateButton.active = false;
        }
        if (scrollList.hasSelection()) {
            FREQ selectedFrequency = frequencies.get(scrollList.getSelection());
            FREQ currentFrequency = frequencySelector.getFrequency();
            //Enable the set button if there is not currently a frequency set or the selected frequency
            // is different from the one that is currently set
            setButton.active = currentFrequency == null || !currentFrequency.equals(selectedFrequency);
            //Enable the delete button if the player is the owner of the frequency
            deleteButton.active = Minecraft.getInstance().player != null && selectedFrequency.ownerMatches(Minecraft.getInstance().player.getUUID());
        } else {
            setButton.active = false;
            deleteButton.active = false;
        }
        //TODO: Rework this as it only really is needed when the status changes or the frequency changes
        frequencySelector.buttonsUpdated();
    }

    private List<FREQ> getFrequencies() {
        return publicFreq ? frequencySelector.getPublicFrequencies() : frequencySelector.getPrivateFrequencies();
    }

    private void setFrequency() {
        setFrequency(frequencyField.getText());
        frequencyField.setText("");
        updateButtons();
    }

    private void setFrequency(String freq) {
        if (!freq.isEmpty()) {
            frequencySelector.sendSetFrequency(new FrequencyIdentity(freq, publicFreq));
        }
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        FREQ frequency = frequencySelector.getFrequency();
        if (frequency == null) {
            MutableComponent noneComponent = MekanismLang.NONE.translateColored(EnumColor.DARK_RED);
            drawString(matrix, MekanismLang.FREQUENCY.translate(noneComponent), 27, yStart + 67, titleTextColor());
            drawString(matrix, MekanismLang.OWNER.translate(noneComponent), 27, yStart + 77, titleTextColor());
            drawString(matrix, MekanismLang.SECURITY.translate(noneComponent), 27, yStart + 87, titleTextColor());
        } else {
            //Color the name the same as the subheading text color should be
            MutableComponent name = TextComponentUtil.color(TextComponentUtil.getString(frequency.getName()), subheadingTextColor());
            drawTextScaledBound(matrix, MekanismLang.FREQUENCY.translate(name), 27, yStart + 67, titleTextColor(), getGuiWidth() - 36);
            drawString(matrix, OwnerDisplay.of(Minecraft.getInstance().player, frequency.getOwner(), frequency.getClientOwner(), false).getTextComponent(),
                  27, yStart + 77, titleTextColor());
            drawString(matrix, MekanismLang.SECURITY.translate(frequency.getSecurity()), 27, yStart + 87, titleTextColor());
        }
        drawTextScaledBound(matrix, MekanismLang.SET.translate(), 27, yStart + 100, titleTextColor(), 20);
    }

    public interface IGuiFrequencySelector<FREQ extends Frequency> {

        FrequencyType<FREQ> getFrequencyType();

        void sendSetFrequency(FrequencyIdentity identity);

        void sendRemoveFrequency(FrequencyIdentity identity);

        @Nullable
        FREQ getFrequency();

        List<FREQ> getPublicFrequencies();

        List<FREQ> getPrivateFrequencies();

        default void buttonsUpdated() {
        }
    }

    public interface IGuiColorFrequencySelector<FREQ extends Frequency & IColorableFrequency> extends IGuiFrequencySelector<FREQ> {

        default void sendColorUpdate(boolean next) {
            FREQ freq = getFrequency();
            if (freq != null) {
                Mekanism.packetHandler().sendToServer(PacketGuiSetFrequencyColor.create(freq, next));
            }
        }
    }

    public interface ITileGuiFrequencySelector<FREQ extends Frequency, TILE extends TileEntityMekanism & IFrequencyHandler> extends IGuiFrequencySelector<FREQ> {

        TILE getTileEntity();

        @Override
        default void sendSetFrequency(FrequencyIdentity identity) {
            Mekanism.packetHandler().sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.SET_TILE, getFrequencyType(), identity, getTileEntity().getBlockPos()));
        }

        @Override
        default void sendRemoveFrequency(FrequencyIdentity identity) {
            Mekanism.packetHandler().sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.REMOVE_TILE, getFrequencyType(), identity, getTileEntity().getBlockPos()));
        }

        @Override
        default FREQ getFrequency() {
            return getTileEntity().getFrequency(getFrequencyType());
        }

        @Override
        default List<FREQ> getPublicFrequencies() {
            return getTileEntity().getPublicCache(getFrequencyType());
        }

        @Override
        default List<FREQ> getPrivateFrequencies() {
            return getTileEntity().getPrivateCache(getFrequencyType());
        }
    }

    public interface IItemGuiFrequencySelector<FREQ extends Frequency, CONTAINER extends FrequencyItemContainer<FREQ>> extends IGuiFrequencySelector<FREQ> {

        CONTAINER getFrequencyContainer();

        @Override
        default void sendSetFrequency(FrequencyIdentity identity) {
            Mekanism.packetHandler().sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.SET_ITEM, getFrequencyType(), identity, getFrequencyContainer().getHand()));
        }

        @Override
        default void sendRemoveFrequency(FrequencyIdentity identity) {
            Mekanism.packetHandler().sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.REMOVE_ITEM, getFrequencyType(), identity, getFrequencyContainer().getHand()));
        }

        @Override
        default FREQ getFrequency() {
            return getFrequencyContainer().getFrequency();
        }

        @Override
        default List<FREQ> getPublicFrequencies() {
            return getFrequencyContainer().getPublicCache();
        }

        @Override
        default List<FREQ> getPrivateFrequencies() {
            return getFrequencyContainer().getPrivateCache();
        }
    }
}