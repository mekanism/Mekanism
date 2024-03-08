package mekanism.api.gear.config;

import mekanism.api.text.IHasTextComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Conveys that an option should be represented by the corresponding mode icon when displayed in the module tweaker.
 *
 * @apiNote Currently only supported for {@link ModuleEnumData}.
 * @since 10.5.3
 */
public interface IHasModeIcon extends IHasTextComponent {

    /**
     * @return The icon to use for displaying this mode.
     *
     * @implNote The file is currently expected to be 16x16, and be 5 wide, down two pixels and eight pixels tall. Ideally this will be made less strict, but for now just
     * look at how the jetpack icons are.
     */
    ResourceLocation getModeIcon();
}