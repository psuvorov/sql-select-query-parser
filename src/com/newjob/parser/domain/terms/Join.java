package com.newjob.parser.domain.terms;

import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.JoinType;
import com.newjob.parser.domain.enums.TermType;

public class Join {

    private final JoinType joinType;

    private String referencedTableName = "";

    private String referencedAlias = "";

    private Query referencedSubquery;

    private TermType referencedTermType;

    private String joinClause;

    public Join(JoinType joinType, String referencedTableName, String referencedAlias, String joinClause) {
        this.joinType = joinType;
        this.referencedTableName = referencedTableName;
        this.referencedAlias = referencedAlias;
        this.joinClause = joinClause;
        referencedTermType = TermType.SimpleTerm; // Indicates that it's just a joined table
    }

    public Join(JoinType joinType, Query referencedSubquery, String referencedAlias, String joinClause) {
        this.joinType = joinType;
        this.referencedSubquery = referencedSubquery;
        this.referencedAlias = referencedAlias;
        this.joinClause = joinClause;
        referencedTermType = TermType.SubQuery; // We're joining a subquery
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public TermType getReferencedTermType() {
        return referencedTermType;
    }

    public String getReferencedTableName() {
        return referencedTableName;
    }

    public String getReferencedAlias() {
        return referencedAlias;
    }

    public Query getReferencedSubquery() {
        return referencedSubquery;
    }

    public String getJoinClause() {
        return joinClause;
    }
}
