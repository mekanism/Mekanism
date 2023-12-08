package mekanism.common.lib.transmitter.acceptor;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface AcceptorInfo<ACCEPTOR> {

    @Nullable
    ACCEPTOR acceptor();
}