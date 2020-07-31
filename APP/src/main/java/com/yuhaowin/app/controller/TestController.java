package com.yuhaowin.app.controller;

import com.yuhaowin.core.annotation.Controller;
import com.yuhaowin.core.annotation.RequestMapping;
import com.yuhaowin.core.annotation.RequestParam;

import java.io.File;
import java.util.Objects;

@Controller
public class TestController {

    @RequestMapping(value = "/get-test",methodType = "GET")
    public String getTest(@RequestParam("name") String name,@RequestParam("age") String age) {
        return String.format("method=GET, name=%s, age=%s",name,age);
    }

    @RequestMapping(value = "/post-form-test",methodType = "POST")
    public String postFormTest(@RequestParam("name") String name,@RequestParam("age") String age) {
        return String.format("method=POST-FORM, name=%s, age=%s",name,age);
    }

    @RequestMapping(value = "/post-json-test",methodType = "POST")
    public String postJsonTest(@RequestParam("jsonStr") String jsonStr) {
        return String.format("method=POST-JSON, jsonStr=%s",jsonStr);
    }

    @RequestMapping(value = "/upload",methodType = "POST")
    public String upload(@RequestParam("uploadFile") File uploadFile) {
        String fileName;
        if (Objects.isNull(uploadFile)){
            fileName = "文件上传失败";
        }else {
            fileName = uploadFile.getName();
            System.out.println("上传的文件是：" + fileName);
        }
        return String.format("upload-file, fileName=%s",fileName);
    }
}
