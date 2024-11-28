package dev.hugosiu.meetCode.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseAuthConfig {
//  @Value("${service.account.json}")
//  private String SERVICE_ACCOUNT;

  @Value("file:/config/service-account.json")
  Resource resource;

  @Bean
  FirebaseAuth firebaseAuth() throws IOException {
    System.out.println("========== FirebaseAuthConfig invoked! ==========");

//    FileInputStream serviceAccount =
//            new FileInputStream(String.valueOf(resource));

    FirebaseOptions options =  FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
            .build();

    var firebaseApp = FirebaseApp.initializeApp(options);

    return FirebaseAuth.getInstance(firebaseApp);
  }
}
