package mekanism.api.recipes.display.slot;

import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Wrapping slot display that represents a display that only has a chance of being produced.
///
/// @param source Base slot display.
/// @param chance Chance of the display's result being created.
///
/// @since 10.8.0
public record ChanceSlotDisplay(SlotDisplay source, double chance) implements SlotDisplay {

    private static final DeferredHolder<Type<?>, Type<ChanceSlotDisplay>> TYPE = DeferredHolder.create(Registries.SLOT_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chance"));

    @Override
    public Type<ChanceSlotDisplay> type() {
        return TYPE.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        //TODO - 26.2: Figure out how we want to represent the chance
        return source.resolve(context, factory);
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.source.isEnabled(enabledFeatures);
    }

    /// Helper method to create a slot display based on the given chance.
    ///
    /// @param display Base slot display.
    /// @param chance Chance of the display's result being created.
    public static SlotDisplay create(SlotDisplay display, double chance) {
        if (chance <= 0) {
            return SlotDisplay.Empty.INSTANCE;
        } else if (chance == 1) {
            return display;
        }
        return new ChanceSlotDisplay(display, chance);
    }
}