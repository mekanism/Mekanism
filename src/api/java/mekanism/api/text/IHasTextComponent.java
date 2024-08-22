package mekanism.api.text;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
public interface IHasTextComponent {

    //TODO - 1.18: Debate replacing this with returning IFormattableTextComponent so that we know it can be "modified" instead of having to copy it
    // when it is likely to just be a new object anyways and not something that is cached.
    // A better solution may be to make a IHasFormattableTextComponent that extends IHasTextComponent for things that we know it is a new instance?

    /**
     * Gets the text component that represents this object.
     */
    Component getTextComponent();

    /**
     * Helper interface that also implements Neo's TranslatableEnum interface
     * @since 10.7.3
     */
    interface IHasEnumNameTextComponent extends IHasTextComponent, TranslatableEnum {

        @NotNull
        @Override
        default Component getTranslatedName() {
            return getTextComponent();
        }
    }
}