package mekanism

record IntegrationSettings(boolean shouldCompile = true, IntegrationScope scope = IntegrationScope.BASE_ONLY, PerModule perModule = PerModule.NONE) {

    static String getName(String name, String module = 'main') {
        String target = ''
        def components = name.split('_')
        for (def component in components) {
            target += component.substring(0, 1).toUpperCase(Locale.ROOT) + component.substring(1)
        }
        if (module != 'main') {
            return "${module}Integration${target}"
        }
        return "integration${target}"
    }

    static String getDataName(String name, String module = 'main') {
        String target = getName(name, module)
        return 'datagen' + target.substring(0, 1).toUpperCase(Locale.ROOT) + target.substring(1)
    }
}