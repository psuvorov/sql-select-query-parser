package com.newjob.parser.domain.terms;

import com.newjob.parser.domain.enums.SortType;

public class Sort {

    private final String column;

    private final SortType sortType;

    public Sort(String column, SortType sortType) {
        this.column = column;
        this.sortType = sortType;
    }

    public String getColumn() {
        return column;
    }

    public SortType getSortType() {
        return sortType;
    }
}
