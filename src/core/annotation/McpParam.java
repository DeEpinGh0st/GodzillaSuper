package core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Param declaration for an @McpTool method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface McpParam {
    String name();
    String desc() default "";
    boolean required() default false;
    String type() default "string";        // string / int / bool
    String defaultValue() default "";
}
