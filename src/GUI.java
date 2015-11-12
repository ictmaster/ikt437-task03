import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
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

        // Create main ontology model
        model = new OntProp().getUpdatedModel();

        //Create classes
        OntClass topics = model.createClass(OntologyProperties.URI + "Topics");
        OntClass courses = model.createClass(OntologyProperties.URI + "Courses");
        OntClass learningTypes = model.createClass(OntologyProperties.URI + "Learningtypes");

        //Create some learning types
        Individual presentation = model.createIndividual(OntologyProperties.URI + "Presentation", learningTypes);
        Individual lecture = model.createIndividual(OntologyProperties.URI + "Lecture", learningTypes);

        //Create some topics
        Individual advancedSubject = model.createIndividual(OntologyProperties.URI + "Integrals", topics);
        Individual baseSubject = model.createIndividual(OntologyProperties.URI + "Algebra", topics);

        //Create some courses
        Individual mathCourse = model.createIndividual(OntologyProperties.URI + "MA-154", courses);
        Individual programmingCourse = model.createIndividual(OntologyProperties.URI + "DAT101", courses);

        //Add some properties
        model.add(topics,OntProp.hasSubtopic,advancedSubject);
        model.add(topics,OntProp.hasSubtopic,baseSubject);
        model.add(topics, OntProp.hasPracticalPart, presentation);
        model.add(topics, OntProp.hasTheoreticalPart, lecture);
        model.add(courses,OntProp.hasTopic,mathCourse);
        model.add(courses,OntProp.hasTopic,programmingCourse);


        //Set property values
        mathCourse.addProperty(OntProp.hasTopic, baseSubject);
        mathCourse.addProperty(OntProp.hasTopic, advancedSubject);
        advancedSubject.addProperty(OntProp.hasRequirement, baseSubject);
        advancedSubject.addProperty(OntProp.hasSubtopic, baseSubject);
        baseSubject.addProperty(OntProp.isSubtopicOf, advancedSubject);
        baseSubject.addProperty(OntProp.isRequirement, advancedSubject);

        presentation.addProperty(OntProp.isPracticalPart, advancedSubject);
        advancedSubject.addProperty(OntProp.hasPracticalPart, presentation);
        lecture.addProperty(OntProp.isTheoreticalPart, baseSubject);
        baseSubject.addProperty(OntProp.hasTheoreticalPart, lecture);

        //Initial population of the dropdowns
        populateDropDown(model, topics, courses);


        //LISTENERS (not using lambdas cause of not collapsible in intellij)

        createTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String resName = newTopic.getText();

                newResource = model.createIndividual(OntologyProperties.URI + resName, topics);
                model.add(topics, OntProp.hasSubtopic, newResource);

                if (!dependantOn.isEmpty()) {
                    for (String item : dependantOn) {
                        newResource.addProperty(OntProp.hasRequirement, model.getIndividual(item));
                        model.getIndividual(item).addProperty(OntProp.isRequirement, newResource);
                    }
                }

                if (!subtopics.isEmpty()) {
                    for (String item : subtopics) {
                        newResource.addProperty(OntProp.hasSubtopic, model.getIndividual(item));
                        model.getIndividual(item).addProperty(OntProp.isSubtopicOf, newResource);
                    }
                }

                if (!types.isEmpty()) {
                    for (String item : types) {

                        if (item.equals("Presentation")) {
                            newResource.addProperty(OntProp.hasPracticalPart, model.getIndividual(OntologyProperties.URI + item));
                            model.getIndividual(OntologyProperties.URI + item).addProperty(OntProp.isPracticalPart, newResource);

                        } else if (item.equals("Lecture")) {
                            newResource.addProperty(OntProp.hasTheoreticalPart, model.getIndividual(OntologyProperties.URI + item));
                            model.getIndividual(OntologyProperties.URI + item).addProperty(OntProp.isTheoreticalPart, newResource);
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
                newResource = model.createIndividual(OntologyProperties.URI + cName, courses);
                model.add(courses, OntProp.hasTopic, newResource);

                if(!courseHasTopic.isEmpty()){
                    for (String item : courseHasTopic) {
                        newResource.addProperty(OntProp.hasTopic, model.getIndividual(item));
                        model.getIndividual(item).addProperty(OntProp.isTopicOf, newResource);
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
                if (FileHandler.importModel(model)) {
                    output.append("Loaded ontology from file '" + FileHandler.getFilename() + "'...\n");
                    populateDropDown(model, topics, courses);
                } else {
                    output.append("Loading ontology canceled...\n");
                }
            }
        });

        saveOntButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(FileHandler.exportModel(model)){
                    output.append("Wrote ontology to file '"+FileHandler.getFilename()+"'...\n");
                } else {
                    output.append("Writing ontology canceled...\n");
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
            drops[i].addItem("None");
        }
        NodeIterator allTopics = model.listObjectsOfProperty(topics, OntProp.hasSubtopic);
        List<String> resourceList = new ArrayList<>();

        while (allTopics.hasNext()) {
            resourceList.add(allTopics.nextNode().toString());
        }

        for (String item : resourceList) {
            dependsDrop.addItem(item);
            subtopicOf.addItem(item);
            deleteTopicDrop.addItem(item);
        }

        typeDrop.addItem("Presentation");
        typeDrop.addItem("Lecture");

        resourceList.forEach(courseTopicDrop::addItem);

        NodeIterator allCourses = model.listObjectsOfProperty(courses, OntProp.hasTopic);
        resourceList.clear();

        while(allCourses.hasNext()){
            resourceList.add(allCourses.nextNode().toString());
        }

        resourceList.forEach(deleteCourseDrop::addItem);
    }
}
