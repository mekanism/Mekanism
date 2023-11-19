package mekanism.common.registration.impl;

import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventDeferredRegister extends MekanismDeferredRegister<GameEvent> {

    public GameEventDeferredRegister(String modid) {
        super(Registries.GAME_EVENT, modid);
    }

    public MekanismDeferredHolder<GameEvent, GameEvent> register(String name) {
        return register(name, 16);
    }

    public MekanismDeferredHolder<GameEvent, GameEvent> register(String name, int notificationRadius) {
        return register(name, () -> new GameEvent(notificationRadius));
    }
}