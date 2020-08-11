# [강좌] 스프링 입문 - 코드로 배우는 스프링 부트, 웹 MVC, DB 접근 기술 - chap 6. 스프링 DB 접근 기술 - (4)  JPA

# JPA

* JPA는 기존의 반복 코드는 물론이고, 기본적인 SQL도 JPA가 직접 만들어서 실행해준다.
    * sql을 개발자가 작성할 필요가 줄어든다.


* JPA를 사용하면, SQL과 데이터 중심의 설계에서 객체 중심의 설계로 패러다임을 전환을 할 수 있다.

* JPA를 사용하면 개발 생산성을 크게 높일 수 있다

* JPA는 인터페이스고, HIBERNATE같은 구현체들로 사용하낟. 

* JPA는 ORM 기술이다 (Object Relational Mapping)
### `build.gradle 파일에 JPA, h2 데이터베이스 관련 라이브러리 추가`

```gradle
dependencies {
    implementation 'org.springframeworkboot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    //implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.h2database:h2'
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
    exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
}

```

* `spring-boot-starter-data-jpa` 는 내부에 jdbc 관련 라이브러리를 포함한다. 따라서 jdbc는 제거해도 된다.

### `스프링 부트 properties에 JPA 설정 추가 `
```
resources/application.properties 
```

```properties
spring.datasource.url=jdbc:h2:tcp://localhost/~/test
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=none
```

* `show-sql` : JPA가 생성하는 SQL을 출력한다.
    * 콘솔창에 출력해준다. 
* `ddl-auto` : JPA는 테이블을 자동으로 생성하는 기능을 제공하는데 none 를 사용하면 해당 기능을 끈다.

* `create` 를 사용하면 엔티티 정보를 바탕으로 테이블도 직접 생성해준다

### JPA를 쓸라면 엔티티 맵핑을 해야한다. 
```java
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
@Entity
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

```
* `@Entity` :어노테이션을 붙이면 JPA가 관리해준다.
* `@Id` : Pk를 설정하는 어노테이션. 엔티티클래스에는 Pk(@id)가 필수다.   
* `@GeneratedValue(strategy = GenerationType.IDENTITY)` : AutoIncrement 설정

### JPA 레포지토리 생성
```java

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class JpaMemberRepository implements MemberRepository {

    private final EntityManager em;

    public JpaMemberRepository(EntityManager em) {
        this.em = em;
    }
    @Override
    public Member save(Member member) {
        em.persist(member); // 이러면 jpa가 모든 처리를 해준다.
        return member;

    }

    @Override
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    @Override
    public Optional<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class )
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findAny();
    }

    @Override
    public List<Member> findAll() {

        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
}

```


* EntityManager 를 주입 받아야 한다. 
* `em.createQuery` : `jpql` 이라는 쿼리 언어. 엔티티(객체)를 대상으로 쿼리를 날리는것.
* jpa 사용시 주의사항 : `서비스 계층에 트랜잭션이 필요하다 `. 모든 변경이 트랜잭션 안에서 사용되야 한다
    ```java
    @Transactional
    public class MemberService {
    ...
    }
    ```
* `org.springframework.transaction.annotation.Transactional` 를 사용하자.

* 스프링은 해당 클래스의 메서드를 실행할 때 트랜잭션을 시작하고, 메서드가 정상 종료되면 트랜잭션을 커
밋한다. 만약 런타임 예외가 발생하면 롤백한다.

* JPA를 통한 모든 데이터 변경은 트랜잭션 안에서 실행해야 한다.

### JPA를 사용하도록 스프링 설정 변경
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

@Configuration
public class SpringConfig {

//    private final DataSource dataSource;
//
//    @Autowired
//    public SpringConfig(DataSource dataSource) {
//        this.dataSource = dataSource;
//    }

    private final EntityManager em;

    @Autowired
    public SpringConfig(EntityManager em) {
        this.em = em;
    }

    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {

        //return new MemoryMemberRepository();
        //return new JdbcMemberRepository(dataSource);
        //return new JdbcTemplateMemberRepository(dataSource);
        return new JpaMemberRepository(em);
    }

}


```
> 참고: JPA도 스프링 만큼 성숙한 기술이고, 학습해야 할 분량도 방대하다. 다음 강의와 책을 참고하자.
> - 인프런 강의 링크: 인프런 - 자바 ORM 표준 JPA 프로그래밍 - 기본편
> - JPA 책 링크: 자바 ORM 표준 JPA 프로그래밍 - YES24