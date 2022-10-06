package com.oreki.yygh.repository;

import com.oreki.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {

    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);


    List<Schedule> getSchedulesByHoscodeAndDepcode(String hoscode, String depcode);

    void deleteByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> getSchedulesByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date workDate);
}
