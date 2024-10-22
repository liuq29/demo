private final JavaClasses classes = new ClassFileImporter()
            .importPackages("com.yourproject")
            .stream()
            .filter(clazz -> !clazz.getName().startsWith("com.cn.package")) // 排除特定包
            .collect(Collectors.toSet());
