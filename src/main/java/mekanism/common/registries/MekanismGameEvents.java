package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.GameEventDeferredRegister;
import net.minecraft.world.level.gameevent.GameEvent;

public class MekanismGameEvents {

    private MekanismGameEvents() {
    }

    public static final GameEventDeferredRegister GAME_EVENTS = new GameEventDeferredRegister(Mekanism.MODID);

    public static final MekanismDeferredHolder<GameEvent, GameEvent> SEISMIC_VIBRATION = GAME_EVENTS.register("seismic_vibration", 64);
    public static final MekanismDeferredHolder<GameEvent, GameEvent> JETPACK_BURN = GAME_EVENTS.register("jetpack_burn");
    public static final MekanismDeferredHolder<GameEvent, GameEvent> GRAVITY_MODULATE = GAME_EVENTS.register("gravity_modulate");
    public static final MekanismDeferredHolder<GameEvent, GameEvent> GRAVITY_MODULATE_BOOSTED = GAME_EVENTS.register("gravity_modulate_boosted", 32);
}