import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private JComboBox spTopics;
    private JComboBox spCourses;
    private JLabel spLabTopics;
    private JLabel spLabCourses;
    private JComboBox spAction;
    private JLabel spLabAction;
    private JButton spTopicAddButton;
    private JButton spCourseAddButton;
    private JButton spGetInfoButton;
    private JComboBox spSelectedTopics;
    private JLabel spLabSelectedTopics;
    private JButton spDeleteTopicButton;
    private JComboBox spSelectedCourses;
    private JButton spDeleteCoursesButton;

    Individual newResource;
    List<String> dependantOn = new ArrayList<>();
    List<String> subtopics = new ArrayList<>();
    List<String> types = new ArrayList<>();
    List<String> courseHasTopic = new ArrayList<>();


    public GUI() {
        //Workaround for annoying width-changing dropdowns
        dependsDrop.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
        subtopicOf.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
        deleteCourseDrop.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
        deleteTopicDrop.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
        spTopics.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
        spCourses.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");

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

        StudyPlan studyPlan = new StudyPlan();

        //Initial population of the dropdowns
        populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getCourses());

        //Sparql prefixes as tooltip
        sparql.setToolTipText("<html>"+OntologyProperties.SparqlPrefixes.replaceAll("\n", "<br/>")+"</html>");

        //LISTENERS
        createTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                        newResource.addProperty(MyOntology.hasSubtopic, mOnt.getModel().getIndividual(item));
                        mOnt.getModel().getIndividual(item).addProperty(MyOntology.isSubtopicOf, newResource);
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
            }
        });

        saveCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        loadOntButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (FileHandler.importModel(mOnt.getModel())) {
                    output.append("Loaded ontology from file '" + FileHandler.getFilename() + "'...\n");
                    populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getCourses());
                } else {
                    output.append("Loading ontology canceled...\n");
                }
            }
        });

        saveOntButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(FileHandler.exportModel(mOnt.getModel())){
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
                Individual i = mOnt.getModel().getIndividual(courseToDel);
                i.remove();
                output.append("Removed course "+courseToDel+"...\n");
                populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getCourses());
            }
        });

        deleteTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String topicToDel = deleteTopicDrop.getSelectedItem().toString();
                Individual i = mOnt.getModel().getIndividual(topicToDel);
                i.remove();
                output.append("Removed topic "+topicToDel+"...\n");
                populateDropDown(mOnt.getModel(), mOnt.getTopics(), mOnt.getTopics());
            }
        });

        sparqlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String resultString;
                try {
                    resultString = new Sparql(mOnt.getModel(), sparql.getText()).executeQuery();
                    output.append("Query processed, opening result window...\n");
                    new ResultWindow("SPARQL Query Results", resultString);
                } catch(QueryParseException ex) {
                    ex.printStackTrace();
                    output.append("Query failed...\n");
                }
            }
        });

        spTopicAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = spTopics.getSelectedItem().toString();
                if(!selected.equals("None")){
                    studyPlan.addTopic(selected);
                    studyPlan.updateOptions(spAction, spSelectedTopics, spSelectedCourses);
                    output.append("Added topic: " + selected + "to studyplan\n");
                }
            }
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
            spCourses.addItem(item);
        }


    }
}
