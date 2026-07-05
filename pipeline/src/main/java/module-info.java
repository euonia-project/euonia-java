module com.euonia.pipeline {
    requires transitive com.euonia.core;
    exports com.euonia.pipeline;

    // 允许 core 模块通过反射（SimpleServiceProvider）创建 pipeline 内部类实例
    opens com.euonia.pipeline to com.euonia.core;
}
