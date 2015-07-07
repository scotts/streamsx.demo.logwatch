package streamsx.demo.logwatch.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.function7.Function;

public class SuspectFinder {
    public static TStream<Suspect> find(TStream<Failure> failures, final int attempts, final long seconds) {
        return failures.last(attempts)
                       .aggregate(new Function<List<Failure>, Suspect>() {
                           @Override
                           public Suspect apply(List<Failure> tuples) {
                               Date min = Collections.min(tuples).time;
                               Date max = Collections.max(tuples).time;
                               long diff = max.getTime() - min.getTime();

                               if (diff >= (seconds * 1000)) {
                                   return null;
                               }

                               return new Suspect(new Date(diff), max, attempts, tuples.get(0).rhost, tuples.get(0).user);
                           }
                       }, Suspect.class);
    }
}
