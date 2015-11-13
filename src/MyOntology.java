import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

public class MyOntology {
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
    OntClass topics;
    OntClass courses;

    public OntClass getLearningTypes() {
        return learningTypes;
    }

    public OntClass getTopics() {
        return topics;
    }

    public OntClass getCourses() {
        return courses;
    }

    OntClass learningTypes;

    public MyOntology(){
        this(ModelFactory.createOntologyModel());
    }

    public MyOntology(OntModel model){
        this.model = model;
        addProperties();
        createClasses();
        FileHandler.importModel(this.model, "ont.ttl");
        //addStartData();
    }

    public OntModel getModel(){
        return this.model;
    }

    private void createClasses(){
        //Create classes
        topics = model.createClass(OntologyProperties.URI + "Topics");
        courses = model.createClass(OntologyProperties.URI + "Courses");
        learningTypes = model.createClass(OntologyProperties.URI + "Learningtypes");
    }

    private void addProperties() {
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

    public void addStartData(){
        //Create some learning types
        Individual presentation = model.createIndividual(OntologyProperties.URI + "Presentation", getLearningTypes());
        Individual lecture = model.createIndividual(OntologyProperties.URI + "Lecture", getLearningTypes());

        //Create some topics
        Individual advancedSubject = model.createIndividual(OntologyProperties.URI + "Integrals", getTopics());
        Individual baseSubject = model.createIndividual(OntologyProperties.URI + "Algebra", getTopics());

        //Create some courses
        Individual mathCourse = model.createIndividual(OntologyProperties.URI + "MA-154", getCourses());
        Individual programmingCourse = model.createIndividual(OntologyProperties.URI + "DAT101", getCourses());

        //Add some properties
        model.add(topics, hasSubtopic,advancedSubject);
        model.add(topics, hasSubtopic,baseSubject);
        model.add(topics, hasPracticalPart, presentation);
        model.add(topics, hasTheoreticalPart, lecture);
        model.add(courses, hasTopic,mathCourse);
        model.add(courses, hasTopic,programmingCourse);

        //Set property values
        mathCourse.addProperty(MyOntology.hasTopic, baseSubject);
        mathCourse.addProperty(MyOntology.hasTopic, advancedSubject);
        advancedSubject.addProperty(MyOntology.hasRequirement, baseSubject);
        advancedSubject.addProperty(MyOntology.hasSubtopic, baseSubject);
        baseSubject.addProperty(MyOntology.isSubtopicOf, advancedSubject);
        baseSubject.addProperty(MyOntology.isRequirement, advancedSubject);

        presentation.addProperty(MyOntology.isPracticalPart, advancedSubject);
        advancedSubject.addProperty(MyOntology.hasPracticalPart, presentation);
        lecture.addProperty(MyOntology.isTheoreticalPart, baseSubject);
        baseSubject.addProperty(MyOntology.hasTheoreticalPart, lecture);
    }


}
