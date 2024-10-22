// 扫描项目包中的类
    private final JavaClasses allClasses = new ClassFileImporter().importPackages("com.yourproject");

    // 过滤后的类，排除com.cn.package下的所有类
    private final Set<JavaClass> classes = allClasses.stream()
            .filter(clazz -> !clazz.getName().startsWith("com.cn.package")) // 排除特定包
            .collect(Collectors.toSet());
