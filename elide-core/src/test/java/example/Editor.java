/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package example;

import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.ComputedRelationship;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.FilterExpressionPath;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.SharePermission;

import lombok.Getter;
import lombok.Setter;

import java.beans.Transient;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Model for authors.
 */
@Entity
@Table(name = "editor")
@Include(rootLevel = true)
@SharePermission
@Audit(action = Audit.Action.CREATE,
        operation = 10,
        logStatement = "{0}",
        logExpressions = {"${editor.name}"})
public class Editor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @Exclude
    private String naturalKey = UUID.randomUUID().toString();

    @Override
    public int hashCode() {
        return naturalKey.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Editor)) {
            return false;
        }

        return ((Editor) obj).naturalKey.equals(naturalKey);
    }

    @Getter @Setter
    private String name;

    @Transient
    @ComputedRelationship
    @OneToOne
    @FilterExpressionPath("this")
    @ReadPermission(expression = "Field path editor check")
    public Editor getEditor() {
        return this;
    }
}