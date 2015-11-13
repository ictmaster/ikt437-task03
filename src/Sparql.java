import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;

public class Sparql {
    OntModel model;
    String queryString;
    public Sparql(OntModel model, String queryString){
        this.model = model;
        this.queryString = queryString;
    }

    public void setNewQueryString(String queryString){
        this.queryString = queryString;
    }

    public String executeQuery(){
        return executeQuery(this.queryString);
    }

    public String executeQuery(String queryString) throws QueryParseException {
        Query query = QueryFactory.create(OntologyProperties.SparqlPrefixes+queryString);
        QueryExecution execution = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = execution.execSelect();

        return ResultSetFormatter.asText(resultSet);
    }

}
