package com.atguigu.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author pk
 * @Date 2026-10-01 19:25
 * @Descriotion 工具类
 */
@Slf4j
@Component
public class CalculatorTools {

    @Tool(name = "sum", value = "计算两个数的和")
    public double sum(@P(name = "a", value = "参数a", required = true) double a,
                      @P(name = "b", value = "参数b", required = true) double b) {
        log.info("调用加法运算");
        return a + b;
    }

    @Tool(name = "squareRoot", value = "计算一个数的开平方")
    public double squareRoot(@P(name = "a", value = "参数a", required = true) double a) {
        log.info("调用平方根运算");
        return Math.sqrt(a);
    }
}
