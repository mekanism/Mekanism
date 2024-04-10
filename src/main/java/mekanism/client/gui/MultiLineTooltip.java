package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationThunk;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiLineTooltip extends Tooltip {

    private final List<Component> message;
    private final List<Component> narration;

    private MultiLineTooltip(List<Component> message) {
        this(message, message);
    }

    private MultiLineTooltip(List<Component> message, List<Component> narration) {
        super(message.get(0), narration.get(0));
        this.message = message;
        this.narration = narration;
    }

    public static Tooltip createMulti(Component... messages) {
        if (messages == null || messages.length == 0) {
            throw new IllegalArgumentException("Messages cannot be null or empty");
        } else if (messages.length == 1) {
            throw new IllegalArgumentException("Use normal tooltip instead");
        }
        return new MultiLineTooltip(List.of(messages));
    }

    @Nullable
    public static Tooltip createMulti(List<Component> messages) {
        if (messages.isEmpty()) {
            return null;
        } else if (messages.size() == 1) {
            return create(messages.get(0));
        }
        return new MultiLineTooltip(List.copyOf(messages));
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
        if (!this.narration.isEmpty()) {
            output.add(NarratedElementType.HINT, NarrationThunk.from(this.narration));
        }
    }

    @NotNull
    @Override
    public List<FormattedCharSequence> toCharSequence(@NotNull Minecraft minecraft) {
        if (this.cachedTooltip == null) {
            List<FormattedCharSequence> tooltip = new ArrayList<>();
            for (Component component : message) {
                tooltip.addAll(splitTooltip(minecraft, component));
            }
            this.cachedTooltip = List.copyOf(tooltip);
        }
        return this.cachedTooltip;
    }
}