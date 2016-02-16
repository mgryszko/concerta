package concerta.core;

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import static concerta.core.EventType.*;

public class TimeSliceTest {
    @Test
    public void elapse_time_slice_start_end_events() {
        Observable<Event> timeSlice = new TimeSlice(scheduler).start(duration);
        timeSlice.subscribe(eventObserver);

        eventsObservedAfter(duration, new Event(STARTING), new Event(ELAPSED));
        eventObserver.assertCompleted();
    }

    @Test
    public void in_progress_events_of_time_slice() {
        int inProgressPeriod = 2;
        Observable<Event> timeSlice = new TimeSlice(scheduler).inProgressEvery(inProgressPeriod).start(duration);
        timeSlice.subscribe(eventObserver);

        eventsObservedAfter(2 * inProgressPeriod, new Event(STARTING), new Event(IN_PROGRESS), new Event(IN_PROGRESS));
    }

    @Test
    public void will_elapse_soon_events_before_time_slice_end() {
        Observable<Event> timeSlice = new TimeSlice(scheduler).elapsesIn(5, 3, 1).start(duration);
        timeSlice.subscribe(eventObserver);

        eventsObservedAfter(duration, new Event(STARTING), new Event(WILL_ELAPSE_SOON), new Event(WILL_ELAPSE_SOON), new Event(WILL_ELAPSE_SOON), new Event(ELAPSED));
    }

    private int duration = 10;
    private TestScheduler scheduler = Schedulers.test();
    private TestSubscriber<Event> eventObserver = new TestSubscriber<>();

    private void eventsObservedAfter(int time, Event... expectedEvents) {
        scheduler.advanceTimeBy(time, TimeSlice.UNIT);
        eventObserver.assertValues(expectedEvents);
    }
}
