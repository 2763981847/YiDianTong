package com.oreki.yygh.common.exception;

import com.oreki.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result error(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }
    @ExceptionHandler(YyghException.class)
    public Result error(YyghException e){
        e.printStackTrace();
        return Result.fail();
    }

}
