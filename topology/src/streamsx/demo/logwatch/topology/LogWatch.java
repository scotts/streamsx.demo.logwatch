package streamsx.demo.logwatch.topology;

import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.file.FileStreams;
import com.ibm.streamsx.topology.function7.Consumer;
import com.ibm.streamsx.topology.function7.Predicate;
import com.ibm.streamsx.topology.function7.Function;

public class LogWatch {
    public static void main(String[] args) throws Exception {
        String contextType = args[0];
        String messagesFile = args[1];
        String breakinsFile = args[2];
        TStream<Breakin> breakins = createLogWatchTopology(messagesFile, breakinsFile);
        StreamsContextFactory.getStreamsContext(contextType).submit(breakins.topology()).get();
    }
    
    public static class LogLine {
        public final Date time;
        public final String hostname;
        public final String service;
        public final String message;

        public LogLine(Date t, String h, String s, String m) {
            this.time = t;
            this.hostname = h;
            this.service = s;
            this.message = m;
        }

        public String toString() {
            return "time=" + time.toString() + ", hostname=" + hostname + ", service=" + service + ", message=" + message;
        }
    }

    public static TStream<Breakin> createLogWatchTopology(String messagesFile, String breakinsFile) {
        Topology topology = new Topology("LogWatch");

        TStream<String> messagesFileName = topology.strings(messagesFile);
        TStream<String> rawLines = FileStreams.textFileReader(messagesFileName);

        TStream<LogLine> parsedLines = rawLines.transform(new Function<String, LogLine>() {
            @Override
            public LogLine apply(String tuple) {
                String[] tokens = tuple.split(" ");

                DateFormat df = new SimpleDateFormat("MMMddHH:mm:ssyyyy");
                Date date = null;
                try {
                    date = df.parse(tokens[0] + tokens[1] + tokens[2] + "2011");
                } catch (Exception e) {
                    System.out.println("Exception caught: " + e.toString());
                }

                String message = "";
                for (int i = 5; i < tokens.length; i++) {
                    message = message.concat(tokens[i]);
                    message = message.concat(" ");
                }

                return new LogLine(date, tokens[3], tokens[4], message);
            }
        }, LogLine.class);

        TStream<LogLine> rawFailures = parsedLines.filter(new Predicate<LogLine>() {
            @Override
            public boolean test(LogLine tuple) {
                return tuple.service.contains("sshd") && tuple.message.contains("authentication failure");
            }
        });

        TStream<Failure> failures = rawFailures.transform(new Function<LogLine, Failure>() {
            @Override
            public Failure apply(LogLine tuple) {
                String[] semisplit = tuple.message.split(";");
                String[] eqsplit = semisplit[1].split("[= ]");

                return new Failure(tuple.time, eqsplit[4], eqsplit[6], eqsplit[8], eqsplit[12], (eqsplit.length == 16? eqsplit[15]: ""));
            }
        }, Failure.class);

        TStream<Suspect> suspects = SuspectFinder.find(failures, 5, 60);

        TStream<LogLine> rawSuccesses = parsedLines.filter(new Predicate<LogLine>() {
            @Override
            public boolean test(LogLine tuple) {
                return tuple.service.contains("sshd") && tuple.message.contains("session opened for user");
            }
        });

        TStream<Success> successes = rawSuccesses.transform(new Function<LogLine, Success>() {
            @Override
            public Success apply(LogLine tuple) {
                Matcher matcher = Pattern.compile("user (\\w+) by").matcher(tuple.message);
                matcher.find();
                return new Success(tuple.time, matcher.group(1));
            }
        }, Success.class);

        TStream<Breakin> breakins = DeterministicJoin.join(suspects, successes);
        breakins.sink(new TextFileSink<Breakin>(breakinsFile));

        return breakins; 
    }
}
