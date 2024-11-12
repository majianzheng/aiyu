package io.github.majianzheng.jarboot.dao;

import io.github.majianzheng.jarboot.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 审计日志
 * @author majianzheng
 */
@Repository
public interface AuditLogDao extends JpaRepository<AuditLog, Long> {

}
