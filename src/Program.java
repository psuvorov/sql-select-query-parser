import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import tests.ComplexTestCases;

import java.util.Scanner;

public class Program {

    public static void main(String[] args) {

        System.out.println("Enter SQL text:");

        StringBuilder sb = new StringBuilder();
        String line;
        try (Scanner scan = new Scanner(System.in)) {
            while (scan.hasNextLine() && (line = scan.nextLine()).length() != 0) {
                sb.append(line);
            }
        }

        try {
            Query res = QueryParser.parseQuery(sb.toString());

            System.out.println(res);
        } catch (InvalidQueryFormatException ex) {
            System.out.print("An Error occurred: ");
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
