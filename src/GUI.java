import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
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
    private JLabel Topics;

    //Some initialization
    ObjectProperty isSubstopicOf;
    ObjectProperty hasSubtopic;
    ObjectProperty hasRequirement;
    ObjectProperty isRequirement;
    Individual newResource;
    List<String> dependantOn = new ArrayList<>();
    List<String> subtopics = new ArrayList<>();

    OntModel model;

    public GUI() {
        setContentPane(rootPanel);
        pack();
        setVisible(true);
        setBounds(100,100, 500,500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String uri = "http://jonasn12.uia.io/ontology#";

        // Create main ontology model
        model = ModelFactory.createOntologyModel();

        // Properties
        ObjectProperty isSubstopicOf = model.createObjectProperty(uri + "isSubtopicOf");
        ObjectProperty hasSubtopic = model.createObjectProperty(uri + "hasSubtopic");
        ObjectProperty hasRequirement = model.createObjectProperty(uri + "hasRequirement");
        ObjectProperty isRequirement = model.createObjectProperty(uri + "isRequirement");

        // Configuring props..
        isSubstopicOf.isInverseOf(hasSubtopic);
        hasRequirement.isInverseOf(isRequirement);

        OntClass topics = model.createClass(uri + "Topics");
        //Create some topics to have
        Individual advancedSubject = model.createIndividual(uri + "Integrals", topics);
        Individual baseSubject = model.createIndividual(uri + "Algebra", topics);

        model.add(topics,hasSubtopic,advancedSubject);
        model.add(topics,hasSubtopic,baseSubject);

        advancedSubject.setPropertyValue(hasRequirement, baseSubject);
        advancedSubject.setPropertyValue(hasSubtopic, baseSubject);
        baseSubject.setPropertyValue(isSubstopicOf, advancedSubject);
        baseSubject.setPropertyValue(isRequirement, advancedSubject);

        populateDropDown(model, topics);

        createTopicButton.addActionListener(e -> {
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
                    newResource.setPropertyValue(isSubstopicOf, model.getIndividual(item));
                    model.getIndividual(item).setPropertyValue(isSubstopicOf, newResource);
                }
            }

            populateDropDown(model, topics);

            dependantOn.clear();
            subtopics.clear();

            NodeIterator allTopics = model.listObjectsOfProperty(topics, hasSubtopic);

            while (allTopics.hasNext()) {
                System.out.println(allTopics.nextNode());
            }

        });

        saveOntButton.addActionListener(e -> {

            FileWriter out = null;
            try {
                out = new FileWriter( "ont.ttl" );
                model.write( out, "Turtle" );
                System.out.println("Wrote ontology to file...");
            } catch (IOException w) {
                w.printStackTrace();
            } finally {
                if (out != null) {
                    try {out.close();} catch (IOException ignore) {}
                }
            }
        });

        // Listeners
        addDepend.addActionListener(e -> {
            String selected = dependsDrop.getSelectedItem().toString();
            if(!selected.equals("None")) {
                output.append(" Added dependency : " + selected + "\n");
                dependantOn.add(selected);
            }
        });

        addSub.addActionListener(e -> {
            String selected = subtopicOf.getSelectedItem().toString();
            if(!selected.equals("None")) {
            output.append(" Added subtopic : " + selected+"\n");
            subtopics.add(selected);

            }
        });
    }

        public void populateDropDown(Model model, Resource topics) {

            dependsDrop.removeAllItems();
            subtopicOf.removeAllItems();
            dependsDrop.validate();
            subtopicOf.validate();

            NodeIterator allTopics = model.listObjectsOfProperty(topics, hasSubtopic);

            // Get model....
            Resource test = model.getResource("Algebra");
            System.out.println(test);

            List<String> resourceList = new ArrayList<>();

            while (allTopics.hasNext()) {
                resourceList.add(allTopics.nextNode().toString());
            }

            dependsDrop.addItem("None");
            subtopicOf.addItem("None");

            for (String item : resourceList) {
                dependsDrop.addItem(item);
                subtopicOf.addItem(item);
            }
        }
    }
