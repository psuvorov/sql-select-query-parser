package com.newjob.parser.domain.terms;

public class Having {

    private final String havingClause;

    public Having(String havingClause) {
        this.havingClause = havingClause;
    }

    public String getHavingClause() {
        return havingClause;
    }
}
