import org.apache.log4j.Level;

public class Runner {

    public static void main(String[] args) {
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
        GUI ui = new GUI();
    }
}
