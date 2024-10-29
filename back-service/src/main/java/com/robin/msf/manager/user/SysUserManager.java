package com.robin.msf.manager.user;

import com.robin.core.base.manager.JdbcManager;
import com.robin.msf.model.user.SysUser;
import jakarta.inject.Singleton;


/**
 * <p>Created at: 2019-10-17 16:42:27</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Singleton
public class SysUserManager extends JdbcManager<SysUser,Long> {

}
