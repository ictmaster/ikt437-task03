import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
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


        //for(int i =0;i < UIManager.getInstalledLookAndFeels().length; i++)
        //    System.out.println(UIManager.getInstalledLookAndFeels()[i]);

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


        //ADD PREDEFINED STUFF
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
        mathCourse.setPropertyValue(hasTopic, baseSubject);
        mathCourse.setPropertyValue(hasTopic, advancedSubject);
        advancedSubject.setPropertyValue(hasRequirement, baseSubject);
        advancedSubject.setPropertyValue(hasSubtopic, baseSubject);
        baseSubject.setPropertyValue(isSubtopicOf, advancedSubject);
        baseSubject.setPropertyValue(isRequirement, advancedSubject);

        presentation.setPropertyValue(isPracticalPart, advancedSubject);
        advancedSubject.setPropertyValue(hasPracticalPart, presentation);
        lecture.setPropertyValue(isTheoreticalPart, baseSubject);
        baseSubject.setPropertyValue(hasTheoreticalPart, lecture);
        //ADD PREDEFINED STUFF END

        //Populate the dropdown with predefined courses
        populateDropDown(model, topics, courses);


        createTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String resName = newTopic.getText();

                newResource = model.createIndividual(uri + resName, topics);
                model.add(topics, hasSubtopic, newResource);

                if (!dependantOn.isEmpty()) {
                    for (String item : dependantOn) {
                        newResource.setPropertyValue(hasRequirement, model.getIndividual(item));
                        model.getIndividual(item).setPropertyValue(isRequirement, newResource);
                    }
                }

                if (!subtopics.isEmpty()) {
                    for (String item : subtopics) {
                        newResource.setPropertyValue(isSubtopicOf, model.getIndividual(item));
                        model.getIndividual(item).setPropertyValue(isSubtopicOf, newResource);
                    }
                }

                if (!types.isEmpty()) {
                    for (String item : types) {

                        if (item.equals("Presentation")) {
                            newResource.setPropertyValue(hasPracticalPart, model.getIndividual(uri+item));
                            model.getIndividual(uri + item).setPropertyValue(hasPracticalPart, newResource);

                        } else if (item.equals("Lecture")) {
                            newResource.setPropertyValue(hasTheoreticalPart, model.getIndividual(uri+item));
                            model.getIndividual(uri + item).setPropertyValue(hasPracticalPart, newResource);
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
                        newResource.setPropertyValue(hasTopic, model.getIndividual(item));
                        model.getIndividual(item).setPropertyValue(hasTopic, newResource);
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

                        FileReader in = new FileReader(jfc.getSelectedFile());
                        model.read(new FileInputStream(jfc.getSelectedFile().getPath()),null, "TTL");

                                //model.read(in, uri); TODO: Fix this error

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

        //courseTopicDrop.addItem("None");

        resourceList.forEach(courseTopicDrop::addItem);

        NodeIterator allCourses = model.listObjectsOfProperty(courses, hasTopic);
        resourceList.clear();

        while(allCourses.hasNext()){
            resourceList.add(allCourses.nextNode().toString());
        }

        resourceList.forEach(deleteCourseDrop::addItem);
    }
}
