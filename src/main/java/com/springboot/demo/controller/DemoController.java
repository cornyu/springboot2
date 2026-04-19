package com.springboot.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DemoController {

    @RequestMapping(value = "/hello")
    @ResponseBody
    public String initMenu(@RequestParam(value="name",required=false)String name, HttpServletRequest request){
        System.out.println("name: "+name);
        return "hello " + name;
    }


}
