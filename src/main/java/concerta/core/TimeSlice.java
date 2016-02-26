package concerta.core;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static concerta.core.EventType.*;
import static java.lang.Math.toIntExact;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static rx.Observable.interval;
import static rx.Observable.just;

public class TimeSlice {
    private Scheduler scheduler = Schedulers.immediate();
    private Duration inProgressPeriod = Duration.ZERO;
    private Collection<Integer> elapsesIn = emptyList();

    public TimeSlice() {
    }

    public TimeSlice(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public TimeSlice inProgressEvery(Duration period) {
        inProgressPeriod = period;
        return this;
    }

    public TimeSlice elapsesIn(List<Integer> times) {
        elapsesIn = new ArrayList<>(times);
        return this;
    }

    public Observable<Event> start(Duration duration) {
        return everyOneUnitUpTo(duration).concatMap(new ToEvent(duration));
    }

    private Observable<Duration> everyOneUnitUpTo(Duration duration) {
        return interval(0, 1, TimeUnit.SECONDS, scheduler)
            .map(t -> Duration.of(t, SECONDS))
            .take(toIntExact(duration.getSeconds() + 1));
    }

    private final class ToEvent implements Func1<Duration, Observable<Event>>  {
        private final Duration duration;

        private ToEvent(Duration duration) {
            this.duration = duration;
        }

        @Override
        public Observable<Event> call(Duration t) {
            if (t.equals(Duration.ZERO)) {
                return just(new Event(STARTING, duration));
            }
            if (t.equals(duration)) {
                return just(new Event(ELAPSED, duration));
            }
            if (willElapseSoon(t)) {
                return just(new Event(WILL_ELAPSE_SOON, timeToGo(t)));
            }
            if (inProgress(t)) {
                return just(new Event(IN_PROGRESS, t));
            }
            return just(new Event(TICK, t));
        }

        private boolean willElapseSoon(Duration t) {
            return elapsesIn.stream().anyMatch(e -> Duration.of(e, MINUTES).equals(timeToGo(t)));
        }

        private Duration timeToGo(Duration t) {
            return duration.minus(t);
        }

        private boolean inProgress(Duration t) {
            if (inProgressPeriod.equals(Duration.ZERO)) {
                return false;
            }
            return isMultipleOf(t, inProgressPeriod);
        }

        private boolean isMultipleOf(Duration d1, Duration d2) {
            return d1.getSeconds() % d2.getSeconds() == 0;
        }
    }
}
