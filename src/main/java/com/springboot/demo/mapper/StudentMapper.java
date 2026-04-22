package com.springboot.demo.mapper;

import com.springboot.demo.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 学生表Mapper接口
 */
@Mapper
public interface StudentMapper {

    /**
     * 根据ID查询学生
     *
     * @param id 学生ID
     * @return 学生信息
     */
    @Select("SELECT id, name, age FROM student WHERE id = #{id}")
    Student selectById(@Param("id") Long id);

    /**
     * 查询所有学生
     *
     * @return 学生列表
     */
    @Select("SELECT id, name, age FROM student")
    List<Student> selectAll();

    /**
     * 插入学生
     *
     * @param student 学生信息
     * @return 影响行数
     */
    @Insert("INSERT INTO student(name, age) VALUES(#{name}, #{age})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Student student);

    /**
     * 更新学生信息
     *
     * @param student 学生信息
     * @return 影响行数
     */
    @Update("UPDATE student SET name = #{name}, age = #{age} WHERE id = #{id}")
    int update(Student student);

    /**
     * 根据ID删除学生
     *
     * @param id 学生ID
     * @return 影响行数
     */
    @Delete("DELETE FROM student WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据姓名模糊查询
     *
     * @param name 姓名关键字
     * @return 学生列表
     */
    @Select("SELECT id, name, age FROM student WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Student> selectByNameLike(@Param("name") String name);

    /**
     * 统计学生总数
     *
     * @return 学生总数
     */
    @Select("SELECT COUNT(*) FROM student")
    Long count();
}