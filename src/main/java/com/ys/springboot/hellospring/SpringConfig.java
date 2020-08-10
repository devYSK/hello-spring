package com.ys.springboot.hellospring;

import com.ys.springboot.hellospring.repository.MemberRepository;
import com.ys.springboot.hellospring.repository.MemoryMemberRepository;
import com.ys.springboot.hellospring.service.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
}
