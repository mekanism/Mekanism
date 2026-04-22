package mekanism.client.gui.tooltip;

import java.util.List;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;

class MultiLineTooltip {

    //TODO - 26.1: test this works
    static Tooltip create(List<Component> message) {
        MutableComponent parent = MutableComponent.create(new LiteralContents(""));
        for (Component component : message) {
            if (!parent.getSiblings().isEmpty()) {
                parent.append(MutableComponent.create(new LiteralContents("\n")));
            }
            parent.append(component);
        }
        return Tooltip.create(parent);
    }
}