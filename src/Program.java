import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import tests.SampleQueries;

public class Program {

    public static void main(String[] args) {

//        StringBuilder sb = new StringBuilder();
//        Scanner scanner = new Scanner(System.in);
//
//        while (scanner.hasNextLine())
//            sb.append(scanner.nextLine()).append(" ");
//
//        scanner.close();

        try {
//            Query res = QueryParser.parseQuery(sb.toString());
//            Query res = (new QueryParser()).parseQuery(SampleQueries.Query5, true);
            Query res = QueryParser.parseQuery(SampleQueries.Query5);

            System.out.println(res.getInfo());
        } catch (InvalidQueryFormatException e) {
            e.printStackTrace();
        }
    }


}
