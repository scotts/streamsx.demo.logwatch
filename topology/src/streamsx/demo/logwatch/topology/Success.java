package streamsx.demo.logwatch.topology;

import java.util.Date;

import com.ibm.streamsx.topology.tuple.Keyable;

public class Success implements Keyable<String> {
    public final Date time;
    public final String user;

    public Success(Date t, String u) {
        this.time = t;
        this.user = u;
    }

    public String toString() {
        return "time=" + time.toString() + ", user=" + user;
    }

    @Override
    public String getKey() {
        return user;
    }
}
