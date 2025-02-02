package team.sb.authorizationserver.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import team.sb.authorizationserver.global.security.custom.CustomUserDetailsService;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    private String signKey;

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailService;

    @Value("${security.oauth2.jwt.signkey}")
    private void setSecretKey(String secretKey) {
        this.signKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource).passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        super.configure(endpoints);

//        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
//        tokenEnhancerChain.setTokenEnhancers(
//                Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter())
//        );

        endpoints
                .accessTokenConverter(jwtAccessTokenConverter())
                .tokenStore(tokenStore())
//                .tokenEnhancer(tokenEnhancer())
                .userDetailsService(userDetailService);
    }

//    // keystore 사용
//    @Bean
//    public JwtAccessTokenConverter jwtAccessTokenConverter() {
//        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
//                new ClassPathResource("src/main/resources/sbtoken.jks"), "sbpass".toCharArray()
//        );
//        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("sbauth"));
//        return converter;
//    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(signKey);
        return converter;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

//    // token에 추가 정보 넣을 때 사용
//    @Bean
//    public TokenEnhancer tokenEnhancer() {
//        return new CustomTokenEnhancer();
//    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
                .allowFormAuthenticationForClients();
    }

}
