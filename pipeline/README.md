
# Configure Pipeline

``` java
@Configuration
public class PipelineConfiguration {
    @Bean
    public PipelineFactory pipelineFactory(ServiceResolver resolver) {
        return new DefaultPipelineFactory(resolver);
    }
}
```
