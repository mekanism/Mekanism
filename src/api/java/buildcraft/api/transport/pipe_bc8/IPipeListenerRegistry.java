package buildcraft.api.transport.pipe_bc8;

public interface IPipeListenerRegistry {
    IPipeListenerFactory getFactory(String globalUniqueTag);

    String getGlobalUniqueTag(IPipeListener listener);

    void registerListenerFactory(String modUniqueTag, IPipeListenerFactory factory);
}
