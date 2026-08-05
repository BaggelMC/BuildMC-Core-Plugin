package net.mathias2246.buildmc.claims.protections;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@SuppressWarnings("UnusedReturnValue")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProtectionConfig {
    String value();
    boolean defaultEnabled() default true;
}
