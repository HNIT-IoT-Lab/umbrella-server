package com.volunteer.controller;


import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.entity.VolunteerActivity;
import com.volunteer.entity.common.Result;
import com.volunteer.entity.common.ResultGenerator;
import com.volunteer.entity.vo.ActivityListVo;
import com.volunteer.entity.vo.AuditeActivityVo;
import com.volunteer.entity.vo.Ids;
import com.volunteer.entity.vo.SignUpListVo;
import com.volunteer.service.SignUpRecordService;
import com.volunteer.service.VolunteerActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hefuren
 * @since 2021-11-07
 */
@Api(tags = "志愿者活动模块")
@RestController
@RequestMapping("/volunteerActivity")
@Slf4j
public class ActivityController {

    @Autowired
    private VolunteerActivityService volunteerActivityService;

    @Autowired
    private SignUpRecordService signUpRecordService;

    /**
     * 创建志愿者活动
     * @param volunteerActivity
     * @return
     */
    @ApiOperation(value = "活动创建接口")
    @PostMapping("/createActivity")
    public Result createActivity(@RequestBody VolunteerActivity volunteerActivity){
        try {
            int result = volunteerActivityService.createActivity( volunteerActivity);
            if (result == 1){
                return ResultGenerator.getSuccessResult("活动添加成功");
            }else {
                return ResultGenerator.getFailResult("请检查活动");
            }
        } catch (Exception exception) {
            log.error("系统异常：{}",exception.getMessage());
            return ResultGenerator.getSuccessResult(exception.getMessage());
        }

    }

    /**
     * 删除志愿者活动
     * @param id
     * @return
     */
    @ApiOperation(value = "活动删除接口")
    @PostMapping("/deleteActivity")
    public Result deleteActivity(@RequestBody Integer id){
        try {
            volunteerActivityService.deleteActivity(id);
            return ResultGenerator.getSuccessResult();
        } catch (Exception exception) {
            log.error("系统异常：{}",exception.getMessage());
            return ResultGenerator.getFailResult("系统异常");
        }
    }

    /**
     * 批量删除志愿者活动
     * @param ids
     * @return
     */
    @PostMapping("/deleteListActivity")
    @ApiOperation(value = "活动批量删除接口")
    public Result deleteListActivity(@RequestBody Ids ids){
        System.out.println(ids);
        try {
            volunteerActivityService.deleteListActivity(ids.getIds());
            return ResultGenerator.getSuccessResult();
        } catch (Exception exception) {
            log.error("系统异常：{}",exception.getMessage());
            return ResultGenerator.getFailResult("系统异常");
        }
    }

    /**
     * 更新志愿者活动状态
     * @param auditeActivity
     * @return
     */
    @ApiOperation(value = "活动状态更新接口")
    @PostMapping("/updateActivityStatus")
    public  Result updateActivityStatus(AuditeActivityVo auditeActivity) {
        try {
            VolunteerActivity volunteerActivity = volunteerActivityService.updateActivityStatus(auditeActivity);
            return ResultGenerator.getSuccessResult(volunteerActivity);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultGenerator.getFailResult(e.getMessage());
        }
    }

    /**
     * 查询单个志愿者活动
     * @param id
     * @return
     */
    @ApiOperation(value = "活动查询接口")
    @GetMapping("/getActivityInfo")
    public Result selectOneActivity(Integer id){
        try {
            VolunteerActivity volunteerActivity=volunteerActivityService.selectOne(id);
            return ResultGenerator.getSuccessResult(volunteerActivity);
        } catch (Exception exception) {
            exception.printStackTrace();
            log.info("系统异常：{}",exception.getMessage());
            return ResultGenerator.getFailResult(exception.getMessage());
        }
    }

    /**
     * 更新活动接口
     * @param volunteerActivity
     */
    @PostMapping("/updateActivity")
    @ApiOperation(value = "活动更新接口")
    public Result updateActivity(@RequestBody VolunteerActivity volunteerActivity){
        try {
            int date= volunteerActivityService.updateActivity(volunteerActivity);
            return ResultGenerator.getSuccessResult(date);
        } catch (Exception exception) {
            log.error("系统异常：{}",exception.getMessage());
            return ResultGenerator.getFailResult(exception.getMessage());
        }
    }

    /**
     * 分页查询活动接口
     * @param volunteerActivity
     */
    @ApiOperation(value = "活动分页查询接口")
    @GetMapping("/selectListActivity")
    public Result selectListActivity(VolunteerActivity volunteerActivity){
        try {
            IPage<VolunteerActivity> data = volunteerActivityService.selectListActivity(volunteerActivity);
            return ResultGenerator.getSuccessResult(data);
        }catch (Exception exception){
            log.error("系统异常：{}",exception.getMessage());
            return ResultGenerator.getFailResult(exception.getMessage());
        }
    }

    @ApiOperation(value = "活动状态分页查询接口")
    @PostMapping("/findListByStutas")
    public Result findListByStutas(@RequestBody Map<String,String> map){
        JSONObject jsonObject =new JSONObject(map);
        String status=jsonObject.getStr("status");
        Integer pageNo= jsonObject.getInt("pageNo");
        Integer pageSize= jsonObject.getInt("pageSize");
        try {
            ActivityListVo listByStutas = volunteerActivityService.findListByStutas(status, pageNo, pageSize);
            return ResultGenerator.getSuccessResult(listByStutas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultGenerator.getFailResult(e.getMessage());
        }
    }

    @ApiOperation(value = "获取最近报名的")
    @GetMapping("/getSignUpList")
    public Result getSignUpList(Integer activityId) {
        try {
            List<SignUpListVo> signUpList = signUpRecordService.getSignUpList(activityId);
            return ResultGenerator.getSuccessResult(signUpList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultGenerator.getFailResult("系统异常，请联系管理员");
        }
    }

    /**
     * 更新活动状态的接口
     *
     * @return
     */
    @PostMapping("/updateStatus")
    public Result updateActivityStatus() {
        try {
            volunteerActivityService.updateActivityStatus();
        }catch (Exception e) {
            log.error(e.getMessage());
            return ResultGenerator.getFailResult("服务器异常: " + e.getMessage(), HttpStatus.HTTP_INTERNAL_ERROR);
        }
        return ResultGenerator.getSuccessResult();
    }
}

