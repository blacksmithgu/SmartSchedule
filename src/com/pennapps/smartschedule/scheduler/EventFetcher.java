package com.pennapps.smartschedule.scheduler;

import org.joda.time.DateTime;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;

public class EventFetcher {
	public static String TEST_ACCOUNT = "blacksmithgu@gmail.com";
	public static String TEST_TYPE = "com.google";
	
	private long calendarID;
	private ContentResolver resolve;
	
	public static final String[] CALENDAR_PROJECTION = new String[] {
		Calendars._ID,
		Calendars.ACCOUNT_NAME,
		Calendars.ACCOUNT_TYPE,
		Calendars.OWNER_ACCOUNT
	};
	
	public static final String[] EVENT_PROJECTION = new String[] {
		Events._ID,
		Events.TITLE,
		Events.EVENT_LOCATION,
		Events.DESCRIPTION,
		Events.DTSTART,
		Events.DTEND,
		Events.DURATION,
		Events.ALL_DAY,
		Events.AVAILABILITY
	};
	
	public EventFetcher(ContentResolver resolver) {
		calendarID = -1L;
		resolve = resolver;
	}
	
	public long getCalendarID() {
		if(calendarID != -1) return calendarID;
		
		Cursor cur = resolve.query(Calendars.CONTENT_URI, CALENDAR_PROJECTION,
				"", null, null);
		
		if(cur.moveToFirst()) {
			calendarID = cur.getLong(0);
			return calendarID;
		}
		
		return -1L; // No ID
	}
	
	public SchedulingCalendar getCalendar(DateTime startTime) {
		if(calendarID == -1) return null; // What are u doin
		
		String query = "((" + Events.CALENDAR_ID + " = ?) AND (" + Events.DTSTART + " > " + startTime.getMillis() + "))";
		
		Cursor cur = resolve.query(Events.CONTENT_URI, EVENT_PROJECTION, query,
				new String[] { "" + calendarID }, null);
		
		SchedulingCalendar cal = new SchedulingCalendar();
		while(cur.moveToNext()) {
			DateTime start = new DateTime(cur.getLong(3));
			DateTime end = new DateTime(cur.getLong(4));
			
			Event evnt = new Event(cur.getLong(0), cur.getString(1), start, end);
			
			cal.addEvent(evnt);
		}
		
		return cal;
	}
}
