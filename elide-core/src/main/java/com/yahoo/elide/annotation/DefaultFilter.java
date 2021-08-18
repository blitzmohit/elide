/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.yahoo.elide.core.filter.Operator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method or field with a default that should be used for filtering via Elide in case no filter is provided for
 * this field in the query
 */
@SuppressWarnings("unused")
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface DefaultFilter {
    Operator op();
    String[] values();
}
