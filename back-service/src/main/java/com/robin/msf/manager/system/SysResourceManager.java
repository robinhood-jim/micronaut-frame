package com.robin.msf.manager.system;

import com.robin.core.base.manager.JdbcManager;
import com.robin.core.base.util.Const;
import com.robin.msf.model.system.SysResource;
import com.robin.msf.model.user.SysResourceUser;
import io.micronaut.spring.tx.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import javax.inject.Singleton;
import java.util.List;


@Singleton
public class SysResourceManager extends JdbcManager<SysResource,Long> {
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    public void updateUserResourceRight(Long userId, List<String> addList, List<String> delList){
        this.getJdbcRepository().deleteByField(SysResourceUser.class, "userId", userId);
        //Add Right
        if(addList!=null && !addList.isEmpty()){
            for (String addId:addList) {
                SysResourceUser vo=new SysResourceUser();
                vo.setUserId(userId);
                vo.setResId(Integer.valueOf(addId));
                vo.setAssignType(SysResourceUser.ASSIGN_ADD);
                vo.setStatus("1");
                this.getJdbcRepository().createVO(vo);
            }
        }
        //Delete Right
        if(delList!=null && !delList.isEmpty()){
            for (String delId:delList) {
                SysResourceUser vo=new SysResourceUser();
                vo.setUserId(userId);
                vo.setResId(new Integer(delId));
                vo.setAssignType(SysResourceUser.ASSIGN_DEL);
                vo.setStatus("1");
                this.getJdbcRepository().createVO(vo);
            }
        }

    }
    public List<SysResource> getOrgAllMenu(Long orgId){
        SysResource queryVO=new SysResource();
        queryVO.setStatus(Const.VALID);
        queryVO.setOrgId(orgId);
        return queryByVO(queryVO,null,null);
    }
}
