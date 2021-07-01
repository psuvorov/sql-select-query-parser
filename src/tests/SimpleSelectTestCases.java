package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.TermType;
import com.newjob.parser.domain.terms.Column;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleSelectTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Simple selection of 1 column

        // Arrange
        final String query = "select aa from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(1, res.getColumns().size());
        assertEquals("aa", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getType());
    }

    @Test
    public void query01_1() throws InvalidQueryFormatException {
        // Simple selection of 3 columns

        // Arrange
        final String query = "select aa, bb, cc from table1";

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

    @Test
    public void query02() throws InvalidQueryFormatException {
        // select all columns

        // Arrange
        final String query = "select * from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(1, res.getColumns().size());
        assertEquals("*", res.getColumns().get(0).getSimpleColumnTermName());
    }

    @Test
    public void query03() throws InvalidQueryFormatException {
        // one simple column and one nested query in select statement

        // Arrange
        final String query = "select aa, (select avg(x) from table_with_x), cc from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(3, res.getColumns().size());
        assertEquals("aa", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals("cc", res.getColumns().get(2).getSimpleColumnTermName());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getType());
        assertEquals(TermType.SubQuery, res.getColumns().get(1).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(2).getType());

        assertEquals(1, res.getColumns().get(1).getSubQuery().getColumns().size());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getSubQuery().getColumns().get(0).getType());
        assertEquals("avg(x)", res.getColumns().get(1).getSubQuery().getColumns().get(0).getSimpleColumnTermName());
    }

    @Test
    public void query04() throws InvalidQueryFormatException {
        // one nested query in select statement and one simple column

        // Arrange
        final String query = "select (select avg(x) from table_with_x), cc from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(2, res.getColumns().size());
        assertEquals("cc", res.getColumns().get(1).getSimpleColumnTermName());
        assertEquals(TermType.SubQuery, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());

        assertEquals(1, res.getColumns().get(0).getSubQuery().getColumns().size());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getSubQuery().getColumns().get(0).getType());
        assertEquals("avg(x)", res.getColumns().get(0).getSubQuery().getColumns().get(0).getSimpleColumnTermName());
    }

    @Test
    public void query04_1() throws InvalidQueryFormatException {
        // one nested query in select statement and one simple column

        // Arrange
        final String query = "select (select avg(x) from table_with_x) as InnerQuery, cc from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(2, res.getColumns().size());
        assertEquals("InnerQuery", res.getColumns().get(0).getAlias());
        assertEquals("cc", res.getColumns().get(1).getSimpleColumnTermName());
        assertEquals(TermType.SubQuery, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());

        assertEquals(1, res.getColumns().get(0).getSubQuery().getColumns().size());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getSubQuery().getColumns().get(0).getType());
        assertEquals("avg(x)", res.getColumns().get(0).getSubQuery().getColumns().get(0).getSimpleColumnTermName());
    }

    @Test
    public void query04_2() throws InvalidQueryFormatException {
        // one nested query in select statement and one simple column

        // Arrange
        final String query = "select (select avg(x) from table_with_x) as 'Inner Query', cc from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(2, res.getColumns().size());
        assertEquals("Inner Query", res.getColumns().get(0).getAlias());
        assertEquals("cc", res.getColumns().get(1).getSimpleColumnTermName());
        assertEquals(TermType.SubQuery, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());

        assertEquals(1, res.getColumns().get(0).getSubQuery().getColumns().size());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getSubQuery().getColumns().get(0).getType());
        assertEquals("avg(x)", res.getColumns().get(0).getSubQuery().getColumns().get(0).getSimpleColumnTermName());
    }

    @Test
    public void query04_3() throws InvalidQueryFormatException {
        // one nested query in select statement and one simple column

        // Arrange
        final String query = "select (select avg(x) from table_with_x) InnerQuery, cc from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(2, res.getColumns().size());
        assertEquals("InnerQuery", res.getColumns().get(0).getAlias());
        assertEquals("cc", res.getColumns().get(1).getSimpleColumnTermName());
        assertEquals(TermType.SubQuery, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());

        assertEquals(1, res.getColumns().get(0).getSubQuery().getColumns().size());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getSubQuery().getColumns().get(0).getType());
        assertEquals("avg(x)", res.getColumns().get(0).getSubQuery().getColumns().get(0).getSimpleColumnTermName());
    }

    @Test
    public void query04_4() throws InvalidQueryFormatException {
        // one nested query in select statement and one simple column

        // Arrange
        final String query = "select (select avg(x) from table_with_x) 'Inner Query', cc from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(2, res.getColumns().size());
        assertEquals("Inner Query", res.getColumns().get(0).getAlias());
        assertEquals("cc", res.getColumns().get(1).getSimpleColumnTermName());
        assertEquals(TermType.SubQuery, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());

        assertEquals(1, res.getColumns().get(0).getSubQuery().getColumns().size());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getSubQuery().getColumns().get(0).getType());
        assertEquals("avg(x)", res.getColumns().get(0).getSubQuery().getColumns().get(0).getSimpleColumnTermName());
    }

    @Test
    public void query05() throws InvalidQueryFormatException {
        // 3 nested queries in select statement

        // Arrange
        final String query = "select (select (select (select avg(x) from inner_table_3) from inner_table_2) from inner_table_1) from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(1, res.getColumns().size());
        assertEquals(TermType.SubQuery, res.getColumns().get(0).getType());

        assertEquals(1, res.getColumns().get(0).getSubQuery().getColumns().size());
        assertEquals(TermType.SubQuery, res.getColumns().get(0).getSubQuery().getColumns().get(0).getType());

        assertEquals(1, res.getColumns().get(0).getSubQuery().getColumns().get(0).getSubQuery().getColumns().get(0).getSubQuery().getColumns().size());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getSubQuery().getColumns().get(0).getSubQuery().getColumns().get(0).getSubQuery().getColumns().get(0).getType());
    }

    @Test
    public void query06() throws InvalidQueryFormatException {
        // Simple selection of 3 columns

        // Arrange
        final String query = "select table1.aa, bb as 'BbB', cc 'CCC' from table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(3, res.getColumns().size());
        assertEquals("table1.aa", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals("bb", res.getColumns().get(1).getSimpleColumnTermName());
        assertEquals("cc", res.getColumns().get(2).getSimpleColumnTermName());
        assertEquals("BbB", res.getColumns().get(1).getAlias());
        assertEquals("CCC", res.getColumns().get(2).getAlias());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(2).getType());
    }

    @Test
    public void query07() throws InvalidQueryFormatException {
        // Simple selection of 3 columns

        // Arrange
        final String query = "SELECT model 'mModel!', not_a_model as 'This is not a model', COUNT(model) AS Qty_model, AVG(price) AS Avg_price FROM PC";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(4, res.getColumns().size());
        assertEquals("model", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals("not_a_model", res.getColumns().get(1).getSimpleColumnTermName());
        assertEquals("COUNT(model)", res.getColumns().get(2).getSimpleColumnTermName());
        assertEquals("AVG(price)", res.getColumns().get(3).getSimpleColumnTermName());
        assertEquals("mModel!", res.getColumns().get(0).getAlias());
        assertEquals("This is not a model", res.getColumns().get(1).getAlias());
        assertEquals("Qty_model", res.getColumns().get(2).getAlias());
        assertEquals("Avg_price", res.getColumns().get(3).getAlias());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(2).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(3).getType());
    }

    @Test
    public void query08() throws InvalidQueryFormatException {
        // TODO: Not passed

        // Arrange
        final String query = "SELECT model 'mModel!', not_a_model as 'This is not a model :(((', COUNT(model) AS Qty_model, AVG(price) AS Avg_price FROM PC";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(4, res.getColumns().size());
        assertEquals("model", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals("not_a_model", res.getColumns().get(1).getSimpleColumnTermName());
        assertEquals("COUNT(model)", res.getColumns().get(2).getSimpleColumnTermName());
        assertEquals("AVG(price)", res.getColumns().get(3).getSimpleColumnTermName());
        assertEquals("mModel!", res.getColumns().get(0).getAlias());
        assertEquals("This is not a model :(((", res.getColumns().get(1).getAlias());
        assertEquals("Qty_model", res.getColumns().get(2).getAlias());
        assertEquals("Avg_price", res.getColumns().get(3).getAlias());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(0).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(1).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(2).getType());
        assertEquals(TermType.SimpleTerm, res.getColumns().get(3).getType());
    }

}
