import org.apache.jena.ontology.Individual;
import org.apache.jena.rdf.model.Resource;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class StudyPlan{
    private List<String> topics;
    private List<String> courses;
    private JTextArea output;
    private MyOntology ontology;

    public StudyPlan(JTextArea output, MyOntology ontology){
        this();
        this.ontology = ontology;
        this.output = output;
    }

    public StudyPlan(){
        topics = new ArrayList<>();
        courses = new ArrayList<>();
    }

    public void addTopic(String name){
        if(!topics.contains(name) && ontology.getModel().getIndividual(name) != null){
            topics.add(name);
            output.append("Added topic "+name+" to selected topics in studyplan\n");
        }
        updateOptions(this.spAction, this.selTopics, this.selCourses);
    }

    public void addCourse(String name){
        if(!courses.contains(name) && ontology.getModel().getIndividual(name) != null){
            courses.add(name);
            output.append("Added course "+name+" to selected courses in studyplan\n");
        }
        updateOptions(this.spAction, this.selTopics, this.selCourses);
    }

    public void deleteCourse(String name){
        if(courses.contains(name)){
            courses.remove(name);
            output.append("Deleted course "+name+" from selected courses in studyplan\n");
        }
        updateOptions(this.spAction, this.selTopics, this.selCourses);
    }

    public void deleteTopic(String name){
        if(topics.contains(name)){
            topics.remove(name);
            output.append("Deleted topics "+name+" from selected topics in studyplan\n");
        }
        updateOptions(this.spAction, this.selTopics, this.selCourses);
    }


     private void updateOptions(JComboBox types, JComboBox selectedTopics, JComboBox selectedCourses){
        JComboBox[] drops = {types, selectedTopics, selectedCourses};
        for(int i = 0; i < drops.length; i++){
            drops[i].removeAllItems();
            drops[i].validate();
        }
        topics.forEach(selectedTopics::addItem);
        courses.forEach(selectedCourses::addItem);
    }

    private JComboBox spAction;
    private JComboBox selTopics;
    private JComboBox selCourses;

    public void updateDropdowns(JComboBox selTopics, JComboBox selCourses, JComboBox action){
        this.selCourses = selCourses;
        this.selTopics = selTopics;
        this.spAction = action;
    }
}

