package streamsx.demo.logwatch.topology;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.ibm.streamsx.topology.function7.Consumer;

public class TextFileSink<T> implements Consumer<T> {
    private String fileName;
    private boolean initialized = false;
    private transient PrintWriter writer;

    public TextFileSink(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void accept(T tuple) {
        if (!initialized) {
            try {
                writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
            } catch (Exception e) {
                System.out.println("Caught exception: " + e.toString());
            }
            initialized = true;
        }
        writer.println(tuple);
        writer.flush();
    }
}
