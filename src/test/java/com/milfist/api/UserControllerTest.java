package com.milfist.api;

import com.milfist.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.common.util.JacksonJsonParser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = Application.class)
public class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void givenToken_whenPostGetSecureRequest_thenOk() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvc.perform(get("/user")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void givenToken_whenPostGetSecureRequest_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .params(getParams())
                .with(httpBasic("user","password"))
                .accept("application/json;charset=UTF-8"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenNoToken_whenPostGetOpenRequest_thenOk() throws Exception {
        mockMvc.perform(get("/actuator")).andExpect(status().isOk());
    }

    @Test
    public void givenInvalidToken_whenGetSecureRequest_thenUnauthorized() throws Exception {
        String accessToken = "c0ce723f-7f9a-45eb-9435-082bddf963fd";
        mockMvc.perform(get("/user")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenNoToken_whenGetSecureRequest_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/user")).andExpect(status().isUnauthorized());
    }

    private String obtainAccessToken() throws Exception {
        ResultActions resultActions = postToOauthTokenPoint();
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("access_token").toString();
    }

    private MultiValueMap<String, String> getParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        return params;
    }

    private ResultActions postToOauthTokenPoint() throws Exception {
        return mockMvc.perform(post("/oauth/token")
                .params(getParams())
                .with(httpBasic("postman_client","user"))
                .accept("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }
}
