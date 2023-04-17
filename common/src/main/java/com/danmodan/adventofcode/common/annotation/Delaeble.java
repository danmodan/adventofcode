package com.danmodan.adventofcode.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Delaeble {

    /**
     * milliseconds
     * @return
     */
    long value();

    /**
     * bitmap (After,Before)
     * <li>0 == none
     * <li>1 == just Before
     * <li>2 == just End
     * <li>3 == both
     * @return
     */
    byte location();
}
