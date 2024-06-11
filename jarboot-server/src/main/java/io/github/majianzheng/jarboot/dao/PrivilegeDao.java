package io.github.majianzheng.jarboot.dao;

import io.github.majianzheng.jarboot.entity.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author majianzheng
 */
@Repository
public interface PrivilegeDao extends JpaRepository<Privilege, Long> {

    /**
     * 根据角色获取所有权限信息
     * @param role 角色
     * @return 权限信息
     */
    List<Privilege> findAllByRole(String role);

    /**
     * 根据角色获取所有权限信息
     * @param role 角色
     * @return 权限信息
     */
    List<Privilege> findAllByRoleIn(Collection<String> role);

    /**
     * 获取角色对某一资源的权限
     * @param role 角色
     * @param authCode 资源
     * @return 权限信息
     */
    Privilege findFirstByRoleAndAuthCode(String role, String authCode);

    /**
     * 根据角色删除所有权限信息
     * @param role 角色
     */
    @Modifying
    void deleteAllByRole(String role);
}
