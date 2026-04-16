package mekanism.client.render.obj;

/*
public class TransmitterLoader implements IGeometryLoader<TransmitterModel> {

    public static final TransmitterLoader INSTANCE = new TransmitterLoader();

    private TransmitterLoader() {
    }

    @NotNull
    @Override
    public TransmitterModel read(@NotNull JsonObject modelContents, @NotNull JsonDeserializationContext deserializationContext) throws JsonParseException {
        //Wrap the Obj loader to read our file
        ObjModel model = ObjLoader.INSTANCE.read(modelContents, deserializationContext);
        ObjModel glass = null;
        if (modelContents.has(SerializationConstants.GLASS)) {
            glass = ObjLoader.INSTANCE.read(modelContents.getAsJsonObject(SerializationConstants.GLASS), deserializationContext);
        }
        return new TransmitterModel(model, glass);
    }
}*/