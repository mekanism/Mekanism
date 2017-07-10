package buildcraft.api.transport.pipe;

/** To be implemented by the real item pipe in Transport mod, but leaves knowledge for classes that do not have direct
 * dependency on transport. */
public interface IItemPipe {
    PipeDefinition getDefinition();
}
