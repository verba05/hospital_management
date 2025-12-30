package hospital_managment.domain;

import java.time.*;

public class TimeRange {

  private LocalTime startTime;
  private LocalTime endTime;

  public TimeRange () { }

  public TimeRange(LocalTime start, LocalTime end) {
    if (!isValid(start, end)) {
      throw new IllegalArgumentException("Invalid time range: start must be non-null and <= end");
    }
    this.startTime = start;
    this.endTime = end;
  }

  public void setStartTime (LocalTime newVar) {
    startTime = newVar;
  }

  public LocalTime getStartTime () {
    return startTime;
  }

  public void setEndTime (LocalTime newVar) {
    endTime = newVar;
  }

  public LocalTime getEndTime () {
    return endTime;
  }

  public static boolean isValid(LocalTime start, LocalTime end) {
    if (start == null || end == null) return false;
    return !start.isAfter(end);
  }

  public LocalTime getStart() {
    return startTime;
  }

  public LocalTime getEnd() {
    return endTime;
  }

  public boolean contains(LocalTime time) {
    if (time == null) return false;
    return !time.isBefore(startTime) && time.isBefore(endTime);
  }

  public boolean overlaps(TimeRange other) {
    if (other == null) return false;
    return !this.endTime.isBefore(other.startTime) && !other.endTime.isBefore(this.startTime);
  }

  @Override
  public String toString() {
    return String.format("TimeRange[%s-%s]", startTime, endTime);
  }

}
