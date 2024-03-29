package com.oreki.yygh.repository;

import com.oreki.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    Hospital getHospitalByHoscode(String hoscode);

    Hospital getHospitalById(String id);


    List<Hospital> getHospitalsByHosnameLike(String hosname);
}
