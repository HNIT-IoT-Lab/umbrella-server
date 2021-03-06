package com.volunteer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencentcloudapi.as.v20180419.models.Activity;
import com.volunteer.entity.SignUpRecord;
import com.volunteer.entity.Volunteer;
import com.volunteer.entity.VolunteerActivity;
import com.volunteer.entity.common.ActivityStatus;
import com.volunteer.entity.vo.ActivityListItemVo;
import com.volunteer.entity.vo.ActivityListVo;
import com.volunteer.entity.vo.ActivityVo;
import com.volunteer.entity.vo.AuditeActivityVo;
import com.volunteer.mapper.VolunteerActivityMapper;
import com.volunteer.service.SignUpRecordService;
import com.volunteer.service.VolunteerActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.util.AES;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author hefuren
 * @since 2021-11-07
 */
@Service
@Slf4j
public class VolunteerActivityServiceImpl extends ServiceImpl<VolunteerActivityMapper, VolunteerActivity> implements VolunteerActivityService {

    @Autowired
    private SignUpRecordService signUpRecordService;

    @Autowired
    private VolunteerActivityService volunteerActivityService;

    /**
     * 创建一个志愿者活动
     *
     * @param volunteerActivity
     * @return 1为成功创建
     */

    @Override
    public int createActivity(VolunteerActivity volunteerActivity) throws Exception {
        System.out.println(volunteerActivity);
        LocalDateTime now = LocalDateTime.now();
        //如果志愿者活动不为空
        if (volunteerActivity != null) {
            //判断活动必须项是否为空
            if (StrUtil.isEmpty(volunteerActivity.getActivityName())) {
                throw new RuntimeException("志愿者活动名称不能为空");
            }
            if (StrUtil.isEmpty(volunteerActivity.getActivityAddress())) {
                throw new RuntimeException("志愿者地址不能为空");
            }
            if (ObjectUtil.isNull(volunteerActivity.getNumberOfNeed())) {
                throw new RuntimeException("志愿者所需人数不能为空");
            }
            if (volunteerActivity.getEndTime() == null) {
                throw new RuntimeException("志愿者活动结束时间不能为空");
            }
            if (volunteerActivity.getStartTime() == null) {
                throw new RuntimeException("志愿者活动开始时间不能为空");
            }
            if (volunteerActivity.getEndTime().isBefore(volunteerActivity.getStartTime())) {
                throw new RuntimeException("志愿者活动结束时间不能早于开始时间");
            }
            if (volunteerActivity.getStartTime().isBefore(now)) {
                throw new RuntimeException("志愿者活动开始时间不能早于当前时间");
            }
//            if (volunteerActivity.getPredictDuration() < 0 || volunteerActivity.getPredictDuration() > 12) {
//                throw new RuntimeException("单次志愿活动最多可累计十二小时，且不能小于0小时");
//            }

            /**
             * 设置活动状态和奖励积分
             */
            initStatus(volunteerActivity,now);
            volunteerActivity.setCreateBy("admin");
            volunteerActivity.setCreateAt(now);
            baseMapper.insert(volunteerActivity);
            return 1;
        } else {
            return -1;
        }

    }

    /**
     * 删除一个志愿者活动
     *
     * @param id
     * @return
     */
    @Override
    public int deleteActivity(Integer id) {
        if (id == null) {
            throw new RuntimeException("id为空");
        }
        VolunteerActivity volunteerActivity = baseMapper.selectById(id);
        //根据id查询志愿者活动是否存在，存在才做逻辑删除 将delete设置为 1
        if (ObjectUtil.isNotNull(volunteerActivity)) {
            volunteerActivity.setDeleted(1);
            return baseMapper.updateById(volunteerActivity);
        }
        return 0;
    }

    /**
     * 批量删除志愿者活动
     *
     * @param ids
     */
    @Override
    public void deleteListActivity(Integer[] ids) {
        for (int i = 0; i < ids.length; i++) {
            deleteActivity(ids[i]);
        }
    }

    /**
     * 审核志愿者活动接口
     *
     * @param auditeActivity
     * @return
     */
    public Boolean isAuditedActivity(AuditeActivityVo auditeActivity) {
        //判断是否传入参数
        if (ObjectUtil.isNull(auditeActivity)) {
            throw new RuntimeException("请输入活动id和真实志愿活动时长");
        }
        //判断传入id是否和法
        if (ObjectUtil.isNull(auditeActivity.getId()) || auditeActivity.getId() < 0) {
            throw new RuntimeException("id有误，请检查后重试");
        }
        //真实志愿时长不能为空
        //判断志愿时长是否正常
        if (ObjectUtil.isNull(auditeActivity.getActualDuration()) || auditeActivity.getActualDuration() < 0) {
            throw new RuntimeException("志愿时长有误，请检查后重试");
        }
        //判断志愿活动是否存在
        VolunteerActivity volunteerActivity = baseMapper.selectById(auditeActivity.getId());
        if (ObjectUtil.isNull(volunteerActivity)) {
            throw new RuntimeException("志愿者活动不存在");
        }
        System.out.println(volunteerActivity);
        //判断活动是否结束 如果活动结束时间大于当前时间则已结束
        if (volunteerActivity.getEndTime().isBefore(LocalDateTime.now())) {
            volunteerActivity.setStatus("02");
        } else {
            throw new RuntimeException("活动还未结束，还无法进行审核");
        }
        //判断活动是否已经被审核
        if (volunteerActivity.getIsAudited() == 1) {
            return false;
        }
        return true;
    }

    /**
     * 查询单个活动
     *
     * @param id
     * @return
     */
    @Override
    public VolunteerActivity selectOne(Integer id) {
        //参数判断
        if (ObjectUtil.isNull(id) || id < 0) {
            throw new RuntimeException("请正确合法id");
        }
        VolunteerActivity volunteerActivity = baseMapper.selectById(id);
        if (ObjectUtil.isNull(volunteerActivity)) {
            throw new RuntimeException("活动信息不存在");
        }
        return volunteerActivity;
    }

    /**
     * 更新志愿者活动信息
     *
     * @param volunteerActivity
     * @return
     */
    @Override
    public int updateActivity(VolunteerActivity volunteerActivity) {
        if (ObjectUtil.isNotNull(volunteerActivity)) {
            return baseMapper.updateById(volunteerActivity);
        }
        return 0;
    }


    /**
     * 根据条件查询志愿者列表
     *
     * @param volunteerActivity
     * @return
     */
    @Override
    public IPage<VolunteerActivity> selectListActivity(VolunteerActivity volunteerActivity) {
        if (ObjectUtil.isNull(volunteerActivity)) {
            volunteerActivity = new VolunteerActivity();
            volunteerActivity.setPageNo(1);
            volunteerActivity.setPageSize(20);
        } else {
            //如果传入页为空，默认第一页
            if (ObjectUtil.isNull(volunteerActivity.getPageNo()) || volunteerActivity.getPageNo() == 0) {
                //如果页码为null或者未赋值 设置默认值1
                volunteerActivity.setPageNo(1);
            } else if (volunteerActivity.getPageNo() < 0) {
                throw new RuntimeException("页码不能小于1");
            }
            //如果页数据为空，默认十条
            if (ObjectUtil.isNull(volunteerActivity.getPageSize()) || volunteerActivity.getPageSize() == 0) {
                //如果页数据数为null或者未赋值 设置默认值20
                volunteerActivity.setPageSize(20);
            } else if (volunteerActivity.getPageSize() < 0) {
                throw new RuntimeException("页数据不能小于1");
            }
        }
        LambdaQueryWrapper<VolunteerActivity> queryWrapper = new LambdaQueryWrapper<>();
        /**
         * 查询条件 StrUtil.isNotEmpty 判断字符串是否为空，为空则不作为查询匹配条件
         *        ObjectUtil.isNotNull 同理 判断 基本类型
         */
        queryWrapper
                .like(StrUtil.isNotEmpty(volunteerActivity.getActivityName()), VolunteerActivity::getActivityName, volunteerActivity.getActivityName())
                .eq(ObjectUtil.isNotNull(volunteerActivity.getId()), VolunteerActivity::getId, volunteerActivity.getId())
                .like(ObjectUtil.isNotNull(volunteerActivity.getActualDuration()), VolunteerActivity::getActualDuration, volunteerActivity.getActualDuration())
                .eq(StrUtil.isNotEmpty(volunteerActivity.getStatus()), VolunteerActivity::getStatus, volunteerActivity.getStatus())
                .like(ObjectUtil.isNotNull(volunteerActivity.getActivityAddress()), VolunteerActivity::getActivityAddress, volunteerActivity.getActivityAddress())
                .eq(ObjectUtil.isNotNull(volunteerActivity.getIsAudited()), VolunteerActivity::getIsAudited, volunteerActivity.getIsAudited())
                .eq(VolunteerActivity::getDeleted, 0);
        /**
         * 俩个参数 pageNo 当前页 pageSize 页大小
         */
        Page<VolunteerActivity> page = new Page<>();
        page.setCurrent(volunteerActivity.getPageNo()).setSize(volunteerActivity.getPageSize());
        return baseMapper.selectPage(page, queryWrapper);
    }

    /**
     * 根据活动状态查询志愿活动列表
     *
     * @param stutas
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityListVo findListByStutas(String stutas, Integer pageNo, Integer pageSize) {
        if (ObjectUtil.isEmpty(stutas)) {
            throw new RuntimeException("请输入活动状态");
        }
        if (!stutas.equals("00") && !stutas.equals("01") && !stutas.equals("02")) {
            throw new RuntimeException("活动状态错误");
        }
        //默认第一页
        if (ObjectUtil.isNull(pageNo)) {
            pageNo = 1;
        }
        //默认十条
        if (ObjectUtil.isNull(pageSize)) {
            pageSize = 10;
        }
        LambdaQueryWrapper<VolunteerActivity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(VolunteerActivity::getStatus, stutas)
                .eq(VolunteerActivity::getDeleted, 0)
                .orderByDesc(VolunteerActivity::getCreateAt);
        Page page = new Page<>(pageNo, pageSize);
        IPage<VolunteerActivity> iPage = baseMapper.selectPage(page, queryWrapper);
        ActivityListVo vo = new ActivityListVo();
        List<ActivityListItemVo> list = new LinkedList<>();
        iPage.getRecords().forEach(item -> {
            ActivityListItemVo itemVo = new ActivityListItemVo();
            itemVo.setImgUrl(item.getActivityImg())
                    .setActivityTitle(item.getActivityName())
                    .setDescription(item.getDescription())
                    .setPercent(item.getNumberOfAttendees() * 100 / item.getNumberOfNeed())
                    .setRegistrationPopulation(Objects.isNull(item.getNumberOfAttendees()) ? 0 : item.getNumberOfAttendees())
                    .setPredictDuration(item.getPredictDuration())
                    .setRemainingPlaces(item.getNumberOfNeed() - item.getNumberOfAttendees())
                    .setActivityId(item.getId());
            list.add(itemVo);
        });
        vo.setList(list);
        vo.setTotal(iPage.getTotal());
        return vo;
    }

    /**
     * 跟更新活动状态和真实时长
     */
    @Override
    public VolunteerActivity updateActivityStatus(AuditeActivityVo auditeActivity) {
        Boolean result = isAuditedActivity(auditeActivity);
        VolunteerActivity volunteerActivity = baseMapper.selectById(auditeActivity.getId());
        if (result) {
            volunteerActivity.setActualDuration(auditeActivity.getActualDuration());
            volunteerActivity.setIsAudited(1);
            baseMapper.updateById(volunteerActivity);
        }
        return volunteerActivity;
    }

    /**
     * 更新所有活动的状态
     */
    @Override
    public void updateActivityStatus() {
        log.info("更新活动状态的定时任务被执行了一次");
        long pageSize = 100;
        long pageNo = 1;
        LambdaQueryWrapper<VolunteerActivity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VolunteerActivity::getDeleted, 0);
        Page<VolunteerActivity> page = new Page<>();
        IPage<VolunteerActivity> selectPage;
        /// TODO 对于已结束未审核的活动（刚结束的活动），更新参加的志愿者的统计信息
        do {
            // 分页查询，一次查询100条
            page.setCurrent(pageNo).setSize(pageSize);
            selectPage = baseMapper.selectPage(page, queryWrapper);
            // 更新状态
            LocalDateTime now = LocalDateTime.now();
            List<VolunteerActivity> activities = selectPage.getRecords();
            for (VolunteerActivity item : activities) {
                initStatus(item,now);
            }
            // 批量更新
            baseMapper.updateStatus(activities);
            pageNo++;
        } while (pageNo * pageSize < selectPage.getTotal());
    }

    private void initStatus(VolunteerActivity volunteerActivity,LocalDateTime now) {
        if (volunteerActivity.getStartTime().isAfter(now)) {
            // now < startTime < endTime 还没开始
            volunteerActivity.setStatus(ActivityStatus.NOT_STARTED);
        } else if (volunteerActivity.getEndTime().isAfter(now)){
            // startTime < now < endTime 正在进行中
            volunteerActivity.setStatus(ActivityStatus.ON_GOING);
        } else {
            // startTime < endTime < now 已结束
            volunteerActivity.setStatus(ActivityStatus.FINISHED);
        }
    }

    /**
     * 活动签到
     *
     * @param volunteerId
     * @param activityId
     */
    @Override
    public boolean signIn(Integer volunteerId, Integer activityId) {
        SignUpRecord signUpRecord = signUpRecordService.getRecord(volunteerId, activityId);
        // 检查是否报名
        if (Objects.nonNull(signUpRecord)) {
            // 检查活动是否正在进行
            VolunteerActivity volunteerActivity = volunteerActivityService.selectOne(activityId);
            LocalDateTime now = LocalDateTime.now();
            if (volunteerActivity.getStartTime().isAfter(now)) {
                throw new RuntimeException("请在活动开始后进行签到");
            }
            if (volunteerActivity.getEndTime().isBefore(now)) {
                throw new RuntimeException("该活动已结束");
            }
            // 签到成功，更新签到状态
            signUpRecord.setIsSignIn(1);
            signUpRecordService.updateById(signUpRecord);
            return true;
        } else {
            throw new RuntimeException("未报名该活动");
        }
    }

    @Override
    public ActivityListVo selectUserActivityList(Integer volunteerID, Integer pageNo, Integer pageSize) {
        //跳过前多少条数据
        int skip = (pageNo-1) * pageSize;
        List<VolunteerActivity> activities = baseMapper.selectUserActivityList(volunteerID, skip, pageSize);
        List<ActivityListItemVo> collect = activities.stream()
                .map(ActivityVo::new)
                .map(ActivityListItemVo::new)
                .collect(Collectors.toList());
        //包装一下数据
        ActivityListVo listVo = new ActivityListVo();
        listVo.setTotal(Long.parseLong(String.valueOf(activities.size())));
        listVo.setList(collect);
        return listVo;
    }
}
