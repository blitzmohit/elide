/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.yahoo.elide.annotation.Include;
import org.junit.jupiter.api.Test;

import java.util.Set;
import javax.persistence.Entity;

public class ClassScannerTest {

    @Test
    public void testGetAllClasses() {
        Set<Class<?>> classes = ClassScanner.getAllClasses("com.yahoo.elide.core.utils");
        assertEquals(31, classes.size());
        assertTrue(classes.contains(ClassScannerTest.class));
    }

    @Test
    public void testGetAnnotatedClasses() {
        Set<Class<?>> classes = ClassScanner.getAnnotatedClasses("example", Include.class);
        assertEquals(30, classes.size(), "Actual: " + classes);
        classes.forEach(cls -> assertTrue(cls.isAnnotationPresent(Include.class)));
    }

    @Test
    public void testGetAllAnnotatedClasses() {
        Set<Class<?>> classes = ClassScanner.getAnnotatedClasses(Include.class);
        assertEquals(41, classes.size(), "Actual: " + classes);
        classes.forEach(cls -> assertTrue(cls.isAnnotationPresent(Include.class)));
    }

    @Test
    public void testGetAnyAnnotatedClasses() {
        Set<Class<?>> classes = ClassScanner.getAnnotatedClasses(Include.class, Entity.class);
        assertEquals(52, classes.size());
        for (Class<?> cls : classes) {
            assertTrue(cls.isAnnotationPresent(Include.class)
                    || cls.isAnnotationPresent(Entity.class));
        }
    }
}
