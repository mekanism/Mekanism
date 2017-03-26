package buildcraft.api.statements;

/** Marker interface that designates a class as being an action. Note that you *must* implement ONE of the following
 * interfaces to be recognised as an action: {@link IActionInternal}, {@link IActionInternalSided}, or
 * {@link IActionExternal} */
public interface IAction extends IStatement {}
