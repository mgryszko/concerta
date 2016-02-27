package concerta.notification;

import concerta.core.Event;
import concerta.core.EventType;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import static concerta.core.EventType.*;

public class EventMessageFormatter {
    private static final int SECONDS_PER_MINUTE = 60;
    private static final String H_MM = "%d:%02d";

    private Map<EventType, String> eventMessages = new EnumMap<>(EventType.class);
    public EventMessageFormatter() {
        fillMessages();
    }

    private void fillMessages() {
        eventMessages.put(STARTING, "Starting time slice - " + H_MM);
        eventMessages.put(IN_PROGRESS,  H_MM + " left");
        eventMessages.put(WILL_ELAPSE_SOON, H_MM + " to go");
        eventMessages.put(ELAPSED, "Time slice elapsed - " + H_MM);
    }

    public String message(Event event) {
        return formatDuration(eventMessages.get(event.getType()), event.getDuration());
    }

    private String formatDuration(String format, Duration duration) {
        long totalSeconds = Math.abs(duration.getSeconds());
        long minutes = totalSeconds / SECONDS_PER_MINUTE;
        long seconds = totalSeconds % SECONDS_PER_MINUTE;
        return String.format(format, minutes, seconds);
    }
}
