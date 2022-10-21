package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.GameEventDeferredRegister;
import mekanism.common.registration.impl.GameEventRegistryObject;
import net.minecraft.world.level.gameevent.GameEvent;

public class MekanismGameEvents {

    private MekanismGameEvents() {
    }

    public static final GameEventDeferredRegister GAME_EVENTS = new GameEventDeferredRegister(Mekanism.MODID);

    //TODO: Eventually we may want to evaluate somehow adding these to SculkSensorBlock.VIBRATION_FREQUENCY_FOR_EVENT
    // so that they have different comparator levels
    public static final GameEventRegistryObject<GameEvent> SEISMIC_VIBRATION = GAME_EVENTS.register("seismic_vibration", 64);
    public static final GameEventRegistryObject<GameEvent> JETPACK_BURN = GAME_EVENTS.register("jetpack_burn");
    public static final GameEventRegistryObject<GameEvent> GRAVITY_MODULATE = GAME_EVENTS.register("gravity_modulate");
    public static final GameEventRegistryObject<GameEvent> GRAVITY_MODULATE_BOOSTED = GAME_EVENTS.register("gravity_modulate_boosted", 32);
}