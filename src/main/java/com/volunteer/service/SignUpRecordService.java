package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.SignUpRecord;
import com.volunteer.entity.vo.SignUpListVo;
import com.volunteer.entity.vo.SignUpRecordVo;
import com.volunteer.entity.vo.SignUpVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hefuren
 * @since 2021-11-07
 */
public interface SignUpRecordService extends IService<SignUpRecord> {
    /**
     * 报名
     * @param query
     * @return  0失败，1成功，2已报名，3未登录
     */
    int signUp(SignUpVo query);

    /**
     * 取消报名
     * @param query
     */
    void cancelRegistration(SignUpVo query);

    /**
     * 检查报名状态
     * @param query
     * @return
     */
    boolean checkSignUpState(SignUpVo query);

    /**
     * 获取报名列表
     * @param activityId
     * @return
     */
    List<SignUpListVo> getSignUpListByActivityId(Integer activityId);

    /**
     * 分页查询报名列表
     * @return
     */
    Page<SignUpRecordVo> getList(SignUpRecordVo signUpRecordVo);

    /**
     * 根据ID删除报名信息
     * @param id
     */
    void deleteRecordById(Integer id);

    /**
     * 检查是否报名
     */
    boolean checkForRegistration(Integer volunteerId,Integer activityId);

    /**
     * 根据志愿者和活动ID获取报名记录
     */
    SignUpRecord getRecord(Integer volunteerId,Integer activityId);
}
