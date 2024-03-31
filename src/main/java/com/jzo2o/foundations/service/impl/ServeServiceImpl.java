package com.jzo2o.foundations.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.enums.HotStatusEnum;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-07-03
 */
@Service
public class ServeServiceImpl extends ServiceImpl<ServeMapper, Serve> implements IServeService {
    @Resource
    private ServeItemMapper serveItemMapper;

    @Resource
    private RegionMapper regionMapper;

    /**
     * 分页查询
     *
     * @param servePageQueryReqDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO) {
        //调用mapper查询数据，这里由于继承了ServiceImpl<ServeMapper, Serve>，使用baseMapper相当于使用ServeMapper
        PageResult<ServeResDTO> serveResDTOPageResult = PageHelperUtils.selectPage(servePageQueryReqDTO, () -> baseMapper.queryServeListByRegionId(servePageQueryReqDTO.getRegionId()));
        return serveResDTOPageResult;

    }

    @Override
    @Transactional
    public void batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOList) {
        for(ServeUpsertReqDTO serveUpsertReqDTO : serveUpsertReqDTOList) {
            ServeItem serveItem = serveItemMapper.selectById(serveUpsertReqDTO.getServeItemId());
            if(ObjectUtil.isNull(serveItem) || serveItem.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
                throw new ForbiddenOperationException("该服务未启用无法添加到区域下使用");
            }
            Integer count = lambdaQuery()
                    .eq(Serve::getServeItemId, serveUpsertReqDTO.getServeItemId())
                    .eq(Serve::getRegionId, serveUpsertReqDTO.getRegionId())
                    .count();
            if(count > 0) {
                throw new ForbiddenOperationException("该服务已存在");
            }
            Serve serve = BeanUtil.toBean(serveUpsertReqDTO, Serve.class);
            Region region = regionMapper.selectById(serveUpsertReqDTO.getRegionId());
            serve.setCityCode(region.getCityCode());
            baseMapper.insert(serve);
        }
        }

    @Override
    @Transactional
    public Serve update(Long id, BigDecimal price) {
        //1.更新服务价格
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getPrice, price)
                .update();
        if(!update){
            throw new CommonException("修改服务价格失败");
        }
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional
    public Serve onSale(Long id) {
        Serve serve = baseMapper.selectById(id);
        if(ObjectUtil.isNull(serve)) {
            throw new CommonException("区域服务不存在");
        }
        Integer saleStatus = serve.getSaleStatus();
        if(!(saleStatus == FoundationStatusEnum.INIT.getStatus() || saleStatus == FoundationStatusEnum.DISABLE.getStatus())) {
            throw new CommonException("草稿或下架状态方可上架");
        }
        Long serveItemId = serve.getServeItemId();
        ServeItem serveItem = serveItemMapper.selectById(serveItemId);
        if (ObjectUtil.isNull(serveItem)) {
            throw new CommonException("所属服务项不存在");
        }
        Integer activeStatus = serveItem.getActiveStatus();
        if (activeStatus != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new CommonException("服务项为启用状态方可上架");
        }
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus())
                .update();
        if(!update){
            throw new CommonException("上架服务失败");
        }
        return baseMapper.selectById(id);
    }
    @Override
    public Serve onHot(Long id) {
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new CommonException("区域服务不存在");
        }
        Integer saleStatus = serve.getSaleStatus();
        if (saleStatus != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new CommonException("服务上架才可设置热门");
        }
        Long serveItemId = serve.getServeItemId();
        ServeItem serveItem = serveItemMapper.selectById(serveItemId);
        if (ObjectUtil.isNull(serveItem)) {
            throw new CommonException("所属服务项不存在");
        }
        Integer activeStatus = serveItem.getActiveStatus();
        if (activeStatus != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new CommonException("服务项为启用状态方可设置热门");
        }
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getIsHot, HotStatusEnum.ENABLE.getStatus())
                .update();
        if(!update) {
            throw new CommonException("设置热门失败");
        }
        return baseMapper.selectById(id);
    }

    @Override
    public Serve offHot(Long id) {
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new CommonException("未找到服务");
        }
        Integer saleStatus = serve.getIsHot();
        if(saleStatus != HotStatusEnum.ENABLE.getStatus()) {
            throw new CommonException("服务非热门，无法取消");
        }
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getIsHot, HotStatusEnum.DISABLE.getStatus())
                .update();
        if (!update) {
            throw new CommonException("取消热门失败");
        }
        return baseMapper.selectById(id);
    }

    @Override
    public Integer queryServeCountByRegionIdAndSaleStatus(Long regionId, Integer saleStatus) {
        Integer count = lambdaQuery()
                .eq(Serve::getRegionId, regionId)
                .eq(Serve::getSaleStatus, saleStatus)
                .count();
        // 手动装箱
        // Integer manualBoxed = Integer.valueOf(100);
        // 手动拆箱
        // int manualUnboxed = manualBoxed.intValue();
        return count;
    }

    @Override
    public void delete(Long id) {
        Serve serve = baseMapper.selectById(id);
        Integer saleStatus = serve.getSaleStatus();
        if(saleStatus != FoundationStatusEnum.INIT.getStatus()) {
            throw new CommonException("服务非草稿，无法删除");
        }
       baseMapper.deleteById(id);

    }

    @Override
    public Serve offSale(Long id) {
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new CommonException("未找到服务");
        }
        Integer saleStatus = serve.getSaleStatus();
        if(saleStatus != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new CommonException("服务非上架，无法下架");
        }
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getSaleStatus, FoundationStatusEnum.DISABLE.getStatus())
                .update();
        if (!update) {
            throw new CommonException("下架服务失败");
        }

        return baseMapper.selectById(id);
    }




}


