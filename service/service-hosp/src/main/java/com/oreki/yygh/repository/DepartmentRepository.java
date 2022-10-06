package com.oreki.yygh.repository;

import com.oreki.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);

    void deleteByHoscodeAndDepcode(String hoscode, String depcode);


    List<Department> getDepartmentsByHoscode(String hoscode);
}
