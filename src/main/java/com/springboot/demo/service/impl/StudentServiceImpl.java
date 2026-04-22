package com.springboot.demo.service.impl;

import com.springboot.demo.entity.Student;
import com.springboot.demo.mapper.StudentMapper;
import com.springboot.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学生服务实现类
 */
@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public Student getById(Long id) {
        return studentMapper.selectById(id);
    }

    @Override
    public List<Student> getAll() {
        return studentMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean add(Student student) {
        return studentMapper.insert(student) > 0;
    }

    @Override
    @Transactional
    public boolean update(Student student) {
        return studentMapper.update(student) > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        return studentMapper.deleteById(id) > 0;
    }

    @Override
    public List<Student> searchByName(String name) {
        return studentMapper.selectByNameLike(name);
    }

    @Override
    public Long getTotalCount() {
        return studentMapper.count();
    }
}