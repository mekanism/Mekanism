package buildcraft.api.statements;

/** Marker interface that designates a class as being a trigger. Note that you *must* implement ONE of the following
 * interfaces to be recognised as a trigger: {@link ITriggerInternal}, {@link ITriggerInternalSided}, or
 * {@link ITriggerExternal} */
public interface ITrigger extends IStatement {}
