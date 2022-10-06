package com.oreki.yygh.service;

import com.oreki.yygh.model.hosp.Schedule;
import com.oreki.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void saveSchedule(Map<String, Object> paramMap);

    Page<Schedule> getSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void removeSchedule(String hoscode, String hosScheduleId);

    Map<String, Object> listSchedules(int page, int limit, String hoscode, String depcode);

    List<Schedule> listScheduleDetails(String hoscode, String depcode, Date workDate);
}
