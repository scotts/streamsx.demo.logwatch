package streamsx.demo.logwatch.topology;

import java.util.Date;

public class Breakin {
    public final Date time;
    public final String rhost;
    public final String user;

    public Breakin(Date time, String rhost, String user) {
        this.time = time;
        this.rhost = rhost;
        this.user = user;
    }

    @Override
    public String toString() {
        return "time=" + time.toString() + ", rhost=" + rhost + ", user=" + user;
    }
}
