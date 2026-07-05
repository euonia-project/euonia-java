package com.euonia.reflection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * 扫描 classpath 中给定包下的类。
 * <p>
 * 支持基于目录的 classpath（典型于 IDE/开发环境）和基于 JAR 的 classpath（典型于打包部署环境）。
 * 仅使用标准 Java API —— 无第三方依赖。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class ClassScanner {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("reflection");

    private ClassScanner() {
        // 工具类
    }

    /**
     * 扫描指定的包，并返回其中找到的所有非匿名、非合成的类。
     *
     * @param packageName 完全限定包名（例如 {@code com.euonia.bus}）
     * @return 包中找到的 {@link Class} 对象列表
     */
    public static List<Class<?>> scan(String packageName) {
        var classes = new LinkedHashSet<Class<?>>();
        var path = packageName.replace('.', '/');
        var classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassScanner.class.getClassLoader();
        }

        try {
            var resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                var resource = resources.nextElement();
                var protocol = resource.getProtocol();
                if ("file".equals(protocol)) {
                    scanDirectory(new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8)),
                        packageName, classes);
                } else if ("jar".equals(protocol)) {
                    scanJar(resource, path, classes);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("扫描包失败: " + packageName, e);
        }

        // 模块系统补充扫描：同时从模块层中扫描类
        scanModuleLayer(packageName, classes);

        return new ArrayList<>(classes);
    }

    /**
     * 递归扫描目录中的 {@code .class} 文件。
     */
    private static void scanDirectory(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists()) {
            return;
        }

        var files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (var file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                var className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    var cls = Class.forName(className);
                    if (!cls.isSynthetic() && !cls.isAnonymousClass()) {
                        classes.add(cls);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                    // 类无法加载 —— 跳过
                }
            }
        }
    }

    /**
     * 扫描 JAR 文件中给定包下的类。
     */
    private static void scanJar(URL resource, String packagePath, Set<Class<?>> classes) {
        var jarPath = resource.getPath();
        // 去除 "file:" 前缀和 "!/..." 后缀以获取 JAR 文件路径
        var separatorIndex = jarPath.indexOf("!/");
        if (separatorIndex >= 0) {
            jarPath = jarPath.substring(0, separatorIndex);
        }
        // 如果存在则去除开头的 "file:"
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring("file:".length());
        }
        jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);

        try (var jar = new JarFile(jarPath)) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                var entryName = entry.getName();
                if (entryName.startsWith(packagePath + "/") && entryName.endsWith(".class")
                    && entryName.indexOf('/', packagePath.length() + 1) < 0) {
                    // 仅处理包的直接子类（不包含子包）
                    var className = entryName.substring(0, entryName.length() - 6).replace('/', '.');
                    try {
                        var cls = Class.forName(className);
                        if (!cls.isSynthetic() && !cls.isAnonymousClass()) {
                            classes.add(cls);
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                        // 类无法加载 —— 跳过
                    }
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException(String.format(bundle.getString("ClassScanner.JarScanFailed"), jarPath), exception);
        }
    }

    /**
     * 从模块层中扫描指定包下的类（JPMS 模块系统补充扫描）。
     * 当 classloader 扫描未找到类时作为回退机制。
     */
    private static void scanModuleLayer(String packageName, Set<Class<?>> classes) {
        var layer = ClassScanner.class.getModule().getLayer();
        if (layer == null) {
            return;
        }
        var packagePath = packageName.replace('.', '/');
        for (var module : layer.modules()) {
            if (module.isNamed() && module.getDescriptor() != null
                    && module.getDescriptor().packages().stream()
                    .anyMatch(p -> p.equals(packageName) || p.startsWith(packageName + "."))) {
                try (var reader = module.getLayer().configuration()
                        .findModule(module.getName())
                        .orElseThrow()
                        .reference()
                        .open()) {
                    reader.list()
                            .filter(r -> r.startsWith(packagePath + "/") && r.endsWith(".class"))
                            .forEach(resource -> {
                                var className = resource.substring(0, resource.length() - 6)
                                        .replace('/', '.');
                                try {
                                    var cls = Class.forName(module, className);
                                    if (!cls.isSynthetic() && !cls.isAnonymousClass()) {
                                        classes.add(cls);
                                    }
                                } catch (LinkageError ignored) {
                                    // skip
                                }
                            });
                } catch (IOException ignored) {
                    // skip modules that can't be opened
                }
            }
        }
    }
}
