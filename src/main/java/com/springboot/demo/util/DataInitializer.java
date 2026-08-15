package com.springboot.demo.util;

import com.springboot.demo.entity.Student;
import com.springboot.demo.service.AppUserService;
import com.springboot.demo.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化类，应用启动时插入测试数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private StudentService studentService;

    @Autowired
    private AppUserService appUserService;

    @Override
    public void run(String... args) throws Exception {
        // 初始化默认管理员账号
        if (appUserService.findByUsername("admin") == null) {
            appUserService.createUser("admin", "123456");
            logger.info("已创建默认管理员账号: admin / 123456");
        }

        // 清空表（测试环境使用）
        // studentService.deleteById(1L); // 示例：删除ID为1的记录

        // 初始化测试数据
        if (studentService.getTotalCount() == 0) {
            logger.info("开始初始化测试数据...");

            Student student1 = new Student();
            student1.setName("张三");
            student1.setAge(20.0);
            studentService.add(student1);

            Student student2 = new Student();
            student2.setName("李四");
            student2.setAge(22.0);
            studentService.add(student2);

            Student student3 = new Student();
            student3.setName("王五");
            student3.setAge(19.5);
            studentService.add(student3);

            Student student4 = new Student();
            student4.setName("赵六");
            student4.setAge(21.0);
            studentService.add(student4);

            logger.info("测试数据初始化完成，共添加 {} 条记录", studentService.getTotalCount());
        } else {
            logger.info("数据库中已有 {} 条学生记录，跳过数据初始化", studentService.getTotalCount());
        }
    }
}