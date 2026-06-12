package com.foodlab.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodlab.audit.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {

    @Select("SELECT * FROM audit_record WHERE business_type = #{businessType} AND business_id = #{businessId} AND deleted = 0 ORDER BY audit_level")
    List<AuditRecord> selectByBusiness(@Param("businessType") String businessType, @Param("businessId") Long businessId);

    @Select("SELECT * FROM audit_record WHERE business_type = #{businessType} AND business_id = #{businessId} AND audit_level = #{auditLevel} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    AuditRecord selectLatestByBusinessAndLevel(@Param("businessType") String businessType,
                                               @Param("businessId") Long businessId,
                                               @Param("auditLevel") Integer auditLevel);

    IPage<AuditRecord> selectAuditPage(Page<AuditRecord> page,
                                        @Param("businessType") String businessType,
                                        @Param("businessCode") String businessCode,
                                        @Param("auditLevel") Integer auditLevel,
                                        @Param("auditStatus") String auditStatus,
                                        @Param("auditorId") Long auditorId,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate);

    @Select("SELECT * FROM audit_record WHERE auditor_id = #{auditorId} AND audit_status = 'PENDING' AND deleted = 0 ORDER BY create_time DESC")
    List<AuditRecord> selectPendingByAuditorId(@Param("auditorId") Long auditorId);
}
