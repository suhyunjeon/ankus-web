/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openflamingo.web;

import org.openflamingo.core.exception.ServiceException;
import org.openflamingo.model.rest.Response;
import org.openflamingo.util.ExceptionUtils;
import org.openflamingo.web.core.ConfigurationHelper;
import org.openflamingo.web.member.Member;
import org.openflamingo.web.member.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * 인덱스 페이지 및 기본적인 페이지 이동 기능을 제공하는 컨트롤러.
 */
@Controller
@RequestMapping("/")
public class IndexController {

    /**
     * SLF4J Logging
     */
    private Logger logger = LoggerFactory.getLogger(IndexController.class);

    /**
     * Memberd Service
     */
    @Autowired
    private MemberService memberService;

    /**
     * 인덱스 페이지로 이동한다.
     *
     * @return Model And View
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("/index");
        mav.addObject("locale", ConfigurationHelper.getHelper().get("application.locale", "English"));
        mav.addObject("mode", ConfigurationHelper.getHelper().get("application.mode", "development"));

        mav.addObject("version", ConfigurationHelper.getHelper().get("version"));
        mav.addObject("timestamp", ConfigurationHelper.getHelper().get("build.timestamp"));
        mav.addObject("buildNumber", ConfigurationHelper.getHelper().get("build.number"));
        mav.addObject("revision", ConfigurationHelper.getHelper().get("revision.number"));
        mav.addObject("buildKey", ConfigurationHelper.getHelper().get("build.key"));
        return mav;
    }

    @RequestMapping(value = "authenticate", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response authenticate(HttpServletRequest request, @RequestBody Map<String, String> param) {
        Response response = new Response();
        try {
            Member member = memberService.getMemberByUser(param.get("username"));
            if (member == null) {
                throw new ServiceException("사용자 정보를 확인할 수 없습니다.");
            }

            if (member.getPassword().equals(param.get("password"))) {
                response.setSuccess(true);

                // 로그인 사용자 계정 활성화 체크를 위한 response parameter
                response.setObject(member.isEnabled());
                // 로그인 사용자 계정 활성화 체크를 위한 에러 메세지
                response.getError().setMessage("로그인 권한이 없습니다.");

                HttpSession session = request.getSession(true);
                session.setAttribute("user", member);
                session.setAttribute("authority", member.getAuthority());
                session.setAttribute("username", member.getUsername());
                session.setAttribute("login", new Date());
                logger.info("'{}' 사용자의 세션을 생성했습니다.", member.getUsername());

                return response;
            }
            throw new ServiceException("패스워드가 잘못되었습니다.");
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
        }
        return response;
    }

    /**
     * 로그아웃 처리를 한 후 메인 페이지로 이동한다.
     *
     * @return Model And View
     */
    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        session = null;
        return new ModelAndView("/index");
    }

    /**
     * 메인 페이지로 이동한다.
     *
     * @return Model And View
     */
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ModelAndView("/index");
        }

        if (session.getAttribute("user") == null) {
            return new ModelAndView("/index");
        }

        ModelAndView mav = new ModelAndView("/main");
        mav.addObject("user", session.getAttribute("user"));
        return mav;
    }

    /**
     * Username 과 Password 로 Email 을 조회한다.
     *
     * @param param
     * @return
     * @auth jhlife
     * @date 2014.02.08
     */
    @RequestMapping(value = "/finduser", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response findemail(@RequestBody Map<String, String> param) {
        Response response = new Response();

        try {
            String email = param.get("email");
            String password = param.get("password");

            Member member = memberService.getMemberByUsername(email, password);
            if (member == null) {
                throw new ServiceException("There is no email address. NOW you can join us!");
            }

            if (member.getPassword().equals(password)) {
                response.setSuccess(true);
                response.setObject(member.getUsername());
                return response;
            }
            throw new ServiceException("Sorry, email and password are unmatched. Please try again.");

        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
        }

        return response;
    }

    /**
     * Username 과 Email 주소로 Password 를 조회한다.
     *
     * @param param
     * @return
     * @auth jhlife
     * @date 2014.02.08
     */
    @RequestMapping(value = "/findpass", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response findpass(@RequestBody Map<String, String> param) {
        Response response = new Response();
        try {
            String username = param.get("username");
            String email = param.get("email");

            Member member = memberService.getMemberByPassword(username, email);

            if (member.getEmail().equals(email)) {
                response.setSuccess(true);
                response.setObject(member.getPassword());

                return response;
            }
            throw new ServiceException("Sorry, username and E-mail address are unmatched!");

        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
        }
        return response;
    }

    /**
     * 신규 Member 를 등록한다.
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response signup(@RequestBody Map<String, String> param) {
        Response response = new Response();

        try {
            String username = param.get("username");
            int existUsername = memberService.existUsername(username);

            if (existUsername > 0) {
                throw new ServiceException("Username '" + username + "' already exists!");
            }

            String email = param.get("email");
            if (email != null && !email.isEmpty()) {
                int count = memberService.getEmailCount(email);
                if (count > 0) {
                    throw new ServiceException("Email already exists!");
                }
            }

            Member member = new Member();
            member.setUsername(username);
            member.setPassword(param.get("password"));
            member.setName(param.get("username"));
            member.setEmail(param.get("email"));
            member.setAuthority("ROLE_USER");

            memberService.registerMember(member);
            response.setSuccess(true);

        } catch (Exception ex) {
            /*ex.printStackTrace();*/
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
        }

        return response;
    }

    /**
     * 전체 Member 목록을 가져온다.
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getMembers", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getMembers(@RequestBody Map<String, String> param) {
        Response response = new Response();

        try {
            List<Member> members = memberService.getMembers(param);
            response.setSuccess(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
        }

        return response;
    }

    /**
     * 지정한 페이지로 리다이렉트한다.
     *
     * @return Model And View
     */
    @RequestMapping(value = "redirect", method = RequestMethod.GET)
    public ModelAndView redirect(@RequestParam String redirect) {
        return new ModelAndView("redirect:" + redirect);
    }

    /**
     * 지정한 페이지로 포워딩한다.
     *
     * @return Model And View
     */
    @RequestMapping(value = "forward", method = RequestMethod.GET)
    public ModelAndView forward(@RequestParam String redirect) {
        return new ModelAndView("forward:" + redirect);
    }

}
