//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.2.0 info.picocli:picocli-codegen:4.2.0

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Parameters;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.lang.System.exit;
import static java.lang.System.out;

@Command(name = "tz", mixinStandardHelpOptions = true, version = "tz 0.1",
        description = "tz made with jbang")
class tz implements Callable<Integer> {
    // tag::parameters[]
    @Parameters(index = "0", description = "time as hh:mm") 
    private LocalTime time; // <.>

    @Parameters(converter = ZoneIdConverter.class, // <.>
                description = "List of timezone ids to convert time stamp to")
    List<ZoneId> zones;
    // end::parameters[]

    public static void main(String... args) {
        int exitCode = new CommandLine(new tz()).execute(args);
        exit(exitCode);
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...

        var t = LocalDateTime.now().with(time);

        // tag::defaultzones[]
        if(zones.isEmpty()) {
            zones = Arrays.asList("America/Los_Angeles",
                    "America/Detroit",
                    "Europe/London",
                    "Europe/Zurich",
                    "Asia/Kolkata",
                    "Australia/Brisbane").stream()
                    .map(tz -> ZoneId.of(tz, ZoneId.SHORT_IDS))
                    .collect(Collectors.toList()); // <.>
        }
        // end::defaultzones[]

        // tag::conversion[]
        String result = zones.stream().map(zone -> {
            var from = t.atZone(ZoneId.systemDefault());
            var to = from.withZoneSameInstant(zone);
            return String.format("%s %s", to.toLocalTime(), zone.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }).collect(Collectors.joining(" / "));
        // end::conversion[]

        out.println(result);
        
        return 0;
    }

}

// tag::zoneconverter[]
class ZoneIdConverter implements ITypeConverter<ZoneId> {
    public ZoneId convert(String value) throws Exception {
        return ZoneId.of(value, ZoneId.SHORT_IDS); // <.>
    }
}
// end::zoneconverter[]
