package mekanism.client.key;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.MekanismLang;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MekKeyBindingBuilder {

    private String description;
    private IKeyConflictContext keyConflictContext = KeyConflictContext.UNIVERSAL;
    private KeyModifier keyModifier = KeyModifier.NONE;
    private InputMappings.Input key;
    private String category = MekanismLang.MEKANISM.getTranslationKey();
    private BiConsumer<KeyBinding, Boolean> onKeyDown;
    private Consumer<KeyBinding> onKeyUp;
    private BooleanSupplier toggleable;
    private boolean repeating;

    public MekKeyBindingBuilder description(IHasTranslationKey description) {
        return description(Objects.requireNonNull(description, "Description cannot be null.").getTranslationKey());
    }

    public MekKeyBindingBuilder description(String description) {
        this.description = Objects.requireNonNull(description, "Description cannot be null.");
        return this;
    }

    public MekKeyBindingBuilder conflictInGame() {
        return conflictContext(KeyConflictContext.IN_GAME);
    }

    public MekKeyBindingBuilder conflictInGui() {
        return conflictContext(KeyConflictContext.GUI);
    }

    public MekKeyBindingBuilder conflictContext(IKeyConflictContext keyConflictContext) {
        this.keyConflictContext = Objects.requireNonNull(keyConflictContext, "Key conflict context cannot be null.");
        return this;
    }

    public MekKeyBindingBuilder modifier(KeyModifier keyModifier) {
        this.keyModifier = Objects.requireNonNull(keyModifier, "Key modifier cannot be null.");
        return this;
    }

    public MekKeyBindingBuilder keyCode(int keyCode) {
        return keyCode(InputMappings.Type.KEYSYM, keyCode);
    }

    public MekKeyBindingBuilder keyCode(InputMappings.Type keyType, int keyCode) {
        Objects.requireNonNull(keyType, "Key type cannot be null.");
        return keyCode(keyType.getOrCreate(keyCode));
    }

    public MekKeyBindingBuilder keyCode(InputMappings.Input key) {
        this.key = Objects.requireNonNull(key, "Key cannot be null.");
        return this;
    }

    public MekKeyBindingBuilder category(IHasTranslationKey category) {
        return category(Objects.requireNonNull(category, "Category cannot be null.").getTranslationKey());
    }

    public MekKeyBindingBuilder category(String category) {
        this.category = Objects.requireNonNull(category, "Category cannot be null.");
        return this;
    }

    public MekKeyBindingBuilder onKeyDown(BiConsumer<KeyBinding, Boolean> onKeyDown) {
        this.onKeyDown = Objects.requireNonNull(onKeyDown, "On key down cannot be null when manually specified.");
        return this;
    }

    public MekKeyBindingBuilder onKeyUp(Consumer<KeyBinding> onKeyUp) {
        this.onKeyUp = Objects.requireNonNull(onKeyUp, "On key up cannot be null when manually specified.");
        return this;
    }

    public MekKeyBindingBuilder toggleable() {
        return toggleable(() -> true);
    }

    public MekKeyBindingBuilder toggleable(BooleanSupplier toggleable) {
        this.toggleable = Objects.requireNonNull(toggleable, "Toggleable supplier cannot be null when manually specified.");
        return this;
    }

    public MekKeyBindingBuilder repeating() {
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
              toggleable,
              repeating
        );
    }
}