package com.sigpwned.oauth4j.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to mark code as ignored for analysis
 */
@Retention(CLASS)
@Target({TYPE, METHOD})
public @interface Generated {

}
