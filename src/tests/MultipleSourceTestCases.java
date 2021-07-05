package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultipleSourceTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Assert
        String query = "select table1.Id from (select Boards.Id from Boards) table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(1, res.getFromSources().size());
        assertEquals("table1", res.getFromSources().get(0).getAlias());
        assertEquals(1, res.getColumns().size());
        assertEquals("table1.Id", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals(1, res.getFromSources().size());
        assertEquals("Boards.Id", res.getFromSources().get(0).getSubQuery().getColumns().get(0).getSimpleColumnTermName());
        assertEquals("Boards", res.getFromSources().get(0).getSubQuery().getFromSources().get(0).getSimpleSourceTableName());
        assertEquals("", res.getFromSources().get(0).getSubQuery().getFromSources().get(0).getAlias());
    }

    @Test
    public void query02() throws InvalidQueryFormatException {
        // Assert
        String query = "select table1.Id from (select Boards.Id from Boards as 'B o a r d s') as table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(1, res.getFromSources().size());
        assertEquals("table1", res.getFromSources().get(0).getAlias());
        assertEquals(1, res.getColumns().size());
        assertEquals("table1.Id", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals(1, res.getFromSources().size());
        assertEquals("Boards.Id", res.getFromSources().get(0).getSubQuery().getColumns().get(0).getSimpleColumnTermName());
        assertEquals("Boards", res.getFromSources().get(0).getSubQuery().getFromSources().get(0).getSimpleSourceTableName());
        assertEquals("B o a r d s", res.getFromSources().get(0).getSubQuery().getFromSources().get(0).getAlias());
    }

    @Test
    public void query03() throws InvalidQueryFormatException {
        // Assert
        String query = "select * from Boards as booards, Lists as LiSts, Cards as 'C a r d s'";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(3, res.getFromSources().size());
    }

    @Test
    public void query04() throws InvalidQueryFormatException {
        // Assert
        String query = "select * from (select boards.* from Boards) boards,Lists as LiSt";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(2, res.getFromSources().size());
    }

    @Test
    public void query05() throws InvalidQueryFormatException {
        // Assert
        String query = "select * from (select boards.* from Boards) as boards, (select lists.* from Lists) lists, Tags where boards.x > 100";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(3, res.getFromSources().size());
    }

    @Test
    public void query07() throws InvalidQueryFormatException {
        // Assert
        String query = "select t.* from Tags as t, Bags b where t.x > 100";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(1, res.getColumns().size());
        assertEquals(2, res.getFromSources().size());
        assertEquals("Tags", res.getFromSources().get(0).getSimpleSourceTableName());
        assertEquals("Bags", res.getFromSources().get(1).getSimpleSourceTableName());
        assertEquals("t", res.getFromSources().get(0).getAlias());
        assertEquals("b", res.getFromSources().get(1).getAlias());
    }

    @Test
    public void query08() throws InvalidQueryFormatException {
        // Assert
        String query = "select t.* from Tags,Bags,Legs";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(3, res.getFromSources().size());
    }

    @Test
    public void query09() throws InvalidQueryFormatException {
        // Assert
        String query = "SELECT\n" +
                "  count       (     b.Username   )\n" +
                "FROM (select * from Users)b";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(1, res.getFromSources().size());
    }

}
