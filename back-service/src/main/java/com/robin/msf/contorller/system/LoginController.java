/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.msf.contorller.system;


import com.robin.core.web.controller.AbstractController;
import com.robin.core.web.util.Session;
import com.robin.msf.manager.system.LoginService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.*;
import lombok.Data;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController extends AbstractController {
    @Inject
    private LoginService loginService;


    @Post("/login")
    public Map<String, Object> login(HttpRequest<?> request, @Body QueryParam queryParam) {
        Map<String, Object> map = new HashMap();
        try {
            Session session = this.loginService.doLogin(queryParam.getAccountName(), queryParam.getPassword().toUpperCase());
            wrapSuccess(map);
            map.put("session", session);
        } catch (Exception ex) {
            wrapFailed(map, ex);
        }
        return map;
    }
    @Data
    public static class QueryParam{
        private String accountName;
        private String password;
    }

    @Get(value = "/getSession")
    public Map<String, Object> getSession(HttpRequest<?> request) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            Session session = loginService.ssoGetUserById(Long.valueOf(request.getParameters().get("userId")));
            if(request.getParameters().get("orgId")!=null && !request.getParameters().get("orgId").isEmpty()) {
                session.setOrgId(Long.valueOf(request.getParameters().get("orgId")));
            }
            loginService.getRights(session);
            retMap.put("session",session);
            wrapSuccess(retMap);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }


}