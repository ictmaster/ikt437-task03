import org.apache.jena.ontology.Individual;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ButtonHandler implements ActionListener {
    private GUI gui;
    private MyOntology ontology;
    private List<String> target;
    private JComboBox select;
    private JTextArea output;
    private ButtonAction action;

    public ButtonHandler(JComboBox select, JTextArea output, ButtonAction action){
        this.select = select;
        this.output = output;
        this.action = action;
    }

    public ButtonHandler(JComboBox select, List<String> target, JTextArea output, ButtonAction action){
        this(select, output, action);
        this.target = target;
    }

    public ButtonHandler(JComboBox select, MyOntology ontology, JTextArea output, GUI gui, ButtonAction action){
        this(select, output, action);
        this.ontology = ontology;
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String selected = this.select.getSelectedItem().toString();
        if(this.action == ButtonAction.ADD){
            if(!selected.equals("None")){
                this.target.add(selected);
                output.append("Added "+selected+"...\n");
            }
        }else if(this.action == ButtonAction.DELETE){
            Individual i = ontology.getModel().getIndividual(selected);
            i.remove();
            output.append("Removed "+selected+"...\n");
            gui.populateDropDown(ontology.getModel(), ontology.getTopics(), ontology.getCourses());
        }
    }

    public enum ButtonAction{
        ADD,
        DELETE
    }
}
