package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.TermType;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LimitAndOffsetTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Simple selection of 3 columns

        // Arrange
        final String query = "select aa, bb, cc from table1 limit 10 offset 3";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(3, res.getColumns().size());
        assertEquals(10, res.getLimit().intValue());
        assertEquals(3, res.getOffset().intValue());
    }

    @Test
    public void query02() throws InvalidQueryFormatException {
        // 3 nested queries in select statement

        // Arrange
        final String query = "select (select (select (select avg(x) from inner_table_3 where inner_table_3.Id != 5 limit 10 offset 3) from inner_table_2) from inner_table_1) from table1";

        // Act
        Query res = QueryParser.parseQuery(query);
        Query subQuery1 = res.getColumns().get(0).getSubQuery();
        Query subQuery2 = subQuery1.getColumns().get(0).getSubQuery();
        Query subQuery3 = subQuery2.getColumns().get(0).getSubQuery();

        // Assert
        assertEquals(10, subQuery3.getLimit().intValue());
        assertEquals(3, subQuery3.getOffset().intValue());
    }

}
