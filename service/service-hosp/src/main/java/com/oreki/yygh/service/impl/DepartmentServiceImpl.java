package com.oreki.yygh.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.oreki.yygh.model.hosp.Department;
import com.oreki.yygh.repository.DepartmentRepository;
import com.oreki.yygh.service.DepartmentService;
import com.oreki.yygh.vo.hosp.DepartmentQueryVo;
import com.oreki.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Resource
    private DepartmentRepository departmentRepository;

    /**
     * 保存科室信息
     *
     * @param paramMap 参数表
     */
    @Override
    public void saveDepartment(Map<String, Object> paramMap) {
        //将map中的参数转为实体对象
        String jsonString = JSONObject.toJSONString(paramMap);
        Department department = JSON.parseObject(jsonString, Department.class);
        //判断数据库中是否含有该数据
        Department dep = getByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());
        //存在数据则进行修改，不存在则进行新增
        if (dep == null) {
            department.setCreateTime(new Date());
        } else {
            department.setId(dep.getId());
            department.setCreateTime(dep.getCreateTime());
        }
        department.setUpdateTime(new Date());
        department.setIsDeleted(0);
        departmentRepository.save(department);
    }

    /**
     * 分页查询科室信息
     *
     * @param page              当前页
     * @param limit             每页数据量
     * @param departmentQueryVo 查询条件
     * @return 查询到的分页对象
     */
    @Override
    public Page<Department> getDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //创建pageable对象
        Pageable pageable = PageRequest.of(page - 1, limit);
        //创建example对象
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        Example<Department> example = Example.of(department, exampleMatcher);
        //分页查询
        Page<Department> pageModel = departmentRepository.findAll(example, pageable);
        return pageModel;
    }

    /**
     * 删除科室接口
     *
     * @param hoscode 医院编号
     * @param depcode 科室编号
     */
    @Override
    public void removeDepartment(String hoscode, String depcode) {
        departmentRepository.deleteByHoscodeAndDepcode(hoscode, depcode);
    }

    /**
     * 根据医院编号查询科室列表
     *
     * @param hoscode 医院编号
     * @return 科室列表
     */
    @Override
    public List<DepartmentVo> listDepartmentsTree(String hoscode) {
        //创建结果集合
        List<DepartmentVo> result;
        //根据医院编号查到所有科室列表
        List<Department> list = departmentRepository.getDepartmentsByHoscode(hoscode);
        //根据一级科室分组
        Map<String, List<Department>> bigcodeDepsMap = list.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //封装结果列表
        result = bigcodeDepsMap.entrySet().parallelStream()
                .map(entry -> {
                    //封装一级科室
                    String bigCode = entry.getKey();
                    List<Department> departments = entry.getValue();
                    DepartmentVo departmentVo = new DepartmentVo();
                    departmentVo.setDepcode(bigCode);
                    departmentVo.setDepname(departments.get(0).getBigname());
                    //封装二级科室
                    List<DepartmentVo> children = new LinkedList<>();
                    departments.forEach(department -> {
                        DepartmentVo departmentVo1 = new DepartmentVo();
                        departmentVo1.setDepcode(department.getDepcode());
                        departmentVo1.setDepname(department.getDepname());
                        children.add(departmentVo1);
                    });
                    departmentVo.setChildren(children);
                    return departmentVo;
                }).collect(Collectors.toList());
        return result;
    }

    /**
     * 根据医院编码和科室编码查询科室
     *
     * @param hoscode 医院编码
     * @param depcode 科室编码
     * @return 科室
     */
    @Override
    public Department getByHoscodeAndDepcode(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}
