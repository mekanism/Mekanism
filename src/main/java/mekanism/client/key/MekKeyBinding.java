package mekanism.client.key;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.MekanismLang;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class MekKeyBinding extends KeyBinding {

    @Nullable
    private final BiConsumer<KeyBinding, Boolean> onKeyDown;
    @Nullable
    private final Consumer<KeyBinding> onKeyUp;
    private final boolean repeating;
    private boolean lastState;

    private MekKeyBinding(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputMappings.Input key, String category,
          @Nullable BiConsumer<KeyBinding, Boolean> onKeyDown, @Nullable Consumer<KeyBinding> onKeyUp, boolean repeating) {
        super(description, keyConflictContext, keyModifier, key, category);
        this.onKeyDown = onKeyDown;
        this.onKeyUp = onKeyUp;
        this.repeating = repeating;
    }

    @Override
    public void setDown(boolean value) {
        super.setDown(value);
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

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class MekBindingBuilder {

        private String description;
        private IKeyConflictContext keyConflictContext = KeyConflictContext.UNIVERSAL;
        private KeyModifier keyModifier = KeyModifier.NONE;
        private InputMappings.Input key;
        private String category = MekanismLang.MEKANISM.getTranslationKey();
        private BiConsumer<KeyBinding, Boolean> onKeyDown;
        private Consumer<KeyBinding> onKeyUp;
        private boolean repeating;

        public MekBindingBuilder description(IHasTranslationKey description) {
            return description(Objects.requireNonNull(description, "Description cannot be null.").getTranslationKey());
        }

        public MekBindingBuilder description(String description) {
            this.description = Objects.requireNonNull(description, "Description cannot be null.");
            return this;
        }

        public MekBindingBuilder conflictInGame() {
            return conflictContext(KeyConflictContext.IN_GAME);
        }

        public MekBindingBuilder conflictInGui() {
            return conflictContext(KeyConflictContext.GUI);
        }

        public MekBindingBuilder conflictContext(IKeyConflictContext keyConflictContext) {
            this.keyConflictContext = Objects.requireNonNull(keyConflictContext, "Key conflict context cannot be null.");
            return this;
        }

        public MekBindingBuilder modifier(KeyModifier keyModifier) {
            this.keyModifier = Objects.requireNonNull(keyModifier, "Key modifier cannot be null.");
            return this;
        }

        public MekBindingBuilder keyCode(int keyCode) {
            return keyCode(InputMappings.Type.KEYSYM, keyCode);
        }

        public MekBindingBuilder keyCode(InputMappings.Type keyType, int keyCode) {
            Objects.requireNonNull(keyType, "Key type cannot be null.");
            return keyCode(keyType.getOrCreate(keyCode));
        }

        public MekBindingBuilder keyCode(InputMappings.Input key) {
            this.key = Objects.requireNonNull(key, "Key cannot be null.");
            return this;
        }

        public MekBindingBuilder category(IHasTranslationKey category) {
            return category(Objects.requireNonNull(category, "Category cannot be null.").getTranslationKey());
        }

        public MekBindingBuilder category(String category) {
            this.category = Objects.requireNonNull(category, "Category cannot be null.");
            return this;
        }

        public MekBindingBuilder onKeyDown(BiConsumer<KeyBinding, Boolean> onKeyDown) {
            this.onKeyDown = Objects.requireNonNull(onKeyDown, "On key down cannot be null when manually specified.");
            return this;
        }

        public MekBindingBuilder onKeyUp(Consumer<KeyBinding> onKeyUp) {
            this.onKeyUp = Objects.requireNonNull(onKeyUp, "On key up cannot be null when manually specified.");
            return this;
        }

        public MekBindingBuilder repeating() {
            this.repeating = true;
            return this;
        }

        public KeyBinding build() {
            return new MekKeyBinding(
                  Objects.requireNonNull(description, "Description has not been set."),
                  keyConflictContext,
                  keyModifier,
                  Objects.requireNonNull(key, "Key has not been set"),
                  category,
                  onKeyDown,
                  onKeyUp,
                  repeating
            );
        }
    }
}