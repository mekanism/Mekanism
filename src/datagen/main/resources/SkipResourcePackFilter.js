var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI')

function initializeCoreMod() {
  return {
    //This skips trying to grab the resource pack filter from the pack.mcmeta,
    // as the ones we have that get loaded in the folder pack, and are otherwise
    // unused are not valid json as they get replaced when resources are processed
    // skipping checking for a resource pack filter removes 30 stack traces from
    // being printed each time our datagen is run. This isn't a forge PR to just
    // skip it as there may be some cases having it in datagen is worthwhile for
    // other mods, just for us, it doesn't actually matter at all, so we skip it
    'Skip Resource Pack Filter': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.server.packs.resources.MultiPackResourceManager',
        'methodName': ASMAPI.mapMethod('m_215467_'),
        'methodDesc': '(Lnet/minecraft/server/packs/PackResources;)Lnet/minecraft/server/packs/resources/ResourceFilterSection;'
      },
      'transformer': function (method) {
        var Opcodes = Java.type('org.objectweb.asm.Opcodes');
        var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
        var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
        var newInstructions = new InsnList();
        newInstructions.add(new InsnNode(Opcodes.ACONST_NULL));
        newInstructions.add(new InsnNode(Opcodes.ARETURN));
        method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);
        return method;
      }
    }
  }
}