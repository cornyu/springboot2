package com.springboot.demo.service;

import com.springboot.demo.entity.Student;
import java.util.List;

/**
 * 学生服务接口
 */
public interface StudentService {

    /**
     * 根据ID获取学生
     *
     * @param id 学生ID
     * @return 学生信息
     */
    Student getById(Long id);

    /**
     * 获取所有学生
     *
     * @return 学生列表
     */
    List<Student> getAll();

    /**
     * 添加学生
     *
     * @param student 学生信息
     * @return 是否成功
     */
    boolean add(Student student);

    /**
     * 更新学生信息
     *
     * @param student 学生信息
     * @return 是否成功
     */
    boolean update(Student student);

    /**
     * 删除学生
     *
     * @param id 学生ID
     * @return 是否成功
     */
    boolean deleteById(Long id);

    /**
     * 根据姓名模糊查询
     *
     * @param name 姓名关键字
     * @return 学生列表
     */
    List<Student> searchByName(String name);

    /**
     * 获取学生总数
     *
     * @return 学生总数
     */
    Long getTotalCount();
}