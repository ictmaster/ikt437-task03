import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

public class FileHandler {
    private static String filename;

    public static String getFilename(){
        return FileHandler.filename;
    }

    public static boolean importModel(OntModel model){
        try {
            JFileChooser jfc = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Turtle File", "ttl");
            jfc.setFileFilter(filter);
            File currentDir = new File(System.getProperty("user.dir"));
            jfc.setCurrentDirectory(currentDir);

            int returnVal = jfc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                filename = jfc.getSelectedFile().getName();
                model.read(new FileInputStream(jfc.getSelectedFile().getPath()), null, "TTL");
                return true;
            } else {
                return false;
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    public static boolean exportModel(OntModel model){
        FileWriter out = null;
        JFileChooser jfc = new JFileChooser();
        try {
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Turtle File", "ttl");
            jfc.setFileFilter(filter);
            File currentDir = new File(System.getProperty("user.dir"));
            jfc.setCurrentDirectory(currentDir);

            int returnVal = jfc.showSaveDialog(null);

            if(returnVal == JFileChooser.APPROVE_OPTION){
                filename = jfc.getSelectedFile().getName();
                out = new FileWriter( jfc.getSelectedFile() );
                model.write( out, "Turtle" );
                return true;
            }else{
                return false;
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
        return false;
    }
}
