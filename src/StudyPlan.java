import org.apache.jena.base.Sys;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        PREREQUISITES_OF_COURSES_TOPICS,    //case2 - 0 , 1
        COURSE_PREREQUISITES_FOR_COURSE,     //case3 - 0 , 1
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
        planTypeMap.put(PLAN_TYPE.PREREQUISITES_OF_COURSES_TOPICS, "Required knowledge to follow course");
        planTypeMap.put(PLAN_TYPE.COURSE_PREREQUISITES_FOR_COURSE, "Prerequisite courses");
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

        if(topics.size() == 1 && courses.size() == 0){ //UC1
            spAction.addItem(planTypeMap.get(PLAN_TYPE.COURSES_WITH_TOPIC));
        }
        if(topics.size() == 0 && courses.size() == 1){ //UC2
            spAction.addItem(planTypeMap.get(PLAN_TYPE.PREREQUISITES_OF_COURSES_TOPICS));
        }
        if(topics.size() == 0 && courses.size() == 1){ //UC3
            spAction.addItem(planTypeMap.get(PLAN_TYPE.COURSE_PREREQUISITES_FOR_COURSE));
        }
        if(topics.size() == 0 && courses.size() > 0) { //UC4
            spAction.addItem(planTypeMap.get(PLAN_TYPE.TOPICS_IN_COURSES));
        }
        if(topics.size() > 1 && courses.size() == 0) { //UC5
            spAction.addItem(planTypeMap.get(PLAN_TYPE.SUBTOPICS_OF_TOPIC));
        }


        if(spAction.getItemCount() <= 0)
            spAction.addItem("No action available");

        if(selTopics.getItemCount() <= 0)
            selTopics.addItem("No topics selected");

        if(selCourses.getItemCount() <= 0)
            selCourses.addItem("No courses selected");
    }

    public void displayPlan(){
        String selectedType = spAction.getSelectedItem().toString();
        for(Map.Entry<PLAN_TYPE, String> entry : planTypeMap.entrySet()){
            if(selectedType.equals(entry.getValue())){
                Sparql sparql;
                String sparqlQueryString = "";
                switch (entry.getKey()){
                    case COURSES_WITH_TOPIC: //Use Case 1
                        sparqlQueryString = "SELECT DISTINCT ?course ?part\n" +
                                "WHERE {\n" +
                                "\t?course j:hasTopic <"+selTopics.getItemAt(0).toString()+"> .\n" +
                                "\tOPTIONAL { ?part j:isPracticalPart <"+selTopics.getItemAt(0).toString()+"> }\n" +
                                "}";
                        break;
                    case PREREQUISITES_OF_COURSES_TOPICS: //Use Case 2
                        sparqlQueryString = "SELECT DISTINCT ?topic\n" +
                                "WHERE {\n" +
                                "\t?topic j:isRequirement* ?depTop .\n" +
                                "\t?depTop j:isTopicOf <"+selCourses.getItemAt(0).toString()+"> .\n" +
                                "\tMINUS { ?topic j:isTopicOf <"+selCourses.getItemAt(0).toString()+"> }\n" +
                                "}";
                        break;
                    case COURSE_PREREQUISITES_FOR_COURSE: //Use Case 3
                        sparqlQueryString = "SELECT DISTINCT ?precourse\n" +
                                "WHERE {\n" +
                                "\t<"+selCourses.getItemAt(0).toString()+"> j:hasTopic ?courseTopic .\n" +
                                "\t?courseTopic j:hasRequirement* ?pretopic .\n" +
                                "\t?pretopic j:isTopicOf ?precourse .\n" +
                                "\tMINUS { ?pretopic j:isTopicOf <"+selCourses.getItemAt(0).toString()+"> }\n" +
                                "}";
                        break;
                    case TOPICS_IN_COURSES: //Use Case 4
                        sparqlQueryString = "SELECT DISTINCT ?course ?topic\n" +
                                "WHERE {\n" +
                                "\t{?course j:hasTopic ?topic} .\n";
                        for(int i = 0; i<selCourses.getItemCount();i++){
                            if(i == 0){
                                sparqlQueryString += "\t{<"+selCourses.getItemAt(i).toString()+"> j:hasTopic ?topic}\n";
                            }else{
                                sparqlQueryString += "\tUNION {<"+selCourses.getItemAt(i).toString()+"> j:hasTopic ?topic}\n";
                            }
                        }
                        sparqlQueryString += "\t}";
                        break;
                    case SUBTOPICS_OF_TOPIC:
                        
                        break;

                    default:
                        output.append("Some error happened...");
                        return;
                }
                sparql = new Sparql(ontology.getModel(), sparqlQueryString);
                new ResultWindow(entry.getValue(), sparql.executeQuery());
            }
        }
        topics.clear();
        courses.clear();
        updateOptions();
    }


    public void updateDropdowns(JComboBox selTopics, JComboBox selCourses, JComboBox action){
        this.selCourses = selCourses;
        this.selTopics = selTopics;
        this.spAction = action;
        updateOptions();
    }
}

