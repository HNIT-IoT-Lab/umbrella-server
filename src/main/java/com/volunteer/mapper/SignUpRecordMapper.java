package com.volunteer.mapper;

import com.volunteer.entity.SignUpRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hefuren
 * @since 2021-11-07
 */
public interface SignUpRecordMapper extends BaseMapper<SignUpRecord> {
         SignUpRecord selectByVolunteerActivityID(Integer id);
}
