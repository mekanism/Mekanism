package mekanism.common.registries;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.GameEventDeferredRegister;
import mekanism.common.registration.impl.GameEventRegistryObject;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class MekanismGameEvents {

    private MekanismGameEvents() {
    }

    public static final GameEventDeferredRegister GAME_EVENTS = new GameEventDeferredRegister(Mekanism.MODID);

    public static final GameEventRegistryObject<GameEvent> SEISMIC_VIBRATION = GAME_EVENTS.register("seismic_vibration", 64);
    public static final GameEventRegistryObject<GameEvent> JETPACK_BURN = GAME_EVENTS.register("jetpack_burn");
    public static final GameEventRegistryObject<GameEvent> GRAVITY_MODULATE = GAME_EVENTS.register("gravity_modulate");
    public static final GameEventRegistryObject<GameEvent> GRAVITY_MODULATE_BOOSTED = GAME_EVENTS.register("gravity_modulate_boosted", 32);

    public static void addFrequencies() {
        if (VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT instanceof Object2IntOpenHashMap<GameEvent> frequencyForEvent) {
            //Follows vanilla's logic for what gives what sort of frequency
            // A frequency of four is used for gliding with an elytra or unique mob actions
            frequencyForEvent.put(JETPACK_BURN.get(), 4);
            frequencyForEvent.put(GRAVITY_MODULATE.get(), 4);
            //Note: We use 5 for boosted modulation to be able to tell it apart easier from normal modulating
            frequencyForEvent.put(GRAVITY_MODULATE_BOOSTED.get(), 5);
            // A frequency of ten is for blocks activating
            frequencyForEvent.put(SEISMIC_VIBRATION.get(), 10);
        }
    }
}