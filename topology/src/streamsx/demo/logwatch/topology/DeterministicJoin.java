package streamsx.demo.logwatch.topology;

import java.util.ArrayList;
import java.util.Date;

import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.function7.BiFunction;
import com.ibm.streamsx.topology.function7.Function;
import com.ibm.streamsx.topology.logic.Logic;

public class DeterministicJoin {
    public static TStream<Breakin> join(TStream<Suspect> failures, TStream<Success> successes) {
        TStream<Breakin> failureBreakins = failures.join(successes.last(),
                Logic.first(new BiFunction<Suspect, Success, Breakin>() {
                    ArrayList<Success> logins = new ArrayList<Success>();

                    @Override
                    public Breakin apply(Suspect failure, Success success) {
                        logins.add(success);

                        Breakin breakin = null;
                        int stale = 0;
                        for (int i = 0; i < logins.size(); i++) {
                            long diff = logins.get(i).time.getTime() - failure.last.getTime();
                            if (0 <= diff && diff <= 60 * 1000) { // diff is in milliseconds
                                breakin = new Breakin(logins.get(i).time, failure.rhost, failure.user);
                                logins.remove(i);
                                stale--;
                                break;
                            }
                            else if (diff > 60) {
                                stale = i;
                            }
                        }

                        if (stale > 0) {
                            logins.subList(0, stale).clear();
                        }

                        return breakin;
                    }
        }), Breakin.class);

        TStream<Breakin> successBreakins = successes.join(failures.last(),
                Logic.first(new BiFunction<Success, Suspect, Breakin>() {
                    ArrayList<Suspect> suspects = new ArrayList<Suspect>();

                    @Override
                    public Breakin apply(Success success, Suspect failure) {
                        suspects.add(failure);

                        Breakin breakin = null;
                        int stale = 0;
                        for (int i = 0; i < suspects.size(); i++) {
                            long diff = success.time.getTime() - suspects.get(i).last.getTime();
                            if (0 <= diff && diff <= 60 * 1000) {
                                breakin = new Breakin(success.time, suspects.get(i).rhost, suspects.get(i).user);
                                suspects.remove(i);
                                stale--;
                                break;
                            }
                            else if (diff > 60) {
                                stale = i;
                            }
                        }

                        if (stale > 0) {
                            suspects.subList(0, stale).clear();
                        }

                        return breakin;
                    }
        }), Breakin.class);

        return successBreakins.union(failureBreakins);
    }
}
