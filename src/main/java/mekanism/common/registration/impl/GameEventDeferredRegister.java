package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventDeferredRegister extends WrappedDeferredRegister<GameEvent> {

    private final String modid;

    public GameEventDeferredRegister(String modid) {
        super(modid, Registry.GAME_EVENT_REGISTRY);
        this.modid = modid;
    }

    public GameEventRegistryObject<GameEvent> register(String name) {
        return register(name, 16);
    }

    public GameEventRegistryObject<GameEvent> register(String name, int notificationRadius) {
        return register(name, () -> new GameEvent(modid + ":" + name, notificationRadius));
    }

    public <GAME_EVENT extends GameEvent> GameEventRegistryObject<GAME_EVENT> register(String name, Supplier<? extends GAME_EVENT> sup) {
        return register(name, sup, GameEventRegistryObject::new);
    }
}