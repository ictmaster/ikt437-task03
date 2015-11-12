import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GUI extends JFrame {
    private JPanel rootPanel;
    private JTextField newTopic;
    private JComboBox dependsDrop;
    private JComboBox subtopicOf;
    private JButton createTopicButton;
    private JButton addSub;
    private JTextArea output;
    private JButton addDepend;
    private JButton saveOntButton;
    private JTabbedPane mainTabbedPane;
    private JScrollPane outputScroller;
    private JComboBox typeDrop;
    private JButton addType;
    private JTextField courseName;
    private JComboBox courseTopicDrop;
    private JButton addTopicButton;
    private JButton saveCourseButton;
    private JButton loadOntButton;
    private JComboBox deleteCourseDrop;
    private JButton deleteCourseButton;
    private JComboBox deleteTopicDrop;
    private JButton deleteTopicButton;
    private JTextArea sparql;
    private JButton sparqlButton;


    //Some initialization
    ObjectProperty isSubtopicOf; //Topic is subtopic of Topic
    ObjectProperty hasSubtopic; //Topic has subtopic Topic

    ObjectProperty hasRequirement; //Topic has requirement Topic
    ObjectProperty isRequirement; //Topic has requirement Topic

    ObjectProperty hasTopic; //Course has Topic
    ObjectProperty isTopicOf; //Topic is topic of Course

    ObjectProperty hasPracticalPart; //Topic has practical part
    ObjectProperty isPracticalPart; //Learningtype is Practical part of Topic

    ObjectProperty hasTheoreticalPart; //Topic has theoretical part
    ObjectProperty isTheoreticalPart;  //Learningtype is theoretical part of Topic

    Individual newResource;
    List<String> dependantOn = new ArrayList<>();
    List<String> subtopics = new ArrayList<>();
    List<String> types = new ArrayList<>();
    List<String> courseHasTopic = new ArrayList<>();

    OntModel model;

    public GUI() {
        //Workaround for annoying width-changing dropdowns
        dependsDrop.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
        subtopicOf.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
        deleteCourseDrop.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
        deleteTopicDrop.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");

        //Looks cooler :)
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setTitle("Jonas' Amazing Ontology Program");
        setContentPane(rootPanel);
        setResizable(false);
        pack();
        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String uri = "http://jonasn12.uia.io/ontology#";

        // Create main ontology model
        model = ModelFactory.createOntologyModel();

        // Properties
        isSubtopicOf = model.createObjectProperty(uri + "isSubtopicOf");
        hasSubtopic = model.createObjectProperty(uri + "hasSubtopic");
        hasRequirement = model.createObjectProperty(uri + "hasRequirement");
        isRequirement = model.createObjectProperty(uri + "isRequirement");
        hasTopic = model.createObjectProperty(uri + "hasTopic");
        isTopicOf = model.createObjectProperty(uri + "isTopicOf");
        hasPracticalPart = model.createObjectProperty(uri + "hasPracticalPart");
        isPracticalPart = model.createObjectProperty(uri + "isPracticalPart");
        hasTheoreticalPart = model.createObjectProperty(uri + "hasTheoreticalPart");
        isTheoreticalPart = model.createObjectProperty(uri + "isTheoreticalPart");

        // Configuring props..
        isSubtopicOf.isInverseOf(hasSubtopic);
        hasRequirement.isInverseOf(isRequirement);
        hasTopic.isInverseOf(isTopicOf);
        hasPracticalPart.isInverseOf(isPracticalPart);
        hasTheoreticalPart.isInverseOf(isTheoreticalPart);

        //Create classes
        OntClass topics = model.createClass(uri + "Topics");
        OntClass courses = model.createClass(uri + "Courses");
        OntClass learningTypes = model.createClass(uri + "Learningtypes");

        //Create some learning types
        Individual presentation = model.createIndividual(uri + "Presentation", learningTypes);
        Individual lecture = model.createIndividual(uri + "Lecture", learningTypes);

        //Create some topics
        Individual advancedSubject = model.createIndividual(uri + "Integrals", topics);
        Individual baseSubject = model.createIndividual(uri + "Algebra", topics);

        //Create some courses
        Individual mathCourse = model.createIndividual(uri + "MA-154", courses);
        Individual programmingCourse = model.createIndividual(uri + "DAT101", courses);

        //Add some properties
        model.add(topics,hasSubtopic,advancedSubject);
        model.add(topics,hasSubtopic,baseSubject);
        model.add(topics, hasPracticalPart, presentation);
        model.add(topics, hasTheoreticalPart, lecture);
        model.add(courses,hasTopic,mathCourse);
        model.add(courses,hasTopic,programmingCourse);


        //Set property values
        mathCourse.addProperty(hasTopic, baseSubject);
        mathCourse.addProperty(hasTopic, advancedSubject);
        advancedSubject.addProperty(hasRequirement, baseSubject);
        advancedSubject.addProperty(hasSubtopic, baseSubject);
        baseSubject.addProperty(isSubtopicOf, advancedSubject);
        baseSubject.addProperty(isRequirement, advancedSubject);

        presentation.addProperty(isPracticalPart, advancedSubject);
        advancedSubject.addProperty(hasPracticalPart, presentation);
        lecture.addProperty(isTheoreticalPart, baseSubject);
        baseSubject.addProperty(hasTheoreticalPart, lecture);

        //Initial population of the dropdowns
        populateDropDown(model, topics, courses);


        //LISTENERS (not using lambdas cause of not collapsible in intellij)

        createTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String resName = newTopic.getText();

                newResource = model.createIndividual(uri + resName, topics);
                model.add(topics, hasSubtopic, newResource);

                if (!dependantOn.isEmpty()) {
                    for (String item : dependantOn) {
                        newResource.addProperty(hasRequirement, model.getIndividual(item));
                        model.getIndividual(item).addProperty(isRequirement, newResource);
                    }
                }

                if (!subtopics.isEmpty()) {
                    for (String item : subtopics) {
                        newResource.addProperty(hasSubtopic, model.getIndividual(item));
                        model.getIndividual(item).addProperty(isSubtopicOf, newResource);
                    }
                }

                if (!types.isEmpty()) {
                    for (String item : types) {

                        if (item.equals("Presentation")) {
                            newResource.addProperty(hasPracticalPart, model.getIndividual(uri + item));
                            model.getIndividual(uri + item).addProperty(isPracticalPart, newResource);

                        } else if (item.equals("Lecture")) {
                            newResource.addProperty(hasTheoreticalPart, model.getIndividual(uri + item));
                            model.getIndividual(uri + item).addProperty(isTheoreticalPart, newResource);
                        }
                    }
                }
                output.append("Added topic : " + resName + "\n");
                populateDropDown(model, topics, courses);

                dependantOn.clear();
                subtopics.clear();
                types.clear();
            }
        });

        saveCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cName = courseName.getText();
                newResource = model.createIndividual(uri + cName, courses);
                model.add(courses, hasTopic, newResource);

                if(!courseHasTopic.isEmpty()){
                    for (String item : courseHasTopic) {
                        newResource.addProperty(hasTopic, model.getIndividual(item));
                        model.getIndividual(item).addProperty(isTopicOf, newResource);
                    }
                }
                output.append("Added course : " + cName + "\n");
                courseHasTopic.clear();
                populateDropDown(model, topics, courses);
            }
        });

        loadOntButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser jfc = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Turtle File", "ttl");
                    jfc.setFileFilter(filter);
                    File currentDir = new File(System.getProperty("user.dir"));
                    jfc.setCurrentDirectory(currentDir);

                    int returnVal = jfc.showOpenDialog(getParent());
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        model.read(new FileInputStream(jfc.getSelectedFile().getPath()),null, "TTL");
                        output.append("Loaded ontology from file '" + jfc.getSelectedFile().getName() + "'...\n");
                    } else {
                        output.append("Loading ontology canceled...\n");
                    }
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                populateDropDown(model, topics, courses);
            }
        });

        saveOntButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileWriter out = null;
                JFileChooser jfc = new JFileChooser();
                try {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Turtle File", "ttl");
                    jfc.setFileFilter(filter);
                    File currentDir = new File(System.getProperty("user.dir"));
                    jfc.setCurrentDirectory(currentDir);

                    int returnVal = jfc.showSaveDialog(getParent());
                    if(returnVal == JFileChooser.APPROVE_OPTION){
                        out = new FileWriter( jfc.getSelectedFile() );
                        model.write( out, "Turtle" );
                        output.append("Wrote ontology to file '"+jfc.getSelectedFile().getName()+"'...\n");
                    }else{
                        output.append("Writing ontology canceled...\n");
                    }
                } catch (IOException w) {
                    w.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
        });

        addTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = courseTopicDrop.getSelectedItem().toString();
                if(!selected.equals("None")){
                    courseHasTopic.add(selected);
                    output.append("Added topic : " + selected + "\n");
                }
            }
        });

        addType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = typeDrop.getSelectedItem().toString();
                if(!selected.equals("None")) {
                    types.add(selected);
                    output.append("Added type : " + selected + "\n");
                }
            }
        });

        addDepend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = dependsDrop.getSelectedItem().toString();
                if (!selected.equals("None")) {
                    dependantOn.add(selected);
                    output.append("Added dependency : " + selected + "\n");
                }
            }
        });

        addSub.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = subtopicOf.getSelectedItem().toString();
                if (!selected.equals("None")) {
                    subtopics.add(selected);
                    output.append("Added subtopic : " + selected + "\n");
                }
            }
        });

        deleteCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String courseToDel = deleteCourseDrop.getSelectedItem().toString();
                Individual i = model.getIndividual(courseToDel);
                i.remove();
                output.append("Removed course "+courseToDel+"...\n");
                populateDropDown(model, topics, courses);
            }
        });

        deleteTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String topicToDel = deleteTopicDrop.getSelectedItem().toString();
                Individual i = model.getIndividual(topicToDel);
                i.remove();
                output.append("Removed topic "+topicToDel+"...\n");
                populateDropDown(model, topics, courses);
            }
        });

        sparqlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String resultString = new Sparql(model, sparql.getText()).executeQuery();
                output.append("Query processed, opening result window...");
                new ResultWindow("SPARQL Query Results", resultString);
            }
        });

    }

    public void populateDropDown(Model model, Resource topics, Resource courses) {

        JComboBox[] drops = {dependsDrop, subtopicOf, typeDrop, courseTopicDrop, deleteCourseDrop, deleteTopicDrop};

        for(int i = 0; i < drops.length; i++){
            drops[i].removeAllItems();
            drops[i].validate();
        }

        NodeIterator allTopics = model.listObjectsOfProperty(topics, hasSubtopic);
        List<String> resourceList = new ArrayList<>();

        while (allTopics.hasNext()) {
            resourceList.add(allTopics.nextNode().toString());
        }

        for(int i = 0; i < drops.length; i++){
            drops[i].addItem("None");
        }

        for (String item : resourceList) {
            dependsDrop.addItem(item);
            subtopicOf.addItem(item);
            deleteTopicDrop.addItem(item);
        }

        typeDrop.addItem("Presentation");
        typeDrop.addItem("Lecture");

        resourceList.forEach(courseTopicDrop::addItem);

        NodeIterator allCourses = model.listObjectsOfProperty(courses, hasTopic);
        resourceList.clear();

        while(allCourses.hasNext()){
            resourceList.add(allCourses.nextNode().toString());
        }

        resourceList.forEach(deleteCourseDrop::addItem);
    }
}
