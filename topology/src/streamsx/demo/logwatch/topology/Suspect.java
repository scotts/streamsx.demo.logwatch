package streamsx.demo.logwatch.topology;

import java.util.Date;

import com.ibm.streamsx.topology.tuple.Keyable;

public class Suspect implements Keyable<String> {
    public final Date diff;
    public final Date last;
    public final int attempts;
    public final String rhost;
    public final String user;

    public Suspect(Date diff, Date last, int attempts, String rhost, String user) {
        this.diff = diff;
        this.last = last;
        this.attempts = attempts; 
        this.rhost = rhost;
        this.user = user;
    }

    @Override
    public String toString() {
        return "diff=" + diff.toString() + ", last=" + last.toString() + ", attempts=" + attempts + ", rhost=" + rhost + ", user=" + user;
    }

    @Override
    public String getKey() {
        return user;
    }
}

