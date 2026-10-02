package com.atguigu.mapper;

import com.atguigu.domain.Appointment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AppointmentMapper extends BaseMapper<Appointment> {

}