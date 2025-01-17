/*
 * Copyright 2021, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */

package example;

import com.yahoo.elide.annotation.Include;
import lombok.Data;

import javax.persistence.Id;

@Include
@Data
public class Author {
    @Id
    private long id;

    private String name;
}
