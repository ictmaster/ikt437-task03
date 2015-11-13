import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class StudyPlan {
    private List<String> topics;
    private List<String> courses;

    public StudyPlan(){
        topics = new ArrayList<>();
        courses = new ArrayList<>();
    }

    public void addTopic(String name){
        if(!topics.contains(name)){
            topics.add(name);
        }
    }

    public void addCourse(String name){
        if(!courses.contains(name)){
            courses.add(name);
        }
    }

    public void deleteCourse(String name){
        if(courses.contains(name)){
            courses.remove(name);
        }
    }

    public void deleteTopic(String name){
        if(topics.contains(name)){
            topics.remove(name);
        }
    }


    public void updateOptions(JComboBox types, JComboBox selectedTopics, JComboBox selectedCourses){
        JComboBox[] drops = {types, selectedTopics, selectedCourses};
        for(int i = 0; i < drops.length; i++){
            drops[i].removeAllItems();
            drops[i].validate();
        }
        topics.forEach(selectedTopics::addItem);
        courses.forEach(selectedCourses::addItem);
    }


    public void coursesWithTopic(MyOntology ont){
    }

}

