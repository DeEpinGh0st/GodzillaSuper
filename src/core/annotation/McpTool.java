package core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a plugin method as an MCP tool.
 * Method signature must be: public String xxx(java.util.Map<String,Object> args)
 * "shellId" param name is reserved for locating the ShellEntity.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface McpTool {
    String name() default "";              // tool name suffix, default = method name
    String desc();
    McpParam[] params() default {};
}
