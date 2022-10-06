package com.oreki.yygh.service;

import com.oreki.yygh.model.hosp.Department;
import com.oreki.yygh.vo.hosp.DepartmentQueryVo;
import com.oreki.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void saveDepartment(Map<String, Object> paramMap);

    Page<Department> getDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);

    void removeDepartment(String hoscode, String depcode);

    List<DepartmentVo> listDepartmentsTree(String hoscode);

    Department getByHoscodeAndDepcode(String hoscode, String depcode);
}
