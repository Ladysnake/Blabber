package me.shedaniel.rei.forge;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dummy annotation to allow our plugin to be loaded on Forge
 * (annotation classes do not need to exist on the classpath at runtime)
 *
 * @author shedaniel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface REIPluginClient {
}
