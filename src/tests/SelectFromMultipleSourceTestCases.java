package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SelectFromMultipleSourceTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Assert
        String query = "select table1.Id from (select Boards.Id from Boards) table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(1, res.getFromSources().size());
    }

    @Test
    public void query02() throws InvalidQueryFormatException {
        // Assert
        String query = "select table1.Id from (select Boards.Id from Boards) as table1";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(1, res.getFromSources().size());
    }

    @Test
    public void query03() throws InvalidQueryFormatException {
        // Assert
        String query = "select * from Boards, Lists, Cards";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals(3, res.getFromSources().size());
    }

    @Test
    public void query04() throws InvalidQueryFormatException {
        // Assert
        String query = "select * from (select boards.* from Boards) boards, Lists";

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

}
