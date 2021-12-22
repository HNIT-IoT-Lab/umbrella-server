package com.volunteer.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.volunteer.entity.common.Result;
import com.volunteer.entity.common.ResultGenerator;
import com.volunteer.entity.common.SignUpStatus;
import com.volunteer.entity.vo.SignUpVo;
import com.volunteer.service.SignUpRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 报名记录控制器
 * </p>
 *
 * @author hefuren
 * @since 2021-11-07
 */

@Api(tags = "报名活动模块")
@RestController
@RequestMapping("/signUpRecord")
public class SignUpRecordController {

    @Autowired
    private SignUpRecordService signUpRecordService;

    /**
     * 志愿者报名参加志愿活动接口
     *
     * @return
     */
    @ApiOperation("报名接口")
    @PostMapping("/signUpActivity")
    public Result signUpActivity(@RequestBody SignUpVo query) {
        if (ObjectUtil.isNull(query)) {
            return ResultGenerator.getFailResult("参数有误");
        }
        try {
            int signUpStatus = signUpRecordService.signUp(query);
            if (signUpStatus == SignUpStatus.NOT_LOGIN) {
                return ResultGenerator.getFailResult("请重新登陆");
            } else if (signUpStatus == SignUpStatus.ALREADY_SIGNED_UP) {
                return ResultGenerator.getFailResult("您已经报名");
            } else if (signUpStatus == SignUpStatus.SIGN_UP_FAIL) {
                return ResultGenerator.getFailResult("报名失败，请稍后重试");
            }
            return ResultGenerator.getSuccessResult("报名成功");
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResultGenerator.getFailResult("系统错误，请联系管理员！");
        }
    }

    /**
     * 志愿者取消报名志愿活动接口
     *
     * @return
     */
    @ApiOperation("取消报名接口")
    @PostMapping("/cancelRegistration")
    public Result cancelRegistration(@RequestBody SignUpVo query) {
        try {
            signUpRecordService.cancelRegistration(query);
            return ResultGenerator.getSuccessResult("success");
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResultGenerator.getFailResult(exception.getMessage());
        }
    }
}

