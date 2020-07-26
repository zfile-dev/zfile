package im.zhaojun.zfile.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import im.zhaojun.zfile.model.support.ResultBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 自定义 Security 配置类
 *
 * @author zhaojun
 */
@EnableWebSecurity
public class MySecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // .authenticationProvider(authenticationProvider())
                .exceptionHandling()
                // 未登录时，进行 json 格式的提示.
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=utf-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    PrintWriter out = response.getWriter();
                    out.write(objectMapper.writeValueAsString(ResultBean.error("未登录")));
                    out.flush();
                    out.close();
                })
                .and()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/admin/**").authenticated()
                .and()
                .formLogin() // 使用自带的登录
                // 登录失败，返回json
                .failureHandler((request, response, ex) -> {
                    response.setContentType("application/json;charset=utf-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    PrintWriter out = response.getWriter();
                    String msg;
                    if (ex instanceof UsernameNotFoundException || ex instanceof BadCredentialsException) {
                        msg = "用户名或密码错误";
                    } else {
                        msg = "登录失败";
                    }
                    out.write(objectMapper.writeValueAsString(ResultBean.error(msg)));
                    out.flush();
                    out.close();
                })
                // 登录成功，返回json
                .successHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=utf-8");
                    PrintWriter out = response.getWriter();
                    out.write(objectMapper.writeValueAsString(ResultBean.success(authentication)));
                    out.flush();
                    out.close();
                })
                .and()
                .exceptionHandling()
                // 没有权限，返回json
                .accessDeniedHandler((request, response, ex) -> {
                    response.setContentType("application/json;charset=utf-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    PrintWriter out = response.getWriter();
                    out.write(objectMapper.writeValueAsString(ResultBean.error("权限不足")));
                    out.flush();
                    out.close();
                })
                .and()
                .logout()
                // 退出成功，返回 json
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=utf-8");
                    PrintWriter out = response.getWriter();
                    out.write(objectMapper.writeValueAsString(ResultBean.error("注销成功")));
                    out.flush();
                    out.close();
                })
                .and()
                .logout().permitAll();

        http.cors();
        http.csrf().disable();
        http.headers().frameOptions().sameOrigin();
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedPercent(true);
        return firewall;
    }

    @Override
    public void configure(AuthenticationManagerBuilder web) throws Exception {
        web.userDetailsService(myUserDetailsServiceImpl()).passwordEncoder(passwordEncoder());
    }

    @Bean
    public MyUserDetailsServiceImpl myUserDetailsServiceImpl() {
        return new MyUserDetailsServiceImpl();
    }

    @Override
    public void configure(WebSecurity web) {
        // 对于在 header 里面增加 token 等类似情况，放行所有 OPTIONS 请求。
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
        web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new Md5PasswordEncoder();
    }

}