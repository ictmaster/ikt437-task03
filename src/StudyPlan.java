import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StudyPlan{
    private List<String> topics;
    private List<String> courses;
    private JTextArea output;
    private MyOntology ontology;
    private JComboBox spAction;
    private JComboBox selTopics;
    private JComboBox selCourses;
    private HashMap<PLAN_TYPE,String> planTypeMap;
    private enum PLAN_TYPE{
        COURSES_WITH_TOPIC,                 //case1 - 1 , 0
        PREREQUICITES_OF_COURSES_TOPICS,    //case2 - 0 , 1
        COURSE_PREREQUICTES_FOR_COURSE,     //case3 - 0 , 1
        TOPICS_IN_COURSES,                  //case4 - 0 , 1+
        SUBTOPICS_OF_TOPIC                  //case5 - 2+, 0 // make first one 'main' topic
    }

    public StudyPlan(JTextArea output, MyOntology ontology){
        this();
        this.ontology = ontology;
        this.output = output;
    }

    public StudyPlan(){
        topics = new ArrayList<>();
        courses = new ArrayList<>();
        planTypeMap = new HashMap<>();
        planTypeMap.put(PLAN_TYPE.COURSES_WITH_TOPIC, "Courses with selected topic");
        planTypeMap.put(PLAN_TYPE.PREREQUICITES_OF_COURSES_TOPICS, "Required knowledge to follow course");
        planTypeMap.put(PLAN_TYPE.COURSE_PREREQUICTES_FOR_COURSE, "Prerequisite courses");
        planTypeMap.put(PLAN_TYPE.TOPICS_IN_COURSES, "Topics thought in courses");
        planTypeMap.put(PLAN_TYPE.SUBTOPICS_OF_TOPIC, "Subtopics covered");
    }

    public void addTopic(String name){
        if(!topics.contains(name) && ontology.getModel().getIndividual(name) != null){
            topics.add(name);
            output.append("Added topic "+name+" to selected topics in studyplan\n");
        }
        updateOptions();
    }

    public void addCourse(String name){
        if(!courses.contains(name) && ontology.getModel().getIndividual(name) != null){
            courses.add(name);
            output.append("Added course "+name+" to selected courses in studyplan\n");
        }
        updateOptions();
    }

    public void deleteCourse(String name){
        if(courses.contains(name)){
            courses.remove(name);
            output.append("Deleted course "+name+" from selected courses in studyplan\n");
        }
        updateOptions();
    }

    public void deleteTopic(String name){
        if(topics.contains(name)){
            topics.remove(name);
            output.append("Deleted topics "+name+" from selected topics in studyplan\n");
        }
        updateOptions();
    }

    private void updateOptions(){
        JComboBox[] drops = {spAction, selTopics, selCourses};
        for(int i = 0; i < drops.length; i++){
            drops[i].removeAllItems();
            drops[i].validate();
        }
        topics.forEach(selTopics::addItem);
        courses.forEach(selCourses::addItem);

        //TODO: fill out all PLAN_TYPES
        if(topics.size() == 1 && courses.size() == 0){
            spAction.addItem(planTypeMap.get(PLAN_TYPE.COURSES_WITH_TOPIC));
        }


        if(spAction.getItemCount() <= 0)
            spAction.addItem("No action available");

        if(selTopics.getItemCount() <= 0)
            selTopics.addItem("No topics selected");

        if(selCourses.getItemCount() <= 0)
            selCourses.addItem("No courses selected");
    }

    public void displayPlan(){

    }


    public void updateDropdowns(JComboBox selTopics, JComboBox selCourses, JComboBox action){
        this.selCourses = selCourses;
        this.selTopics = selTopics;
        this.spAction = action;
        updateOptions();
    }
}

