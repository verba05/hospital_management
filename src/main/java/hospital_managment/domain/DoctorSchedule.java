package hospital_managment.domain;

import java.time.*;
import java.util.*;

public class DoctorSchedule extends BaseEntity {

  private EnumMap<DayOfWeek,TimeRange> weekSchedule;
  private Vector<DoctorAvailabilityOverride> scheduleOverrides;

  public DoctorSchedule () {
    weekSchedule = null;
    scheduleOverrides = null;
  }

  public void setWeekSchedule (EnumMap<DayOfWeek,TimeRange> newVar) {
    weekSchedule = newVar;
  }

  public EnumMap<DayOfWeek,TimeRange> getWeekSchedule () {
    return weekSchedule;
  }

  public void setScheduleOverrides (Vector<DoctorAvailabilityOverride> newVar) {
    scheduleOverrides = newVar;
  }

  public Vector<DoctorAvailabilityOverride> getScheduleOverrides () {
    return scheduleOverrides;
  }

  public void addOverride(DoctorAvailabilityOverride ov) {
    if (ov == null) return;
    if (scheduleOverrides == null) scheduleOverrides = new Vector<>();
    scheduleOverrides.add(ov);
    incrementVersion();
  }

}
