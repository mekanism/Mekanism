package buildcraft.api.statements;

public interface IActionSingle extends IAction {
    /** @return True if this action should only be fired for the first tick of it being active. */
    boolean singleActionTick();
}
