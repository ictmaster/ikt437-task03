import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;

public class Sparql {
    String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX jonas: <http://jonasn12.uia.io/ontology#>";
    OntModel model;
    String queryString;
    public Sparql(OntModel model, String queryString){
        this.model = model;
        this.queryString = queryString;
    }

    public String executeQuery(){
        return executeQuery(this.queryString);
    }

    public String executeQuery(String queryString) {
        Query query = QueryFactory.create(prefixes+queryString);
        QueryExecution execution = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = execution.execSelect();

        return ResultSetFormatter.asText(resultSet);
    }

}
