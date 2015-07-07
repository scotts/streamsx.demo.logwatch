package streamsx.demo.logwatch.topology;

import java.util.Date;

import com.ibm.streamsx.topology.tuple.Keyable;

public class Failure implements Keyable<String>, Comparable<Failure> {
    public final Date time;
    public final String uid;
    public final String euid;
    public final String tty;
    public final String rhost;
    public final String user;

    public Failure(Date time, String uid, String euid, String tty, String rhost, String user) {
        this.time = time;
        this.uid = uid;
        this.euid = euid;
        this.tty = tty;
        this.rhost = rhost;
        this.user = user;
    }

    @Override
    public String toString() {
        return "time=" + time.toString() + ", uid=" + uid + ", euid=" + euid + ", tty=" + tty + ", rhost=" + rhost + ", user=" + user;
    }

    @Override
    public String getKey() {
        return rhost;
    }

    @Override
    public int compareTo(Failure other) {
        return this.time.compareTo(other.time);
    }
}

