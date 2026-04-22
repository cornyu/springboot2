package com.springboot.demo.exception;

import com.springboot.demo.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理数据库异常
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<?> handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        logger.error("数据库操作异常，URL: {}, 错误: {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("数据库操作失败");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        logger.error("空指针异常，URL: {}, 错误: {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("系统内部错误");
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logger.error("运行时异常，URL: {}, 错误: {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("系统运行时异常");
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        logger.error("系统异常，URL: {}, 错误: {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("系统异常");
    }
}