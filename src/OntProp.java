import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

public class OntProp {
    public static ObjectProperty isSubtopicOf; //Topic is subtopic of Topic
    public static ObjectProperty hasSubtopic; //Topic has subtopic Topic

    public static ObjectProperty isRequirement; //Topic has requirement Topic
    public static ObjectProperty hasRequirement; //Topic has requirement Topic

    public static ObjectProperty isTopicOf; //Topic is topic of Course
    public static ObjectProperty hasTopic; //Course has Topic

    public static ObjectProperty isPracticalPart; //Learningtype is Practical part of Topic
    public static ObjectProperty hasPracticalPart; //Topic has practical part

    public static ObjectProperty isTheoreticalPart;  //Learningtype is theoretical part of Topic
    public static ObjectProperty hasTheoreticalPart; //Topic has theoretical part

    OntModel model;

    public OntProp(){
        this(ModelFactory.createOntologyModel());
    }

    public OntProp(OntModel model){
        this.model = model;
    }

    public OntModel getUpdatedModel(){
        populateModel();
        return this.model;
    }

    public void populateModel() {
        //Creating the model properties
        isSubtopicOf = model.createObjectProperty(OntologyProperties.URI + "isSubtopicOf");
        hasSubtopic = model.createObjectProperty(OntologyProperties.URI + "hasSubtopic");
        hasRequirement = model.createObjectProperty(OntologyProperties.URI + "hasRequirement");
        isRequirement = model.createObjectProperty(OntologyProperties.URI + "isRequirement");
        hasTopic = model.createObjectProperty(OntologyProperties.URI + "hasTopic");
        isTopicOf = model.createObjectProperty(OntologyProperties.URI + "isTopicOf");
        hasPracticalPart = model.createObjectProperty(OntologyProperties.URI + "hasPracticalPart");
        isPracticalPart = model.createObjectProperty(OntologyProperties.URI + "isPracticalPart");
        hasTheoreticalPart = model.createObjectProperty(OntologyProperties.URI + "hasTheoreticalPart");
        isTheoreticalPart = model.createObjectProperty(OntologyProperties.URI + "isTheoreticalPart");

        //Setting inverses
        isSubtopicOf.isInverseOf(hasSubtopic);
        isRequirement.isInverseOf(hasRequirement);
        isTopicOf.isInverseOf(hasTopic);
        isPracticalPart.isInverseOf(hasPracticalPart);
        isTheoreticalPart.isInverseOf(hasTheoreticalPart);
    }
}
