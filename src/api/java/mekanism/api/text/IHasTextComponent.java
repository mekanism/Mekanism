package mekanism.api.text;

import net.minecraft.network.chat.Component;

public interface IHasTextComponent {

    //TODO - 1.18: Debate replacing this with returning IFormattableTextComponent so that we know it can be "modified" instead of having to copy it
    // when it is likely to just be a new object anyways and not something that is cached.
    // A better solution may be to make a IHasFormattableTextComponent that extends IHasTextComponent for things that we know it is a new instance?

    /**
     * Gets the text component that represents this object.
     */
    Component getTextComponent();
}