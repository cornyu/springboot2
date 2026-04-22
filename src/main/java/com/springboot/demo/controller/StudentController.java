package com.springboot.demo.controller;

import com.springboot.demo.dto.Result;
import com.springboot.demo.entity.Student;
import com.springboot.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生控制器
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * 根据ID获取学生
     *
     * @param id 学生ID
     * @return 学生信息
     */
    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        Student student = studentService.getById(id);
        if (student != null) {
            return Result.success(student);
        } else {
            return Result.error("学生不存在");
        }
    }

    /**
     * 获取所有学生
     *
     * @return 学生列表
     */
    @GetMapping("/list")
    public Result<List<Student>> getAll() {
        List<Student> students = studentService.getAll();
        return Result.success(students);
    }

    /**
     * 添加学生
     *
     * @param student 学生信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody Student student) {
        boolean success = studentService.add(student);
        if (success) {
            return Result.success(student.getId()); // 返回生成的ID
        } else {
            return Result.error("添加失败");
        }
    }

    /**
     * 更新学生信息
     *
     * @param student 学生信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Student student) {
        boolean success = studentService.update(student);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新失败或学生不存在");
        }
    }

    /**
     * 删除学生
     *
     * @param id 学生ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = studentService.deleteById(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败或学生不存在");
        }
    }

    /**
     * 根据姓名搜索学生
     *
     * @param name 姓名关键字
     * @return 学生列表
     */
    @GetMapping("/search")
    public Result<List<Student>> searchByName(@RequestParam String name) {
        List<Student> students = studentService.searchByName(name);
        return Result.success(students);
    }

    /**
     * 获取学生总数
     *
     * @return 学生总数
     */
    @GetMapping("/count")
    public Result<Long> count() {
        Long count = studentService.getTotalCount();
        return Result.success(count);
    }
}