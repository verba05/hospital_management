package hospital_managment.domain;

import java.time.*;

public class DoctorAvailabilityOverride extends BaseEntity {

  private LocalDate startDate;
  private LocalDate endDate;
  private boolean available;
  private TimeRange timeRange;
  private String reason;

  public DoctorAvailabilityOverride() {
}

  public void setStartDate (LocalDate newVar) {
    startDate = newVar;
  }

  public LocalDate getStartDate () {
    return startDate;
  }

  public void setEndDate (LocalDate newVar) {
    endDate = newVar;
  }

  public LocalDate getEndDate () {
    return endDate;
  }

  public void setAvailable (boolean newVar) {
    available = newVar;
  }

  public boolean getAvailable () {
    return available;
  }

  public void setTimeRanges (TimeRange newVar) {
    timeRange = newVar;
  }

  public TimeRange getTimeRange () {
    return timeRange;
  }

  public void setReason (String newVar) {
    reason = newVar;
  }

  public String getReason () {
    return reason;
  }

  public boolean isInRange(LocalDate date) {
    if (date == null) return false;
    if (startDate != null && date.isBefore(startDate)) return false;
    if (endDate != null && date.isAfter(endDate)) return false;
    return true;
  }


  @Override
  public String toString() {
    return String.format("Override[%s to %s available=%s reason=%s timeRanges=%s]",
      startDate, endDate, available, reason, timeRange);
  }

}
