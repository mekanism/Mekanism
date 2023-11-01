package mekanism.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class MekKeyBinding extends KeyMapping {

    @Nullable
    private final BiConsumer<KeyMapping, Boolean> onKeyDown;
    @Nullable
    private final Consumer<KeyMapping> onKeyUp;
    @Nullable
    private final BooleanSupplier toggleable;
    private final boolean repeating;
    private boolean lastState;

    MekKeyBinding(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key key, String category,
          @Nullable BiConsumer<KeyMapping, Boolean> onKeyDown, @Nullable Consumer<KeyMapping> onKeyUp, @Nullable BooleanSupplier toggleable, boolean repeating) {
        super(description, keyConflictContext, keyModifier, key, category);
        this.onKeyDown = onKeyDown;
        this.onKeyUp = onKeyUp;
        this.toggleable = toggleable;
        this.repeating = repeating;
    }

    private boolean isToggleable() {
        return toggleable != null && toggleable.getAsBoolean();
    }

    @Override
    public void setDown(boolean value) {
        if (isToggleable()) {
            //If it is a toggleable keybinding mimic the behavior of vanilla's toggleable keybinding
            if (value && isConflictContextAndModifierActive()) {
                super.setDown(!this.isDown());
            }
        } else {
            super.setDown(value);
        }
        //Note: We check the state based on isDown instead of value, as the value may be wrong depending on the conflict context
        boolean state = isDown();
        if (state != lastState || (state && repeating)) {
            if (state) {
                if (onKeyDown != null) {
                    onKeyDown.accept(this, lastState);
                }
            } else if (onKeyUp != null) {
                onKeyUp.accept(this);
            }
            lastState = state;
        }
    }

    @Override
    public boolean isDown() {
        return isDown && (isConflictContextAndModifierActive() || isToggleable());
    }
}