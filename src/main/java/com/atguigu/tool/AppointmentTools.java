package com.atguigu.tool;

import com.atguigu.domain.Appointment;
import com.atguigu.service.AppointmentService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppointmentTools {

    private final AppointmentService appointmentService;

    public AppointmentTools(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Tool(name = "预约挂号", value = "根据参数，先执行工具方法queryDepartment查询是否可预约，并直接给用户回答是否可预约，并让用户确认所有预约信息，用户确认后再进行预约")
    public String bookAppointment(Appointment appointment) {
        log.info("预约挂号");
        // 查询数据库中是否包含预约信息
        Appointment appointmentDB = appointmentService.getOne(appointment);
        if (appointmentDB == null) {
            appointment.setId(null);
            if (appointmentService.save(appointment)) {
                return "预约成功，并返回预约详情";
            } else {
                return "预约失败";
            }
        }
        return "您在这一个科室已有预约";
    }

    @Tool(name = "取消预约", value = "根据参数，查询预约是否存在，如果存在则删除预约记录并返回取消预约成功，否则返回取消预约失败")
    public String cancelAppointment(Appointment appointment) {
        log.info("取消预约");
        Appointment appointmentDB = appointmentService.getOne(appointment);
        if (appointmentDB != null) {
            // 删除预约记录
            if (appointmentService.removeById(appointmentDB.getId())) {
                return "取消预约成功";
            } else {
                return "取消预约失败";
            }
        }
        return "你没有该科室和医生在当前时间的预约，请核查信息";
    }

    @Tool(name = "查询是否有号源", value = "根据科室、日期、时间和医生查询是否有号源，并返回给用户")
    public boolean queryDepartment(@P(name = "name", value = "科室名称") String name,
                                   @P(name = "date", value = "日期") String date,
                                   @P(name = "time", value = "时间") String time,
                                   @P(name = "doctorName", value = "医生名称") String doctorName) {
        log.info("查询是否有号源");
        log.info("科室名称：{}", name);
        log.info("日期：{}", date);
        log.info("时间：{}", time);
        log.info("医生名称：{}", doctorName);

        // 维护医生的排班信息：
        // 如果没有指定医生名字，则根据其他条件查询是否有可以预约的医生（有返回true，否则返回false）
        // 如果指定了医生名字，则判断医生是否排班（没有排班返回false）
        // 如果有排班，则判断医生排班时间段是否已约满（约满返回false，有空闲时间返回true）
        return true;
    }
}
