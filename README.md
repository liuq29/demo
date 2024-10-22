import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class UnusedCodeTest {

    // 扫描项目包中的类
    private final JavaClasses classes = new ClassFileImporter().importPackages("com.yourproject");

    @Test
    public void findUnusedClasses() {
        // 获取所有未被引用的类，排除controller类
        Set<JavaClass> unusedClasses = classes.stream()
                .filter(clazz -> !isController(clazz)) // 排除controller类
                .filter(clazz -> classes.stream()
                        .noneMatch(otherClass -> otherClass.getDirectDependenciesFromSelf()
                                .stream()
                                .anyMatch(dep -> dep.getTargetClass().equals(clazz))))
                .collect(Collectors.toSet());

        // 如果发现未使用的类，则测试失败
        assertThat(unusedClasses)
                .withFailMessage("Found unused classes: " + unusedClasses)
                .isEmpty();
    }

    @Test
    public void findUnusedMethods() {
        // 扫描所有类中的方法，并找出没有调用的那些方法
        Set<String> unusedMethods = classes.stream()
                .flatMap(clazz -> clazz.getMethods().stream())
                .filter(method -> !isController(method.getOwner())) // 排除controller类中的方法
                .filter(method -> classes.stream()
                        .noneMatch(otherClass -> otherClass.getMethodCallsFromSelf()
                                .stream()
                                .anyMatch(call -> call.getTarget().equals(method))))
                .map(method -> method.getOwner().getName() + "#" + method.getName())
                .collect(Collectors.toSet());

        // 如果发现未使用的方法，则测试失败
        assertThat(unusedMethods)
                .withFailMessage("Found unused methods: " + unusedMethods)
                .isEmpty();
    }

    // 判断一个类是否是controller类（根据命名或注解）
    private boolean isController(JavaClass clazz) {
        // 根据类名判断是否是controller（可以根据需要修改正则表达式）
        boolean isControllerByName = clazz.getName().endsWith("Controller");

        // 根据注解判断是否是controller
        boolean isControllerByAnnotation = clazz.isAnnotatedWith("org.springframework.stereotype.Controller")
                || clazz.isAnnotatedWith("org.springframework.web.bind.annotation.RestController");

        return isControllerByName || isControllerByAnnotation;
    }
}
