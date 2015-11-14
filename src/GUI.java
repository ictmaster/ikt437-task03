import org.apache.jena.ontology.Individual;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GUI extends JFrame {
    private JButton addDepend;
    private JButton addSub;
    private JButton addTopicButton;
    private JButton addType;
    private JButton createTopicButton;
    private JButton deleteCourseButton;
    private JButton deleteTopicButton;
    private JButton loadOntButton;
    private JButton saveCourseButton;
    private JButton saveOntButton;
    private JButton sparqlButton;
    private JButton spCourseAddButton;
    private JButton spDeleteCoursesButton;
    private JButton spDeleteTopicButton;
    private JButton spGetInfoButton;
    private JButton spTopicAddButton;
    private JComboBox courseTopicDrop;
    private JComboBox deleteCourseDrop;
    private JComboBox deleteTopicDrop;
    private JComboBox dependsDrop;
    private JComboBox spAction;
    private JComboBox spCourses;
    private JComboBox spSelectedCourses;
    private JComboBox spSelectedTopics;
    private JComboBox spTopics;
    private JComboBox subtopicOf;
    private JComboBox typeDrop;
    private JLabel spLabAction;
    private JLabel spLabCourses;
    private JLabel spLabSelectedTopics;
    private JLabel spLabTopics;
    private JPanel rootPanel;
    private JScrollPane outputScroller;
    private JTabbedPane mainTabbedPane;
    private JTextArea output;
    private JTextArea sparql;
    private JTextField courseName;
    private JTextField newTopic;

    Individual newResource;
    List<String> dependantOn = new ArrayList<>();
    List<String> subtopics = new ArrayList<>();
    List<String> types = new ArrayList<>();
    List<String> courseHasTopic = new ArrayList<>();


    public GUI() {
        JComboBox[] drops = {dependsDrop, subtopicOf, typeDrop, courseTopicDrop, deleteCourseDrop, deleteTopicDrop, spTopics, spCourses};
        for(int i = 0; i < drops.length; i++) {
            drops[i].setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
        }

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
        MyOntology mOnt = new MyOntology();

        //Create studyplan object and send in output for console capabilities
        StudyPlan studyPlan = new StudyPlan(output, mOnt);

        //Initial population of the dropdowns
        populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getCourses());

        //Sparql prefixes as tooltip
        sparql.setToolTipText("<html>"+OntologyProperties.SparqlPrefixes.replaceAll("\n", "<br/>")+"</html>");

        createTopicButton.addActionListener(e -> {
            String resName = newTopic.getText();

            newResource = mOnt.getModel().createIndividual(OntologyProperties.URI + resName, mOnt.getTopics());
            mOnt.getModel().add(mOnt.getTopics(), MyOntology.hasSubtopic, newResource);

            if (!dependantOn.isEmpty()) {
                for (String item : dependantOn) {
                    newResource.addProperty(MyOntology.hasRequirement, mOnt.getModel().getIndividual(item));
                    mOnt.getModel().getIndividual(item).addProperty(MyOntology.isRequirement, newResource);
                }
            }

            if (!subtopics.isEmpty()) {
                for (String item : subtopics) {
                    newResource.addProperty(MyOntology.isSubtopicOf, mOnt.getModel().getIndividual(item));
                    mOnt.getModel().getIndividual(item).addProperty(MyOntology.hasSubtopic, newResource);
                }
            }

            if (!types.isEmpty()) {
                for (String item : types) {

                    if (item.equals("Presentation")) {
                        newResource.addProperty(MyOntology.hasPracticalPart, mOnt.getModel().getIndividual(OntologyProperties.URI + item));
                        mOnt.getModel().getIndividual(OntologyProperties.URI + item).addProperty(MyOntology.isPracticalPart, newResource);

                    } else if (item.equals("Lecture")) {
                        newResource.addProperty(MyOntology.hasTheoreticalPart, mOnt.getModel().getIndividual(OntologyProperties.URI + item));
                        mOnt.getModel().getIndividual(OntologyProperties.URI + item).addProperty(MyOntology.isTheoreticalPart, newResource);
                    }
                }
            }
            output.append("Added topic : " + resName + "\n");
            populateDropDown(mOnt.getModel(),mOnt.getTopics(), mOnt.getCourses());

            dependantOn.clear();
            subtopics.clear();
            types.clear();
        });


        saveCourseButton.addActionListener(e -> {
            String cName = courseName.getText();
            newResource = mOnt.getModel().createIndividual(OntologyProperties.URI + cName, mOnt.getCourses());
            mOnt.getModel().add(mOnt.getCourses(), MyOntology.hasTopic, newResource);

            if(!courseHasTopic.isEmpty()){
                for (String item : courseHasTopic) {
                    newResource.addProperty(MyOntology.hasTopic, mOnt.getModel().getIndividual(item));
                    mOnt.getModel().getIndividual(item).addProperty(MyOntology.isTopicOf, newResource);
                }
            }
            output.append("Added course : " + cName + "\n");
            courseHasTopic.clear();
            populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getCourses());
        });

        //Saving and loading from file
        loadOntButton.addActionListener(e -> {
            if (FileHandler.importModel(mOnt.getModel())) {
                output.append("Loaded ontology from file '" + FileHandler.getFilename() + "'...\n");
                populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getCourses());
            } else {
                output.append("Loading ontology canceled...\n");
            }
        });

        saveOntButton.addActionListener(e -> {
            if(FileHandler.exportModel(mOnt.getModel())){
                output.append("Wrote ontology to file '"+FileHandler.getFilename()+"'...\n");
            } else {
                output.append("Writing ontology canceled...\n");
            }
        });

        addTopicButton.addActionListener(new ButtonHandler(courseTopicDrop, courseHasTopic, output, ButtonHandler.ButtonAction.ADD));
        addType.addActionListener(new ButtonHandler(typeDrop, types, output, ButtonHandler.ButtonAction.ADD));
        addDepend.addActionListener(new ButtonHandler(dependsDrop, dependantOn, output, ButtonHandler.ButtonAction.ADD));
        addSub.addActionListener(new ButtonHandler(subtopicOf, subtopics, output, ButtonHandler.ButtonAction.ADD));

        deleteCourseButton.addActionListener(new ButtonHandler(deleteCourseDrop,mOnt,output,this, ButtonHandler.ButtonAction.DELETE));
        deleteTopicButton.addActionListener(new ButtonHandler(deleteTopicDrop,mOnt,output,this, ButtonHandler.ButtonAction.DELETE));

        sparqlButton.addActionListener(e -> {
            String resultString;
            try {
                resultString = new Sparql(mOnt.getModel(), sparql.getText()).executeQuery();
                output.append("Query processed, opening result window...\n");
                new ResultWindow("SPARQL Query Results", resultString);
            } catch(QueryParseException ex) {
                ex.printStackTrace();
                output.append("Query failed...\n");
            }
        });
        studyPlan.updateDropdowns(spSelectedTopics, spSelectedCourses, spAction);
        spTopicAddButton.addActionListener(e -> {
            studyPlan.addTopic(spTopics.getSelectedItem().toString());
            populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getCourses());
        });
        spCourseAddButton.addActionListener(e -> {
            studyPlan.addCourse(spCourses.getSelectedItem().toString());
            populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getCourses());
        });

        spDeleteTopicButton.addActionListener(e -> {
            studyPlan.deleteTopic(spSelectedTopics.getSelectedItem().toString());
            populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getCourses());
        });
        spDeleteCoursesButton.addActionListener(e -> {
            studyPlan.deleteCourse(spSelectedCourses.getSelectedItem().toString());
            populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getCourses());
        });
        spGetInfoButton.addActionListener(e -> {
            studyPlan.displayPlan();
            populateDropDown(mOnt.getModel(),mOnt.getTopics(), mOnt.getCourses());
        });

    }

    public void populateDropDown(Model model, Resource topics, Resource courses) {

        JComboBox[] drops = {dependsDrop, subtopicOf, typeDrop, courseTopicDrop, deleteCourseDrop, deleteTopicDrop, spTopics, spCourses};
        for(int i = 0; i < drops.length; i++){
            drops[i].removeAllItems();
            drops[i].validate();
            drops[i].addItem("None");
        }
        NodeIterator allTopics = model.listObjectsOfProperty(topics, MyOntology.hasSubtopic);
        List<String> resourceList = new ArrayList<>();

        while (allTopics.hasNext()) {
            resourceList.add(allTopics.nextNode().toString());
        }

        //Topics
        for (String item : resourceList) {
            dependsDrop.addItem(item);
            subtopicOf.addItem(item);
            deleteTopicDrop.addItem(item);
            courseTopicDrop.addItem(item);

            if(((DefaultComboBoxModel)spSelectedTopics.getModel()).getIndexOf(item) == -1)
                spTopics.addItem(item);
        }

        typeDrop.addItem("Presentation");
        typeDrop.addItem("Lecture");

        NodeIterator allCourses = model.listObjectsOfProperty(courses, MyOntology.hasTopic);
        resourceList.clear();

        while(allCourses.hasNext()){
            resourceList.add(allCourses.nextNode().toString());
        }
        //Courses
        for (String item : resourceList){
            deleteCourseDrop.addItem(item);
            if(((DefaultComboBoxModel)spSelectedCourses.getModel()).getIndexOf(item) == -1)
                spCourses.addItem(item);
        }
    }
}
