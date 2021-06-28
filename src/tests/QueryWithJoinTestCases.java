package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.TermType;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryWithJoinTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Simple selection of 3 columns

        // Arrange
        final String query = "select aa, bb, cc, xx, yy from table1 join table2 on table1.id = table2.ref_id";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(3, res.getColumns().size());
        assertEquals("aa", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals("bb", res.getColumns().get(1).getSimpleColumnTermName());
        assertEquals("cc", res.getColumns().get(2).getSimpleColumnTermName());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(2).getType());
    }

}
