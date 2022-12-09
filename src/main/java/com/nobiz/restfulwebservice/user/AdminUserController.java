package com.nobiz.restfulwebservice.user;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminUserController {
    private UserDaoService service;

    public AdminUserController(UserDaoService service) {
        this.service = service;
    }

    @GetMapping(path = "/users")
    public MappingJacksonValue retrieveAllUsers() {
        List<User> users = service.findAll();

        /*
        * 전달하는 값을 filtering 하는 방법
        * 전달하려는 instance 에 @JsonFilter 먼저 선언
        * 아래의 방식으로 filter 설정 후 아래의 return 값에 도출
        * return MappingJacksonValue
        * */
        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
                .filterOutAllExcept("id", "name", "password", "ssn");

        FilterProvider filters = new SimpleFilterProvider().addFilter("UserInfo", filter);

        MappingJacksonValue mapping = new MappingJacksonValue(users);
        mapping.setFilters(filters);

        return mapping;
    }

    // GET /admin/users/1 -> /admin/v1/users/1
//    1. 일반 웹 브라우저에서 사용가능
//    @GetMapping(path ="/v1/users/{id}")
//    @GetMapping(path = "/users/{id}/", params = "version=1")
//    2. 일반 웹 브라우저에서 불가능
//    @GetMapping(value = "/users/{id}", headers = "X-API-VERSION=1")
    @GetMapping(path ="/users/{id}", produces = "application/vnd.company.appv1+json")
    public MappingJacksonValue retrieveUserV1(@PathVariable int id) {
        User user = service.findOne(id);

        if (user == null) {
            throw new UserNotFoundException(String.format("ID[%s] not found", id));
        }

        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
                .filterOutAllExcept("id", "name", "password", "ssn");

        FilterProvider filters = new SimpleFilterProvider().addFilter("UserInfo", filter);

        MappingJacksonValue mapping = new MappingJacksonValue(user);
        mapping.setFilters(filters);

        return mapping;
    }

//    @GetMapping(path ="/v2/users/{id}")
//    @GetMapping(path = "/users/{id}/", params = "version=2")
//    @GetMapping(value = "/users/{id}", headers = "X-API-VERSION=2")
    @GetMapping(path ="/users/{id}", produces = "application/vnd.company.appv2+json")
    public MappingJacksonValue retrieveUserV2(@PathVariable int id) {
        User user = service.findOne(id);

        if (user == null) {
            throw new UserNotFoundException(String.format("ID[%s] not found", id));
        }

        // User -> UserV2
        UserV2 userV2 = new UserV2();
        BeanUtils.copyProperties(user, userV2); //id, name, joinDate, password, ssn
        userV2.setGrade("VIP");

        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
                .filterOutAllExcept("id", "name", "joinDate", "grade");

        FilterProvider filters = new SimpleFilterProvider().addFilter("UserInfoV2", filter);

        MappingJacksonValue mapping = new MappingJacksonValue(userV2);
        mapping.setFilters(filters);

        return mapping;
    }
}
