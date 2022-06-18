package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.GameEventDeferredRegister;
import mekanism.common.registration.impl.GameEventRegistryObject;
import net.minecraft.world.level.gameevent.GameEvent;

public class MekanismGameEvents {

    private MekanismGameEvents() {
    }

    public static final GameEventDeferredRegister GAME_EVENTS = new GameEventDeferredRegister(Mekanism.MODID);

    public static final GameEventRegistryObject<GameEvent> SEISMIC_VIBRATION = GAME_EVENTS.register("seismic_vibration", 64);
}