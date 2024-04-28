package mekanism.client.gui.tooltip;

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

class MultiLineTooltip extends Tooltip {

    private final List<Component> message;
    private final List<Component> narration;

    MultiLineTooltip(List<Component> message) {
        this(message, message);
    }

    private MultiLineTooltip(List<Component> message, List<Component> narration) {
        super(message.getFirst(), narration.getFirst());
        this.message = message;
        this.narration = narration;
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