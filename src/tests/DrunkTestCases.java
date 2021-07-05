package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.TermType;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DrunkTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Arrange
        final String query = "select aa,bb, cc    ,      (     select avg     (      x       )     'AAAVVVGGG' from table_with_x    )    as       xx from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(4, res.getColumns().size());
        assertEquals("aa", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals("cc", res.getColumns().get(2).getSimpleColumnTermName());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(2).getType());
        assertEquals(TermType.SubQuery, res.getColumns().get(3).getType());

        assertEquals(1, res.getColumns().get(3).getSubQuery().getColumns().size());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(3).getSubQuery().getColumns().get(0).getType());
        assertEquals("avg(x)", res.getColumns().get(3).getSubQuery().getColumns().get(0).getSimpleColumnTermName());
    }

    @Test
    public void query02() throws InvalidQueryFormatException {
        // Arrange
        final String query = "select aa,bb, cc    ,      (     select avg     (      x       )     as    'AAAVVVGGG' from table_with_x)    as       xx from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(4, res.getColumns().size());
        assertEquals("aa", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals("cc", res.getColumns().get(2).getSimpleColumnTermName());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(2).getType());
        assertEquals(TermType.SubQuery, res.getColumns().get(3).getType());

        assertEquals(1, res.getColumns().get(3).getSubQuery().getColumns().size());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(3).getSubQuery().getColumns().get(0).getType());
        assertEquals("avg(x)", res.getColumns().get(3).getSubQuery().getColumns().get(0).getSimpleColumnTermName());
        assertEquals("AAAVVVGGG", res.getColumns().get(3).getSubQuery().getColumns().get(0).getAlias());
    }

    @Test
    public void query03() throws InvalidQueryFormatException {
        // Arrange
        final String query = "select\n" +
                "aa,\n" +
                "bb,\n" +
                "cc\n" +
                "from\n" +
                "table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(3, res.getColumns().size());
        assertEquals(1, res.getFromSources().size());
    }
}
