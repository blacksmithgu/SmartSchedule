package com.pennapps.smartschedule.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

public class RollingScheduler {
	
	public List<Interval> getScheduleIntervals(SchedulingCalendar calendar, Day start, ScheduledEvent nextEvent, SchedulingSettings settings) {
		// Look for first good opportunity to put problem.
		// Add splitting at a later time.
		
		List<Interval> times = new ArrayList<Interval>();
		
		Day deadline = new Day(nextEvent.getDeadline());
		Day current = start;
		while(current.compareTo(deadline) <= 0) {
			List<Interval> intervals = calendar.getAvailableIntervals(current); // TODO: Be the events deadline.
			for(Interval i : intervals) {
				if(i.toPeriod().getMillis() >= nextEvent.getDuration().getMillis())
				{
					DateTime evnt_start = i.getStart();
					DateTime evnt_stop = evnt_start.plusMillis((int) (i.toPeriod().getMillis() - nextEvent.getDeadline().getMillis()));
				
					times.add(new Interval(evnt_start, evnt_stop));
				}
			}
			
			current = current.next();
		}
		
		return times;
	}
	
	public Event scheduleFirst(SchedulingCalendar calendar, Day start, ScheduledEvent event, SchedulingSettings settings) {
		List<Interval> intervals = getScheduleIntervals(calendar, start, event, settings);
		DateTime realStart = intervals.get(0).getStart();
		
		Event realEvent = new Event(-1L, event.getName(), realStart, realStart.plus(event.getDuration()));
		return realEvent;
	}
	
	public Event scheduleFirst(SchedulingCalendar calendar, ScheduledEvent event, SchedulingSettings settings) {
		return scheduleFirst(calendar, Day.today(), event, settings);
	}
	
	public List<ScheduledEvent> split(ScheduledEvent event, int splits, Period minimumTime) {
		List<ScheduledEvent> events = new ArrayList<ScheduledEvent>();
		long millisPerSegment = event.getDuration().getMillis() / splits;
		Period segmentLength = new Period(Math.max(minimumTime.getMillis(), millisPerSegment));
		
		Period remaining = event.getDuration();
		while(remaining.getMillis() > 0) {
			ScheduledEvent evnt = new ScheduledEvent(event.getName(), event.getDeadline(), segmentLength);
			remaining = remaining.minus(segmentLength);
			
			events.add(evnt);
		}
		
		return events;
	}
	
	public List<ScheduledEvent> split(ScheduledEvent event, int splits) {
		return split(event, splits, Period.millis(0));
	}
}